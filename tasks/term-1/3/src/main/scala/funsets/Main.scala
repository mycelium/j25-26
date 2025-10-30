package funsets

object Main extends App {
  import FunSets._
  
  val s1 = singletonSet(1)
  val s2 = singletonSet(2)
  println(contains(s1, 3))
  println(contains(s2, 2))
  
  val u = union(s1, s2)
  printSet(u)
  
  val i = intersect(u, s1)
  printSet(i)
  
  val d = diff(u, singletonSet(2))
  printSet(d)

  val s_base = union(singletonSet(5), singletonSet(10))
  val is_gt_7: Int => Boolean = x => x > 7
  val result = filter(s_base, is_gt_7) 
  printSet(result)
  
  val ot = union(s1, s2) 
  val positive: Int => Boolean = x => x > 0 
  val pgt1: Int => Boolean = x => x > 1 
  println(forall(ot, positive))
  println(forall(ot, pgt1))

  println(exists(ot, (x: Int) => x > 1))
  
  val f_plus_10: Int => Int = x => x + 10
  printSet(ot)
  printSet(map(ot, f_plus_10))
}
