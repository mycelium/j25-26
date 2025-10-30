package funsets

object Main extends App {
  import FunSets._

  // Создаем одноэлементные множества
  val s1 = singletonSet(1)
  val s2 = singletonSet(2)
  val s3 = singletonSet(3)

  println("s1 содержит 1: " + contains(s1, 1))  // true
  println("s1 содержит 2: " + contains(s1, 2))  // false

  // Объединение множеств
  val s123 = union(union(s1, s2), s3)
  println("Объединение {1}, {2}, {3}:")
  printSet(s123)  // {1,2,3}

  // Пересечение множеств
  val s12 = union(s1, s2)
  val s23 = union(s2, s3)
  val intersection = intersect(s12, s23)
  println("Пересечение {1,2} и {2,3}:")
  printSet(intersection)  // {2}

  // Разность множеств
  val difference = diff(s123, s2)
  println("Разность {1,2,3} и {2}:")
  printSet(difference)  // {1,3}

  // Фильтрация
  val evens = filter(s123, (x: Int) => x % 2 == 0)
  println("Четные числа из {1,2,3}:")
  printSet(evens)  // {2}

  // Множество всех четных чисел (в пределах bound)
  val allEvens: Set = (x: Int) => x % 2 == 0
  println("Все четные числа существуют: " +
    exists(allEvens, (x: Int) => x > 0))  // true

  // Проверка для всех элементов
  println("Все элементы {2,4,6} четные: " +
    forall(union(union(singletonSet(2), singletonSet(4)), singletonSet(6)),
      (x: Int) => x % 2 == 0))  // true

  // Отображение множества
  val doubled = map(s123, (x: Int) => x * 2)
  println("Удвоенные элементы {1,2,3}:")
  printSet(doubled)  // {2,4,6}
}