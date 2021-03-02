import io.threadcso._

class resource {

    // semaphore to signal when it is possible to use the resource
    private val entryPossible = SignallingSemaphore()
    private val mutex = MutexSemaphore()
    private var total = 0
    private var waiting = 0

    def enter(id: Int) = {
        mutex.down
        // if possible to enter, enter
        if (total % 3 == 0) {
            total += id
            mutex.up
        }
        else {
            waiting += 1
            mutex.up
            // otherwise block until told you are able to enter and enter
            entryPossible.down
            waiting -= 1
            total += id
            // if another process is able to enter, signal and pass the baton to next process
            if (total % 3 == 0 && waiting != 0) entryPossible.up
            else mutex.up
        }

    }

    def exit(id: Int) = {
        mutex.down
        // exit the resource
        total = total - id
        // if another process is able to enter, signal and pass the baton to next process
        if (total % 3 == 0 && waiting != 0) entryPossible.up
        else mutex.up
    }
}

import ox.cads.testing._
import scala.collection.immutable.Set

object resourceTest {

    type SeqDatabase = Set[Int]
    type ConcDatabase = resource

    // number of readers
    val N = 5
    // number of iterations
    val I = 10

    // functions for sequential implementation
    // precondition that the sum of ids must be a multiple of 3
    def seqEnter(me: Int)(set: Set[Int]) : (Unit, Set[Int]) = {
        require(set.sum % 3 == 0)
        ((), set.incl(me))
    }

    def seqExit(me: Int)(set: Set[Int]) : (Unit, Set[Int]) = {
        ((), set.excl(me))
    }

    def reader(me: Int, log: GenericThreadLog[SeqDatabase, ConcDatabase]) = {
        for (i <- 0 until I) {
            // attempt to access the resource
            log.log(_.enter(me), "Entering", seqEnter(me))
            // attempt to exit accessing the resource
            log.log(_.exit(me), "Exiting", seqExit(me))
        }
    }

    def doTest = {
        // concurrent resource monitor object
        val concDatabase = new resource
        // set to store which processes are active for sequential part of test
        val seqDatabase: Set[Int] = Set()
        val tester = LinearizabilityTester.JITGraph[SeqDatabase, ConcDatabase](seqDatabase, concDatabase, N, reader _, 2 * I)
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