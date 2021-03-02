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

