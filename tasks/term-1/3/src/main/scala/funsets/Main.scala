package funsets

object Main extends App {
  import FunSets._

  val s1 = singletonSet(1)
  val s2 = singletonSet(2)
  val s3 = singletonSet(3)

  val u = union(s1, s2)
  val i = intersect(u, s2)
  val d = diff(u, s2)
  val f = filter(u, x => x % 2 == 0)
  val m = map(u, x => x * x)

  println("Singleton 1 contains 1? " + contains(s1, 1))
  println("Union of 1 and 2: "); printSet(u)
  println("Intersection with 2: "); printSet(i)
  println("Difference with 2: "); printSet(d)
  println("Filter even: "); printSet(f)
  println("Map x*x: "); printSet(m)
  println("Exists > 1 in union? " + exists(u, _ > 1))
  println("Forall < 3 in union? " + forall(u, _ < 3))
}
