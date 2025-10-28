package funsets

object Main extends App {
  import FunSets._
  println("1 in [1]: " + contains(singletonSet(1), 1))
  println("2 in [1]: "+ contains(singletonSet(1), 2))
  val unionSet = union(singletonSet(1), singletonSet(2))
  println("2 in [1, 2]: "+ contains(unionSet, 2))
  println("3 in [1, 2]: " + contains(unionSet, 3))
  val intersectSet = intersect(unionSet, singletonSet(1))
  println("1 in [1, 2]and[1]: " + contains(intersectSet, 1))
  println("2 in [1, 2]and[1]: " +contains(intersectSet, 2))
  val set123 = union(union(singletonSet(1), singletonSet(2)), singletonSet(3))
  val evenSet = filter(set123, (x: Int) => x % 2 == 0)
  print("Четные числа из {1, 2, 3}:")
  printSet(evenSet)
  println("Все элементы положительные: " + forall(set123, (x: Int) => x > 0))
  println("Существует четный элемент: " + exists(set123, (x: Int) => x % 2 == 0))
  val squaredSet = map(set123, (x: Int) => x * x)
  print("Квадраты элементов {1, 2, 3}: ")
  printSet(squaredSet)
}