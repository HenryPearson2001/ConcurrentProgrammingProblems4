import io.threadcso._
import scala.collection.mutable.Queue
import scala.collection.immutable.List

class pairSemaphore {

    private type person = (Semaphore, String)
    private val men = new Queue[person]()
    private val women = new Queue[person]()
    private var pairs = List[(String, String)]()

    private val mutex = MutexSemaphore()

    def manSync(me: String): String = {
        mutex.down
        if (!women.isEmpty) {
            val (sem, id) = women.dequeue
            pairs = (me, id)::pairs
            sem.up
            id
        }
        else {
            val mySem = SignallingSemaphore()
            men.enqueue((mySem, me))
            mutex.up
            mySem.down
            val (_, id) = pairs(pairs.indexWhere(_._1 == me))
            mutex.up
            id
        }
    }

    def womanSync(me: String): String = {
        mutex.down
        if (!men.isEmpty) {
            val (sem, id) = men.dequeue
            pairs = (id, me)::pairs
            sem.up
            id
        }
        else {
            val mySem = SignallingSemaphore()
            women.enqueue((mySem, me))
            mutex.up
            mySem.down
            val (id, _) = pairs(pairs.indexWhere(_._2 == me))
            mutex.up
            id
        }
    }

}

import scala.util.Random

object pairSemaphoreTest {
    // N is number of clients, time is time server is run for
    private val N = 5; private val time = 1

    private var pairs = List[pair]()

    private var myPairSemaphore = new pairSemaphore

    // to communicate between clients and test - for simplicity women are first string
    private type pair = (String, String)
    val pairChan = ManyOne[pair]

    def receivePairs = proc {
        for (i <- 0 until N + N) {
            // println(i)
            pairs = pairChan?()::pairs
        }
    }

    // process to simulate a client - will request a pair then send the pairing to the test method
    // true for women, false for men
    def client(me: String, sex: Boolean) = proc {
        attempt {
            if (sex) {
                val result = myPairSemaphore.manSync(me)
                pairChan!(result, me)
            }
            else {
                val result = myPairSemaphore.womanSync(me)
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
        val numberOfMen = Random.nextInt(N)
        val men = Array.fill(N)(Random.alphanumeric.take(10).mkString)
        val women = Array.fill(N)(Random.alphanumeric.take(10).mkString)
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