package funsets

object Main extends App {
  import FunSets._ 

  // создаём одиночные множества
  val s1 = singletonSet(1)   // множество {1}
  val s2 = singletonSet(2)   // множество {2}
  val s3 = singletonSet(3)   // множество {3}

  println("Does s1 contain 1? " + contains(s1, 1))
  println("Does s1 contain 2? " + contains(s1, 2))
  println()

  //  объединение 
  val s12 = union(s1, s2)    // множество {1, 2}
  print("Union of {1} and {2}: ")
  printSet(s12)              // → {1,2}

  // пересечение
  val s123 = union(s12, s3)  // {1,2,3}
  val inter = intersect(s12, s123) // пересечение {1,2} c {1,2,3} = {1,2}
  print("Intersection of {1,2} and {1,2,3}: ")
  printSet(inter)

  //  разность
  val diffSet = diff(s123, s1) // {2,3}
  print("Diff {1,2,3} \\ {1}: ")
  printSet(diffSet)

  // фильтрация - только чётные
  val even = filter(s123, x => x % 2 == 0) 
 print("Even elems in {1,2,3}: ")
  printSet(even)

  // проверка forall (все ли элементы множества удовлетворяют условию)
  println("All elems {1,2,3} > 0 ? " + forall(s123, x => x > 0))
  println("All elems of {1,2,3} < 3 ? " + forall(s123, x => x < 3))
  println()

  // проверка exists (существует ли элемент, удовлетворяющий условию)
  println("Exists 2 in {1,2,3}? " + exists(s123, x => x == 2))
  println("Exists 5 in {1,2,3}? " + exists(s123, x => x == 5))
  println()

  // map — преобразование элементов множества
  val mapped = map(s123, x => x * x) // square each element
  print("Squares of {1,2,3}: ")
  printSet(mapped)
}
