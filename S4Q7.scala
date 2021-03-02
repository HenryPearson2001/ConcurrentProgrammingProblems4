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

    def womanSync(me: String): String = monitor.withLock {
        mutex.down
        if (!men.isEmpty) {
            val (sem, id) = men.dequeue
            pairs = (me, id)::pairs
            sem.up
            id
        }
        else {
            val mySem = SignallingSemaphore()
            women.enqueue((mySem, me))
            mutex.up
            mySem.down
            val (_, id) = pairs(pairs.indexWhere(_._1 == me))
            mutex.up
            id
        }
    }

}

