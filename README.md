# ConcurrentProgrammingProblems4
University concurrent programming problems


Questions:

Question 2
[Programming] Recall the following synchronisation problem from an earlier sheet. There are two types of client process, which we shall call men and women. These processes need to pair off for some purpose, with each pair containing one process of each type. Design a monitor to support this, with public methods
def manSync(me: String): String = ... def womanSync(me: String): String = ...
Each process should pass its name to the monitor, and receive back the name of its partner. Test your code.
Question 3
[Programming] Consider a binary tree containing integers, defined as follows:
abstract class IntTree
case class Leaf(n: Int) extends IntTree
case class Branch(l: IntTree, r: IntTree) extends IntTree
Write a concurrent program to calculate the sum of the values on the leaves of the tree, using the recursive parallel pattern, with message passing. Give you program signature
1
object TreeSum{
/∗∗ The sum of the leafs of t. ∗/ def apply(t: IntTree): Int = ...
}
Hint: provide each recursive thread with a channel with which to send its result to its parent thread. Test your code using the testing harness on the course webpage.
Question 5
[Programming] Recall the following synchronisation problem from the previous problem sheet. Each process has an integer-valued identity. A particular resource should be accessed according to the following constraint: a new process can start using the resource only if the sum of the identities of those processes currently using it is divisible by 3. Implement a class to enforce this, with operations enter(id: Int) and exit(id: Int). Your class should use semaphores.
Test your code.
Question 6
[Programming] Implement a bounded buffer, of size n, using two counting semaphores: one semaphore should prevent data from being added to the buffer when it is full; the other should prevent data from being removed from the buffer when it is empty.
Test your implementation using the linearizability framework.
Question 7
[Programming] Recall the men-women problem from question 2. Implement a class to
solve this problem, using semaphores internally. Test your code.
