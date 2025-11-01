package funsets

object Main extends App {
  import FunSets._
  println(contains(singletonSet(1), 1))

  val s1 = singletonSet(1)
  val s2 = singletonSet(2)
  val u = union(s1, s2)

  println(contains(u, 1))
  println(contains(u, 3))

  val mapped = map(u, _ * 2)
  println(contains(mapped, 2))
  println(contains(mapped, 4))
}
