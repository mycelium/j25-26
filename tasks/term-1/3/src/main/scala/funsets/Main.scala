package funsets

object Main extends App {
  import FunSets._
  
  println("=== Functional Sets Laboratory ===")
  
  // Test basic sets
  val single = FunSets.singletonSet(5)
  println(s"Singleton set {5} contains 5: ${FunSets.contains(single, 5)}")
  println(s"Singleton set {5} contains 3: ${FunSets.contains(single, 3)}")
  
  // Test union
  val setA = FunSets.singletonSet(1)
  val setB = FunSets.singletonSet(2)
  val setC = FunSets.union(setA, setB)
  print("Union of {1} and {2}: ")
  FunSets.printSet(setC)
  
  // Test intersection
  val setD = FunSets.union(setC, FunSets.singletonSet(3)) // {1,2,3}
  val setE = FunSets.union(FunSets.singletonSet(2), FunSets.singletonSet(3)) // {2,3}
  val intersectionSet = FunSets.intersect(setD, setE)
  print("Intersection of {1,2,3} and {2,3}: ")
  FunSets.printSet(intersectionSet)
  
  // Test difference
  val differenceSet = FunSets.diff(setD, setE)
  print("Difference of {1,2,3} and {2,3}: ")
  FunSets.printSet(differenceSet)
  
  // Test filter
  val numbers = FunSets.union(
    FunSets.union(FunSets.singletonSet(1), FunSets.singletonSet(2)), 
    FunSets.union(FunSets.singletonSet(3), FunSets.singletonSet(4))
  )
  val evenNumbers = FunSets.filter(numbers, x => x % 2 == 0)
  print("Even numbers from {1,2,3,4}: ")
  FunSets.printSet(evenNumbers)
  
  // Test forall
  val positiveSet = FunSets.union(
    FunSets.union(FunSets.singletonSet(1), FunSets.singletonSet(2)), 
    FunSets.singletonSet(3)
  )
  println(s"All numbers in {1,2,3} are positive: ${FunSets.forall(positiveSet, x => x > 0)}")
  println(s"All numbers in {1,2,3} are greater than 2: ${FunSets.forall(positiveSet, x => x > 2)}")
  
  // Test exists
  val oddSet = FunSets.union(
    FunSets.union(FunSets.singletonSet(1), FunSets.singletonSet(3)), 
    FunSets.singletonSet(5)
  )
  println(s"Exists even number in {1,3,5}: ${FunSets.exists(oddSet, x => x % 2 == 0)}")
  println(s"Exists number greater than 2 in {1,2,3}: ${FunSets.exists(positiveSet, x => x > 2)}")
  
  // Test map
  val squaredSet = FunSets.map(numbers, x => x * x)
  print("Squares of {1,2,3,4}: ")
  FunSets.printSet(squaredSet)
  
  println("\n=== All tests completed ===")
}
