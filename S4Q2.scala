import io.threadcso._
import scala.collection.mutable.Queue

class pairMonitor {

    private type person = (Chan[String], String)
    private val men = new Queue[person]()
    private val women = new Queue[person]()

    def manSync(me: String): String = synchronized {
        if (!women.isEmpty) {
            val (chan, id) = women.dequeue
            chan!(me)
            id
        }
        else {
            val myChan = OneOne[String]
            men.enqueue((myChan, me))
            myChan?()
        }
    }

    def womanSync(me: String): String = synchronized {
        if (!men.isEmpty) {
            val (chan, id) = men.dequeue
            chan!(me)
            id
        }
        else {
            val myChan = OneOne[String]
            women.enqueue((myChan, me))
            myChan?()
        }
    }

}


import scala.util.Random

object pairMonitorTest {
    // N is number of clients, time is time server is run for
    private val N = 50; private val time = 1

    private var pairs = List[pair]()

    // to communicate between clients and test - for simplicity women are first string
    private type pair = (String, String)
    val pairChan = ManyOne[pair]

    def receivePairs = proc {
        repeat {
            pairs = pairChan?()::pairs
        }
    }

    // process to simulate a client - will request a pair then send the pairing to the test method
    // true for women, false for men
    def client(me: String, sex: Boolean) = proc {
        attempt {
            if (sex) {
                val result = myPairMonitor.manSync(me)
                pairChan!(result, me)
            }
            else {
                val result = myPairMonitor.womanSync(me)
                pairChan!(me, result)
            }
        }
        {}
    }

    // checks if every possible pair has been formed (either only men or only women without a pair)
    // and checks each pair has exactly one man and one woman
    def checkPairs(men: Array[String], women: Array[String]): Boolean = {
        var womenUnpaired = false
        var menUnpaired = false
        for ((man, woman) <- pairs) {
            if (man == "N/A") womenUnpaired = true
            if (woman == "N/A") menUnpaired = true
            if ((!men.contains(man)) && (man != "N/A")) false
            if ((!women.contains(woman)) && (woman != "N/A")) false
        }
        if (menUnpaired && womenUnpaired) false
        true
    }

    def doTest = {
        myPairMonitor = new pairMonitor
        val numberOfMen = Random.nextInt(N)
        val men = Array.fill(numberOfMen)(Random.alphanumeric.take(10).mkString)
        val women = Array.fill(N - numberOfMen)(Random.alphanumeric.take(10).mkString)
        val clients = (|| (for (person <- men) yield client(person, false))) || (|| (for (person <- women) yield client(person, true)))
        run (clients || receivePairs)
        assert(checkPairs(men, women))
    }

    def main(args: Array[String]) = {
        for(i <- 0 until 1000) {
            doTest; if(i%10 == 0) print(".")
        }
        println; io.threadcso.exit
    }
}
