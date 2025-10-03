package funsets

object Main extends App {
  import FunSets._
  println(s"Результат ${contains(singletonSet(1), 1)}")
}
