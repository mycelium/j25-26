package recfun

import org.scalatest.funsuite.AnyFunSuite

/*
Важные замечания:
- Тесты проверяют корректность рекурсивных функций.
- Тесты охватывают базовые случаи, граничные случаи и обработку ошибок.
- Для запуска: sbt test
*/

class MainTest extends AnyFunSuite {

  import Main._

  //  Category 1: Pascal's Triangle 

  test("Test 1.1: pascal - top of triangle (0,0)") {
    val result = pascal(0, 0)
    assert(result === 1,
      s"pascal(c=0, r=0) should return 1 (top of triangle), but got $result")
  }

  test("Test 1.2: pascal - left edge (0, r)") {
    for (r <- List(1, 2, 5, 10)) {
      val result = pascal(0, r)
      assert(result === 1,
        s"pascal(c=0, r=$r) should return 1 (left edge is always 1), but got $result")
    }
  }

  test("Test 1.3: pascal - right edge (r, r)") {
    for (r <- List(1, 2, 5, 10)) {
      val result = pascal(r, r)
      assert(result === 1,
        s"pascal(c=$r, r=$r) should return 1 (right edge is always 1), but got $result")
    }
  }

  test("Test 1.4: pascal - middle elements") {
    val cases = List((1,2,2), (1,3,3), (2,3,3), (1,4,4), (2,4,6), (3,4,4))
    for ((c, r, expected) <- cases) {
      val result = pascal(c, r)
      assert(result === expected,
        s"pascal(c=$c, r=$r) should return $expected, but got $result. " +
        s"Pascal's triangle row $r: ${(0 to r).map(pascal(_, r)).mkString(" ")}")
    }
  }

  test("Test 1.5: pascal - row 5") {
    // Row 5: 1 5 10 10 5 1
    val expectedRow = List(1, 5, 10, 10, 5, 1)
    for ((expected, c) <- expectedRow.zipWithIndex) {
      val result = pascal(c, 5)
      assert(result === expected,
        s"pascal(c=$c, r=5) should return $expected, but got $result. " +
        s"Expected row 5: ${expectedRow.mkString(" ")}, " +
        s"actual row 5: ${(0 to 5).map(pascal(_, 5)).mkString(" ")}")
    }
  }

  test("Test 1.6: pascal - negative column") {
    val result = pascal(-1, 5)
    assert(result === 0,
      s"pascal(c=-1, r=5) should return 0 (negative column is out of bounds), but got $result")
  }

  test("Test 1.7: pascal - negative row") {
    val result = pascal(0, -1)
    assert(result === 0,
      s"pascal(c=0, r=-1) should return 0 (negative row is out of bounds), but got $result")
  }

  test("Test 1.8: pascal - column greater than row") {
    val result = pascal(5, 3)
    assert(result === 0,
      s"pascal(c=5, r=3) should return 0 (column > row is out of bounds), but got $result")
  }

  test("Test 1.9: pascal - large values") {
    // Row 10: 1 10 45 120 210 252 210 120 45 10 1
    val result = pascal(5, 10)
    assert(result === 252,
      s"pascal(c=5, r=10) should return 252 (center of row 10), but got $result. " +
      s"Actual row 10: ${(0 to 10).map(pascal(_, 10)).mkString(" ")}")
  }

  //  Category 2: Balance 

  private def assertBalance(input: String, expected: Boolean): Unit = {
    val result = balance(input.toList)
    assert(result === expected,
      s"balance(\"$input\") should return $expected, but got $result. " +
      s"Input has ${input.count(_ == '(')} open and ${input.count(_ == ')')} close parentheses.")
  }

  test("Test 2.1: balance - empty string") {
    assertBalance("", true)
  }

  test("Test 2.2: balance - no parentheses") {
    assertBalance("hello world", true)
  }

  test("Test 2.3: balance - simple balanced ()") {
    assertBalance("()", true)
  }

  test("Test 2.4: balance - nested balanced (())") {
    assertBalance("(())", true)
  }

  test("Test 2.5: balance - multiple balanced ()()") {
    assertBalance("()()", true)
  }

  test("Test 2.6: balance - complex balanced expression") {
    assertBalance("(if (zero? x) max (/ 1 x))", true)
  }

  test("Test 2.7: balance - balanced with text") {
    assertBalance("I told him (that it's not (yet) done). (But he wasn't listening)", true)
  }

  test("Test 2.8: balance - unbalanced :-)") {
    assertBalance(":-)", false)
  }

  test("Test 2.9: balance - unbalanced ())(") {
    assertBalance("())(", false)
  }

  test("Test 2.10: balance - unbalanced open") {
    assertBalance("(", false)
  }

  test("Test 2.11: balance - unbalanced close") {
    assertBalance(")", false)
  }

  test("Test 2.12: balance - wrong order )(") {
    assertBalance(")(", false)
  }

  test("Test 2.13: balance - unbalanced more opens") {
    assertBalance("(()", false)
  }

  test("Test 2.14: balance - unbalanced more closes") {
    assertBalance("())", false)
  }

  test("Test 2.15: balance - deeply nested") {
    assertBalance("(((())))", true)
  }

  //  Category 3: Count Change 

  private def assertCountChange(money: Int, coins: List[Int], expected: Int): Unit = {
    val result = countChange(money, coins)
    assert(result === expected,
      s"countChange(money=$money, coins=${coins.mkString("[",",","]")}) " +
      s"should return $expected, but got $result")
  }

  test("Test 3.1: countChange - zero money") {
    assertCountChange(0, List(1, 2), 1)
  }

  test("Test 3.2: countChange - empty coins") {
    assertCountChange(5, List(), 0)
  }

  test("Test 3.3: countChange - negative money") {
    assertCountChange(-5, List(1, 2), 0)
  }

  test("Test 3.4: countChange - simple case 4 with coins 1,2") {
    // 4 = 1+1+1+1 = 1+1+2 = 2+2 → 3 
    assertCountChange(4, List(1, 2), 3)
  }

  test("Test 3.5: countChange - 5 with coins 2,3") {
    // 5 = 2+3 → 1 
    assertCountChange(5, List(2, 3), 1)
  }

  test("Test 3.6: countChange - 10 with coins 1,5,10") {
    // 10 = 10x1 = 5x1+5 = 2x5 = 10 → 4 
    assertCountChange(10, List(1, 5, 10), 4)
  }

  test("Test 3.7: countChange - 1 with coin 1") {
    assertCountChange(1, List(1), 1)
  }

  test("Test 3.8: countChange - impossible (no valid coins)") {
    assertCountChange(3, List(5, 10), 0)
  }

  test("Test 3.9: countChange - single coin exact") {
    assertCountChange(10, List(10), 1)
  }

  test("Test 3.10: countChange - 6 with coins 1,2,3") {
    // 6: 1+1+1+1+1+1, 1+1+1+1+2, 1+1+2+2, 2+2+2, 1+1+1+3, 1+2+3, 3+3 → 7 
    assertCountChange(6, List(1, 2, 3), 7)
  }

  test("Test 3.11: countChange - larger amount") {
    // 100 cents with quarters, dimes, nickels, pennies
    assertCountChange(100, List(1, 5, 10, 25), 242)
  }

  //  Category 4: N-Queens 

  test("Test 4.1: nQueens - size 0 returns None") {
    val result = nQueens(0)
    assert(result === None,
      s"nQueens(0) should return None (no solution for size 0), but got $result")
  }

  test("Test 4.2: nQueens - size negative returns None") {
    val result = nQueens(-1)
    assert(result === None,
      s"nQueens(-1) should return None (negative size is invalid), but got $result")
  }

  test("Test 4.3: nQueens - size 1 has solution") {
    val result = nQueens(1)
    assert(result.isDefined,
      "nQueens(1) should return Some(...) with a solution, but got None")
    assert(result.get.length === 1,
      s"nQueens(1) solution should have length 1, but got ${result.get.length}. Solution: ${result.get.mkString("[",",","]")}")
    assert(result.get(0) === 0,
      s"nQueens(1) solution should be [0], but got ${result.get.mkString("[",",","]")}")
  }

  test("Test 4.4: nQueens - size 2 has no solution") {
    val result = nQueens(2)
    assert(result === None,
      s"nQueens(2) should return None (no valid placement for 2 queens on 2x2 board), but got ${result.map(_.mkString("[",",","]"))}")
  }

  test("Test 4.5: nQueens - size 3 has no solution") {
    val result = nQueens(3)
    assert(result === None,
      s"nQueens(3) should return None (no valid placement for 3 queens on 3x3 board), but got ${result.map(_.mkString("[",",","]"))}")
  }

  private def assertValidQueensSolution(size: Int): Unit = {
    val result = nQueens(size)
    assert(result.isDefined,
      s"nQueens($size) should return Some(...) with a valid solution, but got None")
    val sol = result.get
    assert(sol.length === size,
      s"nQueens($size) solution should have $size elements, but got ${sol.length}. Solution: ${sol.mkString("[",",","]")}")
    assert(isValidQueensSolution(sol),
      s"nQueens($size) returned an invalid solution: ${sol.mkString("[",",","]")}. " +
      s"Conflict found: ${findConflict(sol)}")
  }

  test("Test 4.6: nQueens - size 4 has solution") {
    assertValidQueensSolution(4)
  }

  test("Test 4.7: nQueens - size 5 has solution") {
    assertValidQueensSolution(5)
  }

  test("Test 4.8: nQueens - size 6 has solution") {
    assertValidQueensSolution(6)
  }

  test("Test 4.9: nQueens - size 8 has solution") {
    assertValidQueensSolution(8)
  }

  // Helper function to validate N-Queens solution
  private def isValidQueensSolution(solution: Array[Int]): Boolean = {
    val n = solution.length
    for (i <- 0 until n; j <- i + 1 until n) {
      // Check same column
      if (solution(i) == solution(j)) return false
      // Check diagonals
      if (math.abs(solution(i) - solution(j)) == math.abs(i - j)) return false
    }
    true
  }

  // Helper to find the first conflict in a queens solution for error reporting
  private def findConflict(solution: Array[Int]): String = {
    val n = solution.length
    for (i <- 0 until n; j <- i + 1 until n) {
      if (solution(i) == solution(j))
        return s"Queens at rows $i and $j are in the same column ${solution(i)}"
      if (math.abs(solution(i) - solution(j)) == math.abs(i - j))
        return s"Queens at rows $i (col=${solution(i)}) and $j (col=${solution(j)}) are on the same diagonal"
    }
    "no conflict found"
  }
}

