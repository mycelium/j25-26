package funsets

object Main extends App {
  import FunSets._
  println(contains(singletonSet(1), 1))

  val A = singletonSet(42)
  val B = singletonSet(24)
  val C = union(A, B)

  println("A содержит 42: " + contains(A, 42))
  println("B содержит 24: " + contains(B, 24))
  println("C содержит 42: " + contains(C, 42))
  println("C содержит 24: " + contains(C, 24))
  println("C содержит 25: " + contains(C, 25))

  val D = intersect(A, B)
  print("D: ")
  printSet(D)

  val E = intersect(A, C)
  print("E: ")
  printSet(E)

  val F = union(C, singletonSet(5))
  val G = diff(F, C)
  print("G: ")
  printSet(G)

  val filtered = filter(F, (x: Int) => x % 2 == 0)
  print("filtered: ")
  printSet(filtered)

  val allEven = forall(F, (x: Int) => x % 2 == 0)
  println("Все элементы F четные: " + allEven)

  val existsEven = exists(F, (x: Int) => x % 2 == 0)
  println("В F есть четные элементы: " + existsEven)

  val mapped = map(F, (x: Int) => x * 2)
  print("mapped: ")
  printSet(mapped)
}
