package funsets

object Main extends App {
  import FunSets._
  
  println("=== Testing Functional Sets ===")
  
  // Test 1: singletonSet and contains
  println("Test 1 - singletonSet:")
  val s1 = singletonSet(1)
  println(s"  singletonSet(1) contains 1: ${contains(s1, 1)}")
  println(s"  singletonSet(1) contains 2: ${contains(s1, 2)}")
  
  // Test 2: union
  println("\nTest 2 - union:")
  val s2 = singletonSet(2)
  val u = union(s1, s2)
  println(s"  union({1}, {2}) contains 1: ${contains(u, 1)}")
  println(s"  union({1}, {2}) contains 2: ${contains(u, 2)}")
  println(s"  union({1}, {2}) contains 3: ${contains(u, 3)}")
  
  // Test 3: intersect
  println("\nTest 3 - intersect:")
  val s3 = singletonSet(1)
  val i = intersect(u, s3)
  println(s"  intersect({1,2}, {1}) contains 1: ${contains(i, 1)}")
  println(s"  intersect({1,2}, {1}) contains 2: ${contains(i, 2)}")
  
  // Test 4: diff
  println("\nTest 4 - diff:")
  val d = diff(u, s1)
  println(s"  diff({1,2}, {1}) contains 1: ${contains(d, 1)}")
  println(s"  diff({1,2}, {1}) contains 2: ${contains(d, 2)}")
  
  // Test 5: filter
  println("\nTest 5 - filter:")
  val numbers = union(union(singletonSet(1), singletonSet(2)), singletonSet(3))
  val even = filter(numbers, x => x % 2 == 0)
  println(s"  filter({1,2,3}, even) contains 2: ${contains(even, 2)}")
  println(s"  filter({1,2,3}, even) contains 1: ${contains(even, 1)}")
  
  // Test 6: forall and exists
  println("\nTest 6 - forall/exists:")
  val smallSet = union(singletonSet(1), singletonSet(2))
  println(s"  forall({1,2}, x => x < 3): ${forall(smallSet, x => x < 3)}")
  println(s"  exists({1,2}, x => x == 2): ${exists(smallSet, x => x == 2)}")
  
  // Test 7: map
  println("\nTest 7 - map:")
  val doubled = map(smallSet, x => x * 2)
  println(s"  map({1,2}, x => x*2) contains 2: ${contains(doubled, 2)}")
  println(s"  map({1,2}, x => x*2) contains 4: ${contains(doubled, 4)}")
  
  // Test 8: printSet
  println("\nTest 8 - printSet:")
  print("  Set {1,2,3}: ")
  printSet(numbers)
  
  println("\n=== All tests completed! ===")
}