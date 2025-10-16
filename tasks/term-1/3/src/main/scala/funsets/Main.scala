package funsets

object Main extends App {
  import FunSets._
  println("Testing FunSets:")
  
  val single = singletonSet(1)
  println(s"contains(singletonSet(1), 1) = ${contains(single, 1)}") // true
  println(s"contains(singletonSet(1), 2) = ${contains(single, 2)}") // false
  
  val set1 = singletonSet(1)
  val set2 = singletonSet(2)
  val united = union(set1, set2)
  println(s"Union contains 1: ${contains(united, 1)}") // true
  println(s"Union contains 2: ${contains(united, 2)}") // true
  println(s"Union contains 3: ${contains(united, 3)}") // false
  
  val setA = union(singletonSet(1), singletonSet(2))
  val setB = union(singletonSet(2), singletonSet(3))
  val intersection = intersect(setA, setB)
  println(s"Intersection contains 2: ${contains(intersection, 2)}") // true
  
  val testSet = union(union(singletonSet(1), singletonSet(2)), singletonSet(3))
  print("Test set: ")
  printSet(testSet)
}
