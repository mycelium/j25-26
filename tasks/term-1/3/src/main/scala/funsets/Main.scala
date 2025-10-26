package funsets

object Main extends App {
  import FunSets._

  val setA = singletonSet(10)
  val setB = singletonSet(20)
  val setC = union(setA, setB)


  println(contains(setA, 10))
  println(contains(setA, 20))
  println(contains(setC, 30))

  val setD = union(setC, singletonSet(30))
  printSet(setD)


  val commonElements = intersect(setD, setC)
  val uniqueElements = diff(setD, setC)

  print("Общие элементы множеств {10,20,30} и {10,20}: ")
  printSet(commonElements)

  print("Уникальные элементы {10,20,30} которых нет в {10,20}: ")
  printSet(uniqueElements)

  val divisibleBy10 = filter(setD, (x: Int) => x % 10 == 0) // {10,20,30}
  print("Числа кратные 10 в множестве {10,20,30}: ")
  printSet(divisibleBy10)

  println("Все ли числа в {5,15,25} делятся на 5: " +
    forall(union(union(singletonSet(5), singletonSet(15)), singletonSet(25)),
      (x: Int) => x % 5 == 0))

  println("Есть ли числа больше 100 в {50,75,90}: " +
    exists(union(union(singletonSet(50), singletonSet(75)), singletonSet(90)),
      (x: Int) => x > 100))

  val incremented = map(setD, (x: Int) => x + 5) // {15,25,35}
  print("Числа увеличенные на 5 из множества {10,20,30}: ")
  printSet(incremented)

}