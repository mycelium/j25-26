package funsets

object Main extends App {
  import FunSets._
  
  println("Проверка одиночных множеств")
  val single1 = singletonSet(1)
  val single2 = singletonSet(2) 
  val single3 = singletonSet(3)
  println(contains(single1, 1))
  println(contains(single2, 1))

  println("Операция объединение: ")
  val usinglea = union(single1, single2)
  printSet(usinglea)
  val usingleb = union(single1, single3)
  printSet(usingleb)

  println("Операция пересечения: ")
  val isingle = intersect(usinglea, usingleb)
  printSet(isingle)

  println("Операция разности: ")
  val dsingle = diff(usinglea, usingleb)
  printSet(dsingle)

  println("Фильтрация: ")
  val single4 = singletonSet(4)
  val single1234 = union(usinglea, union(single3, single4))
  val even = filter(single1234, x => x % 2 == 0)
  printSet(even)

  println("Проверка для всех элементов: ")
  println(forall(single1234, x => x > 0))
  println(forall(single1234, x => x < 2))

  println("Проверка существования элементов: ")
  println(exists(single1234, x => x < 0))
  println(exists(single1234, x => x > 2))

  println("Преобразование элементов: ")
  val squares = map(single1234, x => x * x)
  printSet(squares)


}
