package funsets

import org.scalatest.funsuite.AnyFunSuite

/*
Важные замечания:
- Тесты проверяют корректность функциональных множеств.
- Множества представлены как функции Int => Boolean.
- Для запуска: .\gradlew.bat test
*/

class FunSetsTest extends AnyFunSuite {

  import FunSets._

  // Category 1: Singleton Set

  test("Test 1.1: singletonSet contains its element") {
    val s = singletonSet(1)
    assert(contains(s, 1))
  }

  test("Test 1.2: singletonSet does not contain other elements") {
    val s = singletonSet(1)
    assert(!contains(s, 2))
    assert(!contains(s, 0))
    assert(!contains(s, -1))
  }

  test("Test 1.3: singletonSet with zero") {
    val s = singletonSet(0)
    assert(contains(s, 0))
    assert(!contains(s, 1))
  }

  test("Test 1.4: singletonSet with negative") {
    val s = singletonSet(-5)
    assert(contains(s, -5))
    assert(!contains(s, 5))
  }

  // Category 2: Union

  test("Test 2.1: union contains elements of both sets") {
    val s1 = singletonSet(1)
    val s2 = singletonSet(2)
    val u = union(s1, s2)
    assert(contains(u, 1))
    assert(contains(u, 2))
  }

  test("Test 2.2: union does not contain elements not in either set") {
    val s1 = singletonSet(1)
    val s2 = singletonSet(2)
    val u = union(s1, s2)
    assert(!contains(u, 3))
    assert(!contains(u, 0))
  }

  test("Test 2.3: union of same sets") {
    val s = singletonSet(1)
    val u = union(s, s)
    assert(contains(u, 1))
    assert(!contains(u, 2))
  }

  test("Test 2.4: union of multiple sets") {
    val s1 = singletonSet(1)
    val s2 = singletonSet(2)
    val s3 = singletonSet(3)
    val u = union(union(s1, s2), s3)
    assert(contains(u, 1))
    assert(contains(u, 2))
    assert(contains(u, 3))
  }

  // Category 3: Intersect

  test("Test 3.1: intersect of disjoint sets is empty") {
    val s1 = singletonSet(1)
    val s2 = singletonSet(2)
    val i = intersect(s1, s2)
    assert(!contains(i, 1))
    assert(!contains(i, 2))
  }

  test("Test 3.2: intersect of overlapping sets") {
    val s1 = union(singletonSet(1), singletonSet(2))
    val s2 = union(singletonSet(2), singletonSet(3))
    val i = intersect(s1, s2)
    assert(!contains(i, 1))
    assert(contains(i, 2))
    assert(!contains(i, 3))
  }

  test("Test 3.3: intersect with itself") {
    val s = union(singletonSet(1), singletonSet(2))
    val i = intersect(s, s)
    assert(contains(i, 1))
    assert(contains(i, 2))
  }

  test("Test 3.4: intersect of identical singletons") {
    val s1 = singletonSet(5)
    val s2 = singletonSet(5)
    val i = intersect(s1, s2)
    assert(contains(i, 5))
  }

  // Category 4: Diff

  test("Test 4.1: diff removes elements in second set") {
    val s1 = union(singletonSet(1), singletonSet(2))
    val s2 = singletonSet(2)
    val d = diff(s1, s2)
    assert(contains(d, 1))
    assert(!contains(d, 2))
  }

  test("Test 4.2: diff of disjoint sets") {
    val s1 = singletonSet(1)
    val s2 = singletonSet(2)
    val d = diff(s1, s2)
    assert(contains(d, 1))
    assert(!contains(d, 2))
  }

  test("Test 4.3: diff with itself is empty") {
    val s = union(singletonSet(1), singletonSet(2))
    val d = diff(s, s)
    assert(!contains(d, 1))
    assert(!contains(d, 2))
  }

  test("Test 4.4: diff with empty set") {
    val s1 = union(singletonSet(1), singletonSet(2))
    val s2 = singletonSet(999) // element not in s1
    val d = diff(s1, s2)
    assert(contains(d, 1))
    assert(contains(d, 2))
  }

  // Category 5: Filter

  test("Test 5.1: filter with always true predicate") {
    val s = union(singletonSet(1), singletonSet(2))
    val f = filter(s, _ => true)
    assert(contains(f, 1))
    assert(contains(f, 2))
  }

  test("Test 5.2: filter with always false predicate") {
    val s = union(singletonSet(1), singletonSet(2))
    val f = filter(s, _ => false)
    assert(!contains(f, 1))
    assert(!contains(f, 2))
  }

  test("Test 5.3: filter even numbers") {
    val s = union(union(singletonSet(1), singletonSet(2)), union(singletonSet(3), singletonSet(4)))
    val f = filter(s, x => x % 2 == 0)
    assert(!contains(f, 1))
    assert(contains(f, 2))
    assert(!contains(f, 3))
    assert(contains(f, 4))
  }

  test("Test 5.4: filter positive numbers") {
    val s = union(singletonSet(-1), union(singletonSet(0), singletonSet(1)))
    val f = filter(s, x => x > 0)
    assert(!contains(f, -1))
    assert(!contains(f, 0))
    assert(contains(f, 1))
  }

  // Category 6: Forall

  test("Test 6.1: forall on singleton - true case") {
    val s = singletonSet(2)
    assert(forall(s, x => x % 2 == 0))
  }

  test("Test 6.2: forall on singleton - false case") {
    val s = singletonSet(3)
    assert(!forall(s, x => x % 2 == 0))
  }

  test("Test 6.3: forall on set with all even numbers") {
    val s = union(singletonSet(2), union(singletonSet(4), singletonSet(6)))
    assert(forall(s, x => x % 2 == 0))
  }

  test("Test 6.4: forall fails when one element doesn't match") {
    val s = union(singletonSet(2), union(singletonSet(4), singletonSet(5)))
    assert(!forall(s, x => x % 2 == 0))
  }

  test("Test 6.5: forall with positive predicate") {
    val s = union(singletonSet(1), singletonSet(2))
    assert(forall(s, x => x > 0))
  }

  // Category 7: Exists

  test("Test 7.1: exists on singleton - true case") {
    val s = singletonSet(2)
    assert(exists(s, x => x == 2))
  }

  test("Test 7.2: exists on singleton - false case") {
    val s = singletonSet(2)
    assert(!exists(s, x => x == 3))
  }

  test("Test 7.3: exists finds even in mixed set") {
    val s = union(singletonSet(1), union(singletonSet(3), singletonSet(4)))
    assert(exists(s, x => x % 2 == 0))
  }

  test("Test 7.4: exists returns false when no match") {
    val s = union(singletonSet(1), union(singletonSet(3), singletonSet(5)))
    assert(!exists(s, x => x % 2 == 0))
  }

  test("Test 7.5: exists with negative predicate") {
    val s = union(singletonSet(-5), singletonSet(5))
    assert(exists(s, x => x < 0))
  }

  // Category 8: Map

  test("Test 8.1: map doubles elements") {
    val s = singletonSet(1)
    val m = map(s, x => x * 2)
    assert(!contains(m, 1))
    assert(contains(m, 2))
  }

  test("Test 8.2: map squares elements") {
    val s = union(singletonSet(2), singletonSet(3))
    val m = map(s, x => x * x)
    assert(contains(m, 4))
    assert(contains(m, 9))
    assert(!contains(m, 2))
    assert(!contains(m, 3))
  }

  test("Test 8.3: map with identity") {
    val s = union(singletonSet(1), singletonSet(2))
    val m = map(s, x => x)
    assert(contains(m, 1))
    assert(contains(m, 2))
  }

  test("Test 8.4: map negates elements") {
    val s = singletonSet(5)
    val m = map(s, x => -x)
    assert(!contains(m, 5))
    assert(contains(m, -5))
  }

  test("Test 8.5: map with constant function") {
    val s = union(singletonSet(1), singletonSet(2))
    val m = map(s, _ => 0)
    assert(contains(m, 0))
    assert(!contains(m, 1))
    assert(!contains(m, 2))
  }

  // Category 9: Contains

  test("Test 9.1: contains basic test") {
    val s = singletonSet(42)
    assert(contains(s, 42))
  }

  test("Test 9.2: contains returns false for missing element") {
    val s = singletonSet(42)
    assert(!contains(s, 0))
  }

  // Category 10: ToString

  test("Test 10.1: toString of singleton") {
    val s = singletonSet(1)
    val str = FunSets.toString(s)
    assert(str.contains("1"))
  }

  test("Test 10.2: toString of multiple elements") {
    val s = union(singletonSet(1), singletonSet(2))
    val str = FunSets.toString(s)
    assert(str.contains("1"))
    assert(str.contains("2"))
  }
}

