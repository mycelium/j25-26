package funsets

import common._

object FunSets {

  /** Представляем множество через характеристическую функцию */
  type Set = Int => Boolean

  /** Проверяет, содержится ли элемент в множестве */
  def contains(s: Set, elem: Int): Boolean = s(elem)

  /** Множество из одного элемента */
  def singletonSet(elem: Int): Set = (x: Int) => x == elem

  /** Объединение двух множеств */
  def union(s: Set, t: Set): Set = (x: Int) => s(x) || t(x)

  /** Пересечение двух множеств */
  def intersect(s: Set, t: Set): Set = (x: Int) => s(x) && t(x)

  /** Разность множеств (элементы s, не принадлежащие t) */
  def diff(s: Set, t: Set): Set = (x: Int) => s(x) && !t(x)

  /** Подмножество элементов s, удовлетворяющих предикату p */
  def filter(s: Set, p: Int => Boolean): Set = (x: Int) => s(x) && p(x)

  /** Границы диапазона для кванторов */
  val bound = 1000

  /** Проверка, что все элементы множества удовлетворяют p */
  def forall(s: Set, p: Int => Boolean): Boolean = {
    def iter(a: Int): Boolean = {
      if (a > bound) true
      else if (s(a) && !p(a)) false
      else iter(a + 1)
    }
    iter(-bound)
  }

  /** Проверка, что существует элемент множества, удовлетворяющий p */
  def exists(s: Set, p: Int => Boolean): Boolean = {
    !forall(s, (x: Int) => !p(x))
  }

  /** Преобразование множества функцией f */
  def map(s: Set, f: Int => Int): Set = {
    (y: Int) => exists(s, (x: Int) => f(x) == y)
  }

  /** Печать множества */
  def toString(s: Set): String = {
    val xs = for (i <- -bound to bound if contains(s, i)) yield i
    xs.mkString("{", ",", "}")
  }

  def printSet(s: Set): Unit = println(toString(s))
}
