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
  println(s"Intersection contains 1: ${contains(intersection, 1)}") // false
  println(s"Intersection contains 2: ${contains(intersection, 2)}") // true
  println(s"Intersection contains 3: ${contains(intersection, 3)}") // false
  
  val difference = diff(setA, setB)
  println(s"Difference contains 1: ${contains(difference, 1)}") // true
  println(s"Difference contains 2: ${contains(difference, 2)}") // false
  println(s"Difference contains 3: ${contains(difference, 3)}") // false
  
  val numbers = union(union(singletonSet(1), singletonSet(2)), union(singletonSet(3), singletonSet(4)))
  val evenNumbers = filter(numbers, x => x % 2 == 0)
  println(s"Filtered even contains 2: ${contains(evenNumbers, 2)}") // true
  println(s"Filtered even contains 4: ${contains(evenNumbers, 4)}") // true
  println(s"Filtered even contains 1: ${contains(evenNumbers, 1)}") // false
  println(s"Filtered even contains 3: ${contains(evenNumbers, 3)}") // false
  
  val smallSet = union(singletonSet(1), singletonSet(2))
  println(s"forall(smallSet, x => x > 0): ${forall(smallSet, x => x > 0)}") // true
  println(s"forall(smallSet, x => x < 2): ${forall(smallSet, x => x < 2)}") // false
  
  println(s"exists(smallSet, x => x == 2): ${exists(smallSet, x => x == 2)}") // true
  println(s"exists(smallSet, x => x == 3): ${exists(smallSet, x => x == 3)}") // false
  
  val originalSet = union(singletonSet(1), singletonSet(2))
  val mappedSet = map(originalSet, x => x * 2)
  println(s"Mapped set contains 2: ${contains(mappedSet, 2)}") // true
  println(s"Mapped set contains 4: ${contains(mappedSet, 4)}") // true
  println(s"Mapped set contains 1: ${contains(mappedSet, 1)}") // false
  
  val testSet = union(union(singletonSet(1), singletonSet(2)), singletonSet(3))
  print("Test set: ")
  printSet(testSet)
  
  
}