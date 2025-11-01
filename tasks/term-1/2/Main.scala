object Main {

  def main(args: Array[String]): Unit = {

    println("Pascal's Triangle")
    for (row <- 0 to 10) {
      for (col <- 0 to row)
        print(pascal(col, row) + " ")
      println()
    }
    println()

    println("Parentheses Balancing")
    for(str <- Array("(if (zero? x) max (/ 1 x))",
                     "I told him (that it's not (yet) done). (But he wasn't listening)",
                     ":-)", "())(", "", "()", "((()))", "(()))(", "((()", "hello world",
                     "(a (b (c)) d)", "(a (b (c)) d))", "((a (b (c)) d)", ")(", "())", "((())",
                     "(word (nested (deep)) text)", "(word (nested (deep) text)",
                     "(word (nested deep)) text)", "(()()()())", "(((())))", "(()((())()))",
                     "(()((())())")){
      println(s"\"$str\" is ${if (balance(str.toList)) "" else "not" } balanced")
    }
    println()

    println("Counting Change")
    for(m <- 0 to 5) {
      for(coinAmt <- 1 to 4) {
        val lst = (1 to coinAmt).toList
        print(s"There is(are) ${countChange(m,lst)} way(s) to pay $m with")
        lst.foreach(el => print(s" $el"))
        println()
      }
    }
    println()

    println("N-Queens Problem")
    for(size <- 1 to 10) {
      println(s"Size = $size")
      val opBoard = nQueens(size)
      if (opBoard != None) printBoard(opBoard.get)
      else println("No solution")
      println()
    }
  }

  def pascal(c: Int, r: Int): Int = {

      if (c != 0 && c != r)
        pascal(c - 1, r - 1) + pascal(c, r - 1)
      else 1
  }

  def balance(chars: List[Char]): Boolean = {

    def checkNextChar(charsLeft: List[Char], unclosedAmt: Int): Boolean = {
      // unclosedAmt - Количество незакрытых ( в рассмотренной строке
      if (charsLeft.isEmpty) unclosedAmt == 0
      else{
        charsLeft.head match {
          case '(' => checkNextChar(charsLeft.tail, unclosedAmt + 1)
          case ')' => { if (unclosedAmt == 0) false
                        else checkNextChar(charsLeft.tail, unclosedAmt - 1) }
          case _ => checkNextChar(charsLeft.tail, unclosedAmt)
        }
      }
    }
    checkNextChar(chars, 0)
  }

  def countChange(money: Int, coins: List[Int]): Int = {

    if (money == 0) 1
      else if (coins.isEmpty || money < 0) 0
      else countChange(money, coins.tail) +
        countChange(money - coins.head, coins)
  }

  def nQueens(size: Int): Option[Array[Int]] = {

    def changePos(qToChange: Int, pos: Int, board: Array[Int]): Option[Array[Int]] = {
      // qToChange - Какую фигуру(строку) меняем;
      // pos - На какой позиции находится фигура сейчас;

      // Дошли до конца строки => С такой постановкой предыдущих
      // королев нет вариантов как поставить королеву на новую
      // строку => Возвращаемся на строку вверх и переставляем фигуру
      if (pos == board.size + 1) {
        if (qToChange == 1) // Если первая фигура дошла
          return None       // до конца строки => Нет решения
        changePos(qToChange - 1, board(qToChange - 2) + 1, board)
      } else if (qToChange == board.size + 1) // Успешно Дошли до последней фигуры
        Some(board)
      else {                                  // Проверка на корректность постановки
        if (isSafe(qToChange, pos, board)) {
          board(qToChange - 1) = pos
          changePos(qToChange + 1, 1, board)
        } else {
          // Есть мешающая королева => Идем на следующий столбец
          changePos(qToChange, pos + 1, board)
        }
      }
    }

    def isSafe(row: Int, col: Int, board: Array[Int]): Boolean = {
      for (i <- 1 until row) {
        if (board(i - 1) == col ||           // По вертикали
          board(i - 1) == col + (i - row) || // По диагонали влево
          board(i - 1) == col - (i - row))   // По диагонали вправо
          return false
      }
      true
    }

    val tmp = Array.fill(size)(0)
    changePos(1,1,tmp)
  }

  def printBoard(board: Array[Int]): Unit ={
    for(i <- 0 until board.size) {
      for(j <- 0 until board.size){
        print((if (board(i) == j + 1) 1 else 0) + " ")
      }
      println()
    }
  }

}

