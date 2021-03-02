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