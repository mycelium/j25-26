package funsets

object Main extends App {
  import FunSets._
  
  val s1 = singletonSet(1)
  val s2 = singletonSet(2)
  val s3 = union(s1, s2)
  
  println("Testing basic operations:")
  println(contains(s1, 1)) 
  println(contains(s1, 2)) 
  println(contains(s3, 1)) 
  println(contains(s3, 2)) 
  
  val evenFilter = filter(s3, _ % 2 == 0)
  println(contains(evenFilter, 2)) 
  println(contains(evenFilter, 1)) 
  
  val doubled = map(s3, _ * 2)
  println(contains(doubled, 2)) 
  println(contains(doubled, 4)) 
  
  printSet(s3) 
}
