package funsets

object Main extends App {
  import FunSets._
  println(contains(singletonSet(1), 1))
  println(contains(union(singletonSet(1), singletonSet(2)), 1))
  println(contains(union(singletonSet(1), singletonSet(2)), 2))
  println(contains(union(singletonSet(1), singletonSet(2)), 3))

  var test = singletonSet(1)
  test = union(test, singletonSet(2))
  println(contains(test, 1))
  println(contains(test, 2))
  println(contains(test, 3))
  test = union(test, singletonSet(3))
  test = union(test, singletonSet(4))

  println("Test 2")
  var test2 = (x: Int) => x >= 1 && x <= 5
  var test3 = map(test2, ((x: Int) => x*2))
  println("1: "+contains(test3, 1)) // false no items *2 = 1
  println("2: "+contains(test3, 2)) // true 1*2 = 2
  println("3: "+contains(test3, 3)) // false
  println("4: "+contains(test3, 4)) // true 2*2 = 4
  println("5: "+contains(test3, 5)) // false
  println("6: "+contains(test3, 6)) // true
  println("7: "+contains(test3, 7)) // false
  println("8: "+contains(test3, 8)) // true
  println("9: "+contains(test3, 9)) // false
  println("10: "+contains(test3, 10))//true
  println("12: "+contains(test3, 12))//false



}
