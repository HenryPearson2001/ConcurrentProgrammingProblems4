import io.threadcso._
import scala.collection.mutable.Queue

class pairMonitor {

    private type person = (Chan[String], String)
    private men = new Queue[person]()
    private women = new Queue[person]()

    private monitor = new Monitor

    private val menNonEmpty, womenNonEmpty = monitor.newCondition

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

