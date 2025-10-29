package funsets

object FunSets {

  type Set = Int => Boolean
  
  def contains(s: Set, elem: Int): Boolean = s(elem)
  
  def singletonSet(elem: Int): Set =  (value: Int) => elem == value
  
  def union(s: Set, t: Set): Set = (value: Int) => s(value) || t(value)
  
  def intersect(s: Set, t: Set): Set = (value: Int) => s(value) && t(value)
  
  def diff(s: Set, t: Set): Set = (value: Int) => s(value) && !t(value)
  
  def filter(s: Set, p: Int => Boolean): Set = (value: Int) => p(value) && s(value)
  
  val bound = 1000
  
  def forall(s: Set, p: Int => Boolean): Boolean = {
    def iter(a: Int): Boolean = {
      if (a > bound)     true
      else if (s(a) && !p(a)) false
      else iter(a + 1)
    }
    iter(-bound)
  }
  
  def exists(s: Set, p: Int => Boolean): Boolean = {
    !forall(s,(value: Int) => !p(value))
  }
  
  def map(s: Set, f: Int => Int): Set = (value1: Int) =>
    exists(s, (value2: Int) => f(value2) == value1)
  
  def toString(s: Set): String = {
    val xs = for (i <- -bound to bound if contains(s, i)) yield i
    xs.mkString("{", ",", "}")
  }
  
  def printSet(s: Set): Unit = {
    println(toString(s))
  }
}
