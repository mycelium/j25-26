package funsets

object Main extends App {
  import FunSets._
  val s1 = singletonSet(1)
  val s2 = singletonSet(2)
  val s3 = singletonSet(3)
  println(contains(s1, 1))
  println(contains(s2, 1))

  println("Union: ")
  val s12 = union(s1, s2)
  printSet(s12)
  val s123 = union(s12, s3)
  printSet(s123)

  println("Intersection: ")
  val s13 = union(s1, s3)
  val inter = intersect(s12, s13)
  printSet(inter)

  println("Diff: ")
  val diffSet = diff(s12, s13)
  printSet(diffSet)

  println("Filter: ")
  val s4 = singletonSet(4)
  val num = union(union(s1, s2), union(s3, s4))
  val even = filter(num, x => x%2 ==0)
  printSet(even)

  println("Forall: ")
  printSet(num)
  println(forall(num, x => x>0)) 
  println(forall(num, x => x<3)) 

  println("Exists: ")
  println(exists(num, x => x<3))
  println(exists(num, x => x==5))  

  println("Map: ")
  printSet(s123)
  val doubled = map(s123, x => x * 2)   
  printSet(doubled)


}

