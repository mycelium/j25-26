package funsets

object Main extends App {
  import FunSets._

  println("\n1. singletonSet и contains")
  println("-" * 40)

  val single1 = singletonSet(1)
  val single5 = singletonSet(5)

  println(s"single1 содержит 1: ${contains(single1, 1)}") // true
  println(s"single1 содержит 2: ${contains(single1, 2)}") // false
  println(s"single5 содержит 5: ${contains(single5, 5)}") // true
  println(s"single5 содержит -5: ${contains(single5, -5)}") // false

  println("\n2. union")
  println("-" * 40)

  val single10 = singletonSet(10)
  val union1 = union(single1, single5)
  val union2 = union(union1, single10)

  println(s"union(single1, single5) содержит 1: ${contains(union1, 1)}") // true
  println(s"union(single1, single5) содержит 5: ${contains(union1, 5)}") // true
  println(s"union(single1, single5) содержит 10: ${contains(union1, 10)}") // false
  println(s"union(single1, single5, single10) содержит 1,5,10: ${contains(union2, 1) && contains(union2, 5) && contains(union2, 10)}") // true

  println("\n3. intersect")
  println("-" * 40)

  val setA = union(singletonSet(1), singletonSet(2))
  val setB = union(singletonSet(2), singletonSet(3))
  val intersection = intersect(setA, setB)

  println(s"Множество A = {1, 2}")
  println(s"Множество B = {2, 3}")
  println(s"intersect(A, B) содержит 1: ${contains(intersection, 1)}") // false
  println(s"intersect(A, B) содержит 2: ${contains(intersection, 2)}") // true
  println(s"intersect(A, B) содержит 3: ${contains(intersection, 3)}") // false

  println("\n4. diff")
  println("-" * 40)

  val difference = diff(setA, setB)

  println(s"diff(A, B) содержит 1: ${contains(difference, 1)}") // true (только в A)
  println(s"diff(A, B) содержит 2: ${contains(difference, 2)}") // false (в обоих)
  println(s"diff(A, B) содержит 3: ${contains(difference, 3)}") // false (только в B)

  println("\n5. filter")
  println("-" * 40)

  val numbers = union(union(singletonSet(1), singletonSet(2)), union(singletonSet(3), singletonSet(4)))
  val evenFilter = filter(numbers, x => x % 2 == 0)
  val greaterThan2 = filter(numbers, x => x > 2)

  println(s"Множество: {1, 2, 3, 4}")
  println(s"Чётные числа: содержит 1=${contains(evenFilter, 1)}, 2=${contains(evenFilter, 2)}, 3=${contains(evenFilter, 3)}, 4=${contains(evenFilter, 4)}")
  println(s"Числа > 2: содержит 1=${contains(greaterThan2, 1)}, 2=${contains(greaterThan2, 2)}, 3=${contains(greaterThan2, 3)}, 4=${contains(greaterThan2, 4)}")

  println("\n6. forall")
  println("-" * 40)

  val positiveSet = union(union(singletonSet(1), singletonSet(2)), singletonSet(3))
  val mixedSet = union(union(singletonSet(-1), singletonSet(2)), singletonSet(3))

  println(s"В {1, 2, 3} все положительные: ${forall(positiveSet, x => x > 0)}") // true
  println(s"В {1, 2, 3} все чётные: ${forall(positiveSet, x => x % 2 == 0)}") // false
  println(s"В {-1, 2, 3} все положительные: ${forall(mixedSet, x => x > 0)}") // false

  println("\n7. exists")
  println("-" * 40)

  println(s"В {1, 2, 3} существует чётное: ${exists(positiveSet, x => x % 2 == 0)}") // true
  println(s"В {1, 2, 3} существует отрицательное: ${exists(positiveSet, x => x < 0)}") // false
  println(s"В {-1, 2, 3} существует отрицательное: ${exists(mixedSet, x => x < 0)}") // true

  println("\n8. map")
  println("-" * 40)

  val smallSet = union(union(singletonSet(1), singletonSet(2)), singletonSet(3))
  val doubledSet = map(smallSet, x => x * 2)
  val squaredSet = map(smallSet, x => x * x)

  println(s"Исходное множество: {1, 2, 3}")
  println(s"map(x => x*2): содержит 2=${contains(doubledSet, 2)}, 4=${contains(doubledSet, 4)}, 6=${contains(doubledSet, 6)}")
  println(s"map(x => x*x): содержит 1=${contains(squaredSet, 1)}, 4=${contains(squaredSet, 4)}, 9=${contains(squaredSet, 9)}")

}
