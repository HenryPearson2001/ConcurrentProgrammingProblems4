import io.threadcso._
import scala.collection.mutable.Queue

class boundedBuffer[T](n: Int) {

    private val values = new Queue()

    // semaphore to prevent data being added when buffer is full
    private val full = CountingSemaphore(n)
    // semaphore to prevent data being added when buffer is empty
    private val empty = CountingSemaphore(0)

    private val mutex = MutexSemaphore()

    def put(x: T) = {
        full.down
        mutex.down
        values.enqueue(x)
        empty.up
        mutex.up
    }

    def get: T = {
        empty.down
        mutex.down
        val value = values.dequeue
        full.up
        mutex.up
        value
    }

}

