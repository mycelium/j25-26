package funsets

object Main extends App {
  import FunSets._
  
  val set1 = singletonSet(1)
  val set2 = singletonSet(2)
  val set3 = union(set1, set2)
  
  println("Содержит ли {1} число 1: " + contains(set1, 1))
  println("Содержит ли {1} число 2: " + contains(set1, 2))
  println("Содержит ли {1,2} число 2: " + contains(set3, 2))
 
  val set4 = union(singletonSet(3), set3) // {1,2,3}
  val set5 = union(singletonSet(2), singletonSet(3)) // {2,3}
  
  val intersection = intersect(set4, set5) // {2,3}
  val difference = diff(set4, set5) // {1}
  
  print("Пересечение {1,2,3} и {2,3}: ")
  printSet(intersection)
  
  print("Разность {1,2,3} и {2,3}: ")
  printSet(difference)
  
  val evenFilter = filter(set4, (x: Int) => x % 2 == 0) // {2}
  print("Четные числа в {1,2,3}: ")
  printSet(evenFilter)
  
  println("Все ли числа в {2,4,6} четные: " + 
    forall(union(union(singletonSet(2), singletonSet(4)), singletonSet(6)), 
           (x: Int) => x % 2 == 0))
  
  println("Существует ли четное число в {1,3,5}: " + 
    exists(union(union(singletonSet(1), singletonSet(3)), singletonSet(5)),
           (x: Int) => x % 2 == 0))
  
  val doubled = map(set4, (x: Int) => x * 2) // {2,4,6}
  print("Удвоенные значения {1,2,3}: ")
  printSet(doubled)
  
}
