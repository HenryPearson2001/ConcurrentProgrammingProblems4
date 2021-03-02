import io.threadcso._
import scala.collection.mutable.Queue

abstract class IntTree
case class Leaf(n: Int) extends IntTree
case class Branch(l: IntTree, r: IntTree) extends IntTree



object TreeSum{
    // The sum of the leafs of t

    def worker(t: IntTree, parent: ![Int]): PROC = proc {
        t match {
            case Branch(left, right) => {
                val leftChan, rightChan = OneOneBuf[Int](1)
                run (worker(left, leftChan) || worker(right, rightChan))
                val result = leftChan?() + rightChan?()
                parent!(result)
            }
            case Leaf(n) => parent!(n)
        }
    }

    def apply(t: IntTree): Int = {
        val outputChan = OneOneBuf[Int](1)
        run(worker(t, outputChan))
        outputChan?()
    }

}


import scala.util.Random

object TreeSumTest{
  /** Produce a random tree.
    * @param w the reciprocal of the probability of producing a Leaf. */
  def makeTree(w: Int): IntTree = {
    if(Random.nextInt(w) == 0) return new Leaf(Random.nextInt(100))
    else return new Branch(makeTree(w-1), makeTree(w-1))
  }

  /** Sequential tree sum. */
  def treeSum(t: IntTree): Int = t match{
    case Leaf(n) => n
    case Branch(l, r) => treeSum(l) + treeSum(r)
  }

  /** A single test. */
  def doTest = {
    val t = makeTree(4); val seqSum = treeSum(t); val concSum = TreeSum(t)
    assert(seqSum == concSum)
  }

  def main(args : Array[String]) = {
    for(i <- 0 until 100000){
      doTest; if(i%1000 == 0) print(".")
    }
    println; io.threadcso.exit
  }
}