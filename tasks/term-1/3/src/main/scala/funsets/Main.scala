package funsets

object Main extends App {
  import FunSets._

  val s1 = singletonSet(1)
  val s2 = singletonSet(2)
  val s3 = singletonSet(3)

  val u = union(s1, s2)
  printSet(u)          // {1,2}

  val u2 = union(u, s3)
  printSet(u2)         // {1,2,3}

  val even = (x: Int) => x % 2 == 0
  val filtered = filter(u2, even)
  printSet(filtered)   // {2}

  println(forall(u2, x => x > 0)) // true
  println(exists(u2, x => x == 2)) // true

  val mapped = map(u2, x => x * 10)
  printSet(mapped)     // {10,20,30}
}
