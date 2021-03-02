import io.threadcso._
import scala.collection.mutable.Queue

class buffer[T] {

    def buff[T](in: ?[T], out: ![T]) = proc {
        val values = new Queue[T]()
        serve (
            in =?=> { x =>
                values.enqueue(x)
            }
            | (!values.isEmpty && out) =!=> {
                val x = values.dequeue
                x
            }

        )

    }


}