package funsets

object Main extends App {
  import FunSets._
  println(contains(singletonSet(1), 1))

  val s03 = singletonSet(3)
  val s04 = singletonSet(4)
  println("Тесты:")
  println("union:")
  val un01 = union(s03, s04)
  println("Объединение 3 и 4 проверка 2: " + contains(un01, 2));
  println("Объединение 3 и 4 проверка 3: " + contains(un01, 3));
  println("Объединение 3 и 4 проверка 4: " + contains(un01, 4));

  println("intersect:")
  val per01 = intersect(un01, s03)
  println("Пересечение {3,4} c {3} проверка 4: " + contains(per01, 4));
  println("Пересечение {3,4} c {3} проверка 3: " + contains(per01, 3));

  val paz01 = diff(un01, s03)
  println("diff:")
  println("Разность {3,4} c {3} проверка 4: " + contains(paz01, 4));
  println("Разность {3,4} c {3} проверка 3: " + contains(paz01, 3));

  println("filter:")

  val per1234 = union(union(singletonSet(1), singletonSet(2)), union(singletonSet(3), singletonSet(4)))
  val fil01 = filter(per1234, _ < 2)
  println("(Чисела < 2 из {1,2,3,4}) проверка 1: " + contains(fil01, 1))
  println("(Чисела < 2 из {1,2,3,4}) проверка 2: " + contains(fil01, 2))

  val sset = union(singletonSet(-1), per1234)
  val foeall01 = forall(sset, _ > 0)
  println("Все числа в {-1,1,2,3,4} положительные: " + foeall01)

  println("exists:")
  val ectOtr = exists(sset, _ < 0)
  println("В {-1,1,2,3,4} есть отрицательные: " + ectOtr)

  println("map:")
  val doubl = map(per1234, _ * 2)
  printSet(per1234)
  println("Удвоенные числа из {1,2,3,4} проверка 2: " + contains(doubl, 2))

  printSet(doubl)




}