package funsets

object Main extends App {
  import FunSets._
  
  val set5 = singletonSet(5)
  print("Set {5}: ")
  printSet(set5)
  println()
  val set1 = singletonSet(1)
  val set2 = singletonSet(2)
  val union12 = union(set1, set2)
  println("union12 = {1} union {2} =")
  printSet(union12)
  
  println()
  val setA = union(singletonSet(1), singletonSet(2))
  val setB = union(singletonSet(2), singletonSet(3))
  val intersection = intersect(setA, setB)
  println("A = ")
  printSet(setA)
  println("B = ")
  printSet(setB)
  println("A intersects B = ")
  printSet(intersection)
  
  println()
  val diffAB = diff(setA, setB)
  println("A \\ B = ")
  printSet(diffAB)
  
  println()
  val evenFilter = filter(setA, (x: Int) => x % 2 == 0)
  println("Filter even numbers from A = {1,2} = ")
  printSet(evenFilter)
  
  println()
  val allEven = forall(setA, (x: Int) => x % 2 == 0)
  println("(forall) All elements of A are even: " + allEven)
  
  println()
  val hasEven = exists(setA, (x: Int) => x % 2 == 0)
  println("(exists) A has even numbers: " + hasEven)
  

  println()
  val doubled = map(setA, (x: Int) => x * 2)
  println("A = ")
  printSet(setA)
  println("A * 2 (map) = ")
  printSet(doubled)

}