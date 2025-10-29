package funsets

object Main extends App {
  import FunSets._

  println(s"singletonSet(1) contains 1 : ${contains(singletonSet(1), 1)}")

  val ss1 = singletonSet(1)
  val ss2 = singletonSet(2)
  val ss3 = singletonSet(3)

  // union, intersect, diff
  val stUnion  = union    (ss1,     ss2)
  val stInter0 = intersect(ss1,     ss2)
  val stInter1 = intersect(ss1,     stUnion)
  val stDiff   = diff     (stUnion, union(ss2, ss3))

  println(s"${FunSets.toString(ss1)} union ${FunSets.toString(ss2)} = ${FunSets.toString(stUnion)}")
  println(s"${FunSets.toString(ss1)} intersect ${FunSets.toString(ss2)} = ${FunSets.toString(stInter0)}")
  println(s"${FunSets.toString(ss1)} intersect ${FunSets.toString(stUnion)} = ${FunSets.toString(stInter1)}")
  println(s"${FunSets.toString(stUnion)} diff ${FunSets.toString(union(ss2, ss3))} = ${FunSets.toString(stDiff)}")

  // filter
  val st = union(stUnion, ss3)
  val stHigherThanOne = filter(st, (value: Int) => value > 1)
  println(s"${FunSets.toString(st)} filtered by (value: Int) => value > 1 is ${FunSets.toString(stHigherThanOne)}")

  // forall
  println(s"Is all elements in ${FunSets.toString(st)} even = ${forall(st, (value: Int) => value % 2 == 0)}")
  println(s"Is all elements in ${FunSets.toString(st)} bigger than 0 = ${forall(st, (value: Int) => value > 0)}")

  // exists
  println(s"Does even element exist in ${FunSets.toString(st)} = ${exists(st, (value: Int) => value % 2 == 0)}")
  println(s"Does element bigger than 4 exist in ${FunSets.toString(st)} = ${exists(st, (value: Int) => value > 4)}")

  // map
  println(s"Each element in ${FunSets.toString(st)} multipled by 5 : ${FunSets.toString((map(st, (value: Int) => value * 5)))}")
}
