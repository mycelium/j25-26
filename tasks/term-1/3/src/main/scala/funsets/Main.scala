package funsets

object Main extends App {
  import FunSets._

  val s1 = singletonSet(10)
  val s2 = singletonSet(25)
  val s3 = singletonSet(40)

  val u = union(s1, s2)
  val i = intersect(u, s2)
  val d = diff(u, s2)
  val f = filter(u, x => x % 2 == 0)
  val m = map(u, x => x * x)

  println("Singleton 10 contains 10? " + contains(s1, 10))
  println("Union of 10 and 25: "); printSet(u)
  println("Intersection with 25: "); printSet(i)
  println("Difference with 25: "); printSet(d)
  println("Filter even: "); printSet(f)
  println("Map x*x: "); printSet(m)
  println("Exists > 20 in union? " + exists(u, _ > 20))
  println("Forall < 30 in union? " + forall(u, _ < 30))
}