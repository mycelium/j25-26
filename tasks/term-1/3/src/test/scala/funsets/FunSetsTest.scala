package funsets

import org.scalatest.funsuite.AnyFunSuite

/*
Важные замечания:
- Тесты проверяют корректность функциональных множеств.
- Множества представлены как функции Int => Boolean.
- Для запуска: sbt test
*/

class FunSetsTest extends AnyFunSuite {

  import FunSets._

  // Category 1: Singleton Set

  private def assertContains(s: FunSets.Set, elem: Int, setDesc: String, expected: Boolean): Unit = {
    val result = contains(s, elem)
    assert(result === expected,
      s"contains($setDesc, $elem) should be $expected, but got $result")
  }

  test("Test 1.1: singletonSet contains its element") {
    val s = singletonSet(1)
    assertContains(s, 1, "singletonSet(1)", true)
  }

  test("Test 1.2: singletonSet does not contain other elements") {
    val s = singletonSet(1)
    for (elem <- List(2, 0, -1)) {
      assertContains(s, elem, "singletonSet(1)", false)
    }
  }

  test("Test 1.3: singletonSet with zero") {
    val s = singletonSet(0)
    assertContains(s, 0, "singletonSet(0)", true)
    assertContains(s, 1, "singletonSet(0)", false)
  }

  test("Test 1.4: singletonSet with negative") {
    val s = singletonSet(-5)
    assertContains(s, -5, "singletonSet(-5)", true)
    assertContains(s, 5, "singletonSet(-5)", false)
  }

  // Category 2: Union

  test("Test 2.1: union contains elements of both sets") {
    val s1 = singletonSet(1)
    val s2 = singletonSet(2)
    val u = union(s1, s2)
    assertContains(u, 1, "union({1}, {2})", true)
    assertContains(u, 2, "union({1}, {2})", true)
  }

  test("Test 2.2: union does not contain elements not in either set") {
    val s1 = singletonSet(1)
    val s2 = singletonSet(2)
    val u = union(s1, s2)
    assertContains(u, 3, "union({1}, {2})", false)
    assertContains(u, 0, "union({1}, {2})", false)
  }

  test("Test 2.3: union of same sets") {
    val s = singletonSet(1)
    val u = union(s, s)
    assertContains(u, 1, "union({1}, {1})", true)
    assertContains(u, 2, "union({1}, {1})", false)
  }

  test("Test 2.4: union of multiple sets") {
    val s1 = singletonSet(1)
    val s2 = singletonSet(2)
    val s3 = singletonSet(3)
    val u = union(union(s1, s2), s3)
    for (elem <- List(1, 2, 3)) {
      assertContains(u, elem, "union({1}, {2}, {3})", true)
    }
  }

  // Category 3: Intersect

  test("Test 3.1: intersect of disjoint sets is empty") {
    val s1 = singletonSet(1)
    val s2 = singletonSet(2)
    val i = intersect(s1, s2)
    assertContains(i, 1, "intersect({1}, {2})", false)
    assertContains(i, 2, "intersect({1}, {2})", false)
  }

  test("Test 3.2: intersect of overlapping sets") {
    val s1 = union(singletonSet(1), singletonSet(2))
    val s2 = union(singletonSet(2), singletonSet(3))
    val i = intersect(s1, s2)
    assertContains(i, 1, "intersect({1,2}, {2,3})", false)
    assertContains(i, 2, "intersect({1,2}, {2,3})", true)
    assertContains(i, 3, "intersect({1,2}, {2,3})", false)
  }

  test("Test 3.3: intersect with itself") {
    val s = union(singletonSet(1), singletonSet(2))
    val i = intersect(s, s)
    assertContains(i, 1, "intersect({1,2}, {1,2})", true)
    assertContains(i, 2, "intersect({1,2}, {1,2})", true)
  }

  test("Test 3.4: intersect of identical singletons") {
    val s1 = singletonSet(5)
    val s2 = singletonSet(5)
    val i = intersect(s1, s2)
    assertContains(i, 5, "intersect({5}, {5})", true)
  }

  // Category 4: Diff

  test("Test 4.1: diff removes elements in second set") {
    val s1 = union(singletonSet(1), singletonSet(2))
    val s2 = singletonSet(2)
    val d = diff(s1, s2)
    assertContains(d, 1, "diff({1,2}, {2})", true)
    assertContains(d, 2, "diff({1,2}, {2})", false)
  }

  test("Test 4.2: diff of disjoint sets") {
    val s1 = singletonSet(1)
    val s2 = singletonSet(2)
    val d = diff(s1, s2)
    assertContains(d, 1, "diff({1}, {2})", true)
    assertContains(d, 2, "diff({1}, {2})", false)
  }

  test("Test 4.3: diff with itself is empty") {
    val s = union(singletonSet(1), singletonSet(2))
    val d = diff(s, s)
    assertContains(d, 1, "diff({1,2}, {1,2})", false)
    assertContains(d, 2, "diff({1,2}, {1,2})", false)
  }

  test("Test 4.4: diff with empty set") {
    val s1 = union(singletonSet(1), singletonSet(2))
    val s2 = singletonSet(999) // element not in s1
    val d = diff(s1, s2)
    assertContains(d, 1, "diff({1,2}, {999})", true)
    assertContains(d, 2, "diff({1,2}, {999})", true)
  }

  // Category 5: Filter

  test("Test 5.1: filter with always true predicate") {
    val s = union(singletonSet(1), singletonSet(2))
    val f = filter(s, _ => true)
    assertContains(f, 1, "filter({1,2}, _ => true)", true)
    assertContains(f, 2, "filter({1,2}, _ => true)", true)
  }

  test("Test 5.2: filter with always false predicate") {
    val s = union(singletonSet(1), singletonSet(2))
    val f = filter(s, _ => false)
    assertContains(f, 1, "filter({1,2}, _ => false)", false)
    assertContains(f, 2, "filter({1,2}, _ => false)", false)
  }

  test("Test 5.3: filter even numbers") {
    val s = union(union(singletonSet(1), singletonSet(2)), union(singletonSet(3), singletonSet(4)))
    val f = filter(s, x => x % 2 == 0)
    assertContains(f, 1, "filter({1,2,3,4}, isEven)", false)
    assertContains(f, 2, "filter({1,2,3,4}, isEven)", true)
    assertContains(f, 3, "filter({1,2,3,4}, isEven)", false)
    assertContains(f, 4, "filter({1,2,3,4}, isEven)", true)
  }

  test("Test 5.4: filter positive numbers") {
    val s = union(singletonSet(-1), union(singletonSet(0), singletonSet(1)))
    val f = filter(s, x => x > 0)
    assertContains(f, -1, "filter({-1,0,1}, x > 0)", false)
    assertContains(f, 0, "filter({-1,0,1}, x > 0)", false)
    assertContains(f, 1, "filter({-1,0,1}, x > 0)", true)
  }

  // Category 6: Forall

  private def assertForall(s: FunSets.Set, p: Int => Boolean, setDesc: String, predDesc: String, expected: Boolean): Unit = {
    val result = forall(s, p)
    assert(result === expected,
      s"forall($setDesc, $predDesc) should be $expected, but got $result")
  }

  test("Test 6.1: forall on singleton - true case") {
    val s = singletonSet(2)
    assertForall(s, x => x % 2 == 0, "{2}", "isEven", true)
  }

  test("Test 6.2: forall on singleton - false case") {
    val s = singletonSet(3)
    assertForall(s, x => x % 2 == 0, "{3}", "isEven", false)
  }

  test("Test 6.3: forall on set with all even numbers") {
    val s = union(singletonSet(2), union(singletonSet(4), singletonSet(6)))
    assertForall(s, x => x % 2 == 0, "{2,4,6}", "isEven", true)
  }

  test("Test 6.4: forall fails when one element doesn't match") {
    val s = union(singletonSet(2), union(singletonSet(4), singletonSet(5)))
    assertForall(s, x => x % 2 == 0, "{2,4,5}", "isEven", false)
  }

  test("Test 6.5: forall with positive predicate") {
    val s = union(singletonSet(1), singletonSet(2))
    assertForall(s, x => x > 0, "{1,2}", "x > 0", true)
  }

  // Category 7: Exists

  private def assertExists(s: FunSets.Set, p: Int => Boolean, setDesc: String, predDesc: String, expected: Boolean): Unit = {
    val result = exists(s, p)
    assert(result === expected,
      s"exists($setDesc, $predDesc) should be $expected, but got $result")
  }

  test("Test 7.1: exists on singleton - true case") {
    val s = singletonSet(2)
    assertExists(s, x => x == 2, "{2}", "x == 2", true)
  }

  test("Test 7.2: exists on singleton - false case") {
    val s = singletonSet(2)
    assertExists(s, x => x == 3, "{2}", "x == 3", false)
  }

  test("Test 7.3: exists finds even in mixed set") {
    val s = union(singletonSet(1), union(singletonSet(3), singletonSet(4)))
    assertExists(s, x => x % 2 == 0, "{1,3,4}", "isEven", true)
  }

  test("Test 7.4: exists returns false when no match") {
    val s = union(singletonSet(1), union(singletonSet(3), singletonSet(5)))
    assertExists(s, x => x % 2 == 0, "{1,3,5}", "isEven", false)
  }

  test("Test 7.5: exists with negative predicate") {
    val s = union(singletonSet(-5), singletonSet(5))
    assertExists(s, x => x < 0, "{-5,5}", "x < 0", true)
  }

  // Category 8: Map

  test("Test 8.1: map doubles elements") {
    val s = singletonSet(1)
    val m = map(s, x => x * 2)
    assertContains(m, 1, "map({1}, x => x*2)", false)
    assertContains(m, 2, "map({1}, x => x*2)", true)
  }

  test("Test 8.2: map squares elements") {
    val s = union(singletonSet(2), singletonSet(3))
    val m = map(s, x => x * x)
    assertContains(m, 4, "map({2,3}, x => x*x)", true)
    assertContains(m, 9, "map({2,3}, x => x*x)", true)
    assertContains(m, 2, "map({2,3}, x => x*x)", false)
    assertContains(m, 3, "map({2,3}, x => x*x)", false)
  }

  test("Test 8.3: map with identity") {
    val s = union(singletonSet(1), singletonSet(2))
    val m = map(s, x => x)
    assertContains(m, 1, "map({1,2}, identity)", true)
    assertContains(m, 2, "map({1,2}, identity)", true)
  }

  test("Test 8.4: map negates elements") {
    val s = singletonSet(5)
    val m = map(s, x => -x)
    assertContains(m, 5, "map({5}, x => -x)", false)
    assertContains(m, -5, "map({5}, x => -x)", true)
  }

  test("Test 8.5: map with constant function") {
    val s = union(singletonSet(1), singletonSet(2))
    val m = map(s, _ => 0)
    assertContains(m, 0, "map({1,2}, _ => 0)", true)
    assertContains(m, 1, "map({1,2}, _ => 0)", false)
    assertContains(m, 2, "map({1,2}, _ => 0)", false)
  }

  // Category 9: Contains

  test("Test 9.1: contains basic test") {
    val s = singletonSet(42)
    assertContains(s, 42, "singletonSet(42)", true)
  }

  test("Test 9.2: contains returns false for missing element") {
    val s = singletonSet(42)
    assertContains(s, 0, "singletonSet(42)", false)
  }

  // Category 10: ToString

  test("Test 10.1: toString of singleton") {
    val s = singletonSet(1)
    val str = FunSets.toString(s)
    assert(str.contains("1"),
      s"FunSets.toString(singletonSet(1)) should contain '1', but got: '$str'")
  }

  test("Test 10.2: toString of multiple elements") {
    val s = union(singletonSet(1), singletonSet(2))
    val str = FunSets.toString(s)
    assert(str.contains("1"),
      s"FunSets.toString(union({1},{2})) should contain '1', but got: '$str'")
    assert(str.contains("2"),
      s"FunSets.toString(union({1},{2})) should contain '2', but got: '$str'")
  }
}

