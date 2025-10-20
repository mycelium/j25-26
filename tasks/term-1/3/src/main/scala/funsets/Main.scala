package funsets

object Main extends App {
  import FunSets._

  val s1 = singletonSet(1)
  val s2 = singletonSet(2)
  val s3 = singletonSet(3)
  println(contains(s1, 1))
  println(contains(s2, 1))

  println("Union: ")
  val usa = union(s1, s2)
  printSet(usa)
  val usb = union(s1, s3)
  printSet(usb)

  println("Intersection: ")
  val is = intersect(usa, usb)
  printSet(is)

  println("Diff: ")
  val ds = diff(usa, usb)
  printSet(ds)

  println("Filter: ")
  val s4 = singletonSet(4)
  val s1234 = union(usa, union(s3, s4))
  val even = filter(s1234, x => x % 2 == 0)
  printSet(even)

  println("Forall: ")
  println(forall(s1234, x => x > 0))
  println(forall(s1234, x => x < 2))

  println("Exists: ")
  println(exists(s1234, x => x < 0))
  println(exists(s1234, x => x > 2))

  println("Map: ")
  val squares = map(s1234, x => x * x)
  printSet(squares)


}
