import io.threadcso._
import scala.collection.mutable.Queue

class boundedBuffer[T](n: Int) {

    private val values = new Queue[T]()

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

import ox.cads.testing._
import scala.util.Random
import scala.collection.immutable.Set

object bufferTest {
    // size of bounded buffer
    val n = 20

    type SeqBuffer = Queue[Int]
    type ConcBuffer = boundedBuffer[Int]

    // number of readers
    val N = 5
    // number of iterations
    val I = 5

    // functions for sequential implementation
    // precondition that the sum of ids must be a multiple of 3
    def seqPut(x: Int)(buffer: Queue[Int]) : (Unit, Queue[Int]) = {
        require(buffer.size <= n)
        ((), buffer.enqueue(x))
    }

    def seqGet(buffer: Queue[Int]) : (Int, Queue[Int]) = {
        require(!buffer.isEmpty)
        val value = buffer.dequeue
        (value, buffer)
    }

    def reader(me: Int, log: GenericThreadLog[SeqBuffer, ConcBuffer]) = {
        for (i <- 0 until I) {
            // attempt to access the resource
            log.log(_.put(me), "Putting", seqPut(me))
            // attempt to exit accessing the resource
            log.log(_.get, "Getting", seqGet)
        }
    }

    def doTest = {
        // concurrent resource monitor object
        val concBuffer = new boundedBuffer[Int](n)
        // set to store which processes are active for sequential part of test
        val seqBuffer = Queue[Int]()
        val tester = LinearizabilityTester.JITGraph[SeqBuffer, ConcBuffer](seqBuffer, concBuffer, N, reader _, 2 * I)
        assert(tester() > 0)
    }

    def main(args: Array[String]) = {
        for(i <- 0 until 1000){
            doTest
            if(i%10 == 0) print(".")
        }
        println; io.threadcso.exit
    }
}