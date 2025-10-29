package funsets

object Main extends App {
  import FunSets._
  println(contains(singletonSet(1), 1))

  val s1 = singletonSet(1)
  val s2 = singletonSet(2)
  val u = union(s1, s2)

  println(contains(u, 1))
  println(contains(u, 2))
  println(contains(u, 3))

  val i = intersect(u, s1)
  println(contains(i, 1))
  println(contains(i, 2))

  val evenSet: Set = x => x % 2 == 0
  println(forall(evenSet, x => x % 2 == 0))

  val smallSet = union(singletonSet(2), singletonSet(4))
  println(forall(smallSet, x => x % 2 == 0))

  println(exists(smallSet, x => x == 2))
}