package funsets


object Main extends App {
  import FunSets._

  println("\n1. Testing singletonSet:")
  val s1 = singletonSet(1)
  printSet(s1)
  println(contains(s1, 1)) // true
  println(contains(s1, 2)) // false


  println("\n2. Testing union:")
  val s2 = singletonSet(2)
  val s3 = union(s1, s2)
  printSet(s3)
  
 
  println("\n3. Testing intersect:")
  val s4 = singletonSet(1)
  val s5 = union(singletonSet(1), singletonSet(3))
  val s6 = intersect(s4, s5)
  printSet(s6)
  
 
  println("\n4. Testing diff:")
  val s7 = diff(s5, s4) 
  printSet(s7)
  
  
  println("\n5. Testing forall and exists:")
  val positiveSet = union(singletonSet(1), singletonSet(2))
  println(forall(positiveSet, _ > 0)) // true
  println(exists(positiveSet, _ == 2)) // true
  
 
  println("\n6. Testing map:")
  val squaredSet = map(positiveSet, x => x * x)
  printSet(squaredSet)

 
  

}