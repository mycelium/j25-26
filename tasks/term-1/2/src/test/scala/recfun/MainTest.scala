package recfun

import org.scalatest.funsuite.AnyFunSuite

/*
Важные замечания:
- Тесты проверяют корректность рекурсивных функций.
- Тесты охватывают базовые случаи, граничные случаи и обработку ошибок.
- Для запуска: .\gradlew.bat test
*/

class MainTest extends AnyFunSuite {

  import Main._

  //  Category 1: Pascal's Triangle 

  test("Test 1.1: pascal - top of triangle (0,0)") {
    assert(pascal(0, 0) === 1)
  }

  test("Test 1.2: pascal - left edge (0, r)") {
    assert(pascal(0, 1) === 1)
    assert(pascal(0, 2) === 1)
    assert(pascal(0, 5) === 1)
    assert(pascal(0, 10) === 1)
  }

  test("Test 1.3: pascal - right edge (r, r)") {
    assert(pascal(1, 1) === 1)
    assert(pascal(2, 2) === 1)
    assert(pascal(5, 5) === 1)
    assert(pascal(10, 10) === 1)
  }

  test("Test 1.4: pascal - middle elements") {
    assert(pascal(1, 2) === 2)
    assert(pascal(1, 3) === 3)
    assert(pascal(2, 3) === 3)
    assert(pascal(1, 4) === 4)
    assert(pascal(2, 4) === 6)
    assert(pascal(3, 4) === 4)
  }

  test("Test 1.5: pascal - row 5") {
    // Row 5: 1 5 10 10 5 1
    assert(pascal(0, 5) === 1)
    assert(pascal(1, 5) === 5)
    assert(pascal(2, 5) === 10)
    assert(pascal(3, 5) === 10)
    assert(pascal(4, 5) === 5)
    assert(pascal(5, 5) === 1)
  }

  test("Test 1.6: pascal - negative column") {
    assert(pascal(-1, 5) === 0)
  }

  test("Test 1.7: pascal - negative row") {
    assert(pascal(0, -1) === 0)
  }

  test("Test 1.8: pascal - column greater than row") {
    assert(pascal(5, 3) === 0)
  }

  test("Test 1.9: pascal - large values") {
    // Row 10: 1 10 45 120 210 252 210 120 45 10 1
    assert(pascal(5, 10) === 252)
  }

  //  Category 2: Balance 

  test("Test 2.1: balance - empty string") {
    assert(balance("".toList) === true)
  }

  test("Test 2.2: balance - no parentheses") {
    assert(balance("hello world".toList) === true)
  }

  test("Test 2.3: balance - simple balanced ()") {
    assert(balance("()".toList) === true)
  }

  test("Test 2.4: balance - nested balanced (())") {
    assert(balance("(())".toList) === true)
  }

  test("Test 2.5: balance - multiple balanced ()()") {
    assert(balance("()()".toList) === true)
  }

  test("Test 2.6: balance - complex balanced expression") {
    assert(balance("(if (zero? x) max (/ 1 x))".toList) === true)
  }

  test("Test 2.7: balance - balanced with text") {
    assert(balance("I told him (that it's not (yet) done). (But he wasn't listening)".toList) === true)
  }

  test("Test 2.8: balance - unbalanced :-)") {
    assert(balance(":-)".toList) === false)
  }

  test("Test 2.9: balance - unbalanced ())(") {
    assert(balance("())(".toList) === false)
  }

  test("Test 2.10: balance - unbalanced open") {
    assert(balance("(".toList) === false)
  }

  test("Test 2.11: balance - unbalanced close") {
    assert(balance(")".toList) === false)
  }

  test("Test 2.12: balance - wrong order )(") {
    assert(balance(")(".toList) === false)
  }

  test("Test 2.13: balance - unbalanced more opens") {
    assert(balance("(()".toList) === false)
  }

  test("Test 2.14: balance - unbalanced more closes") {
    assert(balance("())".toList) === false)
  }

  test("Test 2.15: balance - deeply nested") {
    assert(balance("(((())))".toList) === true)
  }

  //  Category 3: Count Change 

  test("Test 3.1: countChange - zero money") {
    assert(countChange(0, List(1, 2)) === 1)
  }

  test("Test 3.2: countChange - empty coins") {
    assert(countChange(5, List()) === 0)
  }

  test("Test 3.3: countChange - negative money") {
    assert(countChange(-5, List(1, 2)) === 0)
  }

  test("Test 3.4: countChange - simple case 4 with coins 1,2") {
    // 4 = 1+1+1+1 = 1+1+2 = 2+2 → 3 
    assert(countChange(4, List(1, 2)) === 3)
  }

  test("Test 3.5: countChange - 5 with coins 2,3") {
    // 5 = 2+3 → 1 
    assert(countChange(5, List(2, 3)) === 1)
  }

  test("Test 3.6: countChange - 10 with coins 1,5,10") {
    // 10 = 10x1 = 5x1+5 = 2x5 = 10 → 4 
    assert(countChange(10, List(1, 5, 10)) === 4)
  }

  test("Test 3.7: countChange - 1 with coin 1") {
    assert(countChange(1, List(1)) === 1)
  }

  test("Test 3.8: countChange - impossible (no valid coins)") {
    assert(countChange(3, List(5, 10)) === 0)
  }

  test("Test 3.9: countChange - single coin exact") {
    assert(countChange(10, List(10)) === 1)
  }

  test("Test 3.10: countChange - 6 with coins 1,2,3") {
    // 6: 1+1+1+1+1+1, 1+1+1+1+2, 1+1+2+2, 2+2+2, 1+1+1+3, 1+2+3, 3+3 → 7 
    assert(countChange(6, List(1, 2, 3)) === 7)
  }

  test("Test 3.11: countChange - larger amount") {
    // 100 cents with quarters, dimes, nickels, pennies
    assert(countChange(100, List(1, 5, 10, 25)) === 242)
  }

  //  Category 4: N-Queens 

  test("Test 4.1: nQueens - size 0 returns None") {
    assert(nQueens(0) === None)
  }

  test("Test 4.2: nQueens - size negative returns None") {
    assert(nQueens(-1) === None)
  }

  test("Test 4.3: nQueens - size 1 has solution") {
    val result = nQueens(1)
    assert(result.isDefined)
    assert(result.get.length === 1)
    assert(result.get(0) === 0)
  }

  test("Test 4.4: nQueens - size 2 has no solution") {
    assert(nQueens(2) === None)
  }

  test("Test 4.5: nQueens - size 3 has no solution") {
    assert(nQueens(3) === None)
  }

  test("Test 4.6: nQueens - size 4 has solution") {
    val result = nQueens(4)
    assert(result.isDefined)
    assert(result.get.length === 4)
    assert(isValidQueensSolution(result.get))
  }

  test("Test 4.7: nQueens - size 5 has solution") {
    val result = nQueens(5)
    assert(result.isDefined)
    assert(result.get.length === 5)
    assert(isValidQueensSolution(result.get))
  }

  test("Test 4.8: nQueens - size 6 has solution") {
    val result = nQueens(6)
    assert(result.isDefined)
    assert(result.get.length === 6)
    assert(isValidQueensSolution(result.get))
  }

  test("Test 4.9: nQueens - size 8 has solution") {
    val result = nQueens(8)
    assert(result.isDefined)
    assert(result.get.length === 8)
    assert(isValidQueensSolution(result.get))
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
}

