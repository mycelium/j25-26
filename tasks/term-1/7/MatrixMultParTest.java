import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/*
Важные замечания:
- Для сравнения умножений я использовал библиотеку Apache Commons Math.
  Чтобы установить ее, необходимо добавить следующую dependency в build.gradle:

  testImplementation 'org.apache.commons:commons-math3:3.6.1'

- Тесты проверяют корректность параллельного умножения матриц.
- Результаты параллельной и однопоточной версий сравниваются с эталонной реализацией.
- Тесты охватывают базовую функциональность, различное количество потоков,
  граничные случаи и обработку ошибок.
*/

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MatrixMultParTest {

    private static final double DELTA = 0.0001;

    // Category 1: Basic Functionality

    @Test
    @Order(1)
    @DisplayName("Test 1.1: Basic 2x2 matrix multiplication")
    void testBasic2x2() {
        double[][] A = {
            {1, 2},
            {3, 4}
        };
        double[][] B = {
            {5, 6},
            {7, 8}
        };

        double[][] expected = referenceMultiply(A, B);
        double[][] resultParallel = MatrixMultPar.multiplyParallel(A, B);
        double[][] resultSingle = MatrixMultPar.multiplySingle(A, B);

        assertNotNull(resultParallel, "Parallel result should not be null");
        assertNotNull(resultSingle, "Single result should not be null");
        assertMatrixEquals(expected, resultParallel, "Parallel 2x2 multiplication incorrect");
        assertMatrixEquals(expected, resultSingle, "Single 2x2 multiplication incorrect");
    }

    @Test
    @Order(2)
    @DisplayName("Test 1.2: Basic 3x3 matrix multiplication")
    void testBasic3x3() {
        double[][] A = {
            {1, 2, 3},
            {4, 5, 6},
            {7, 8, 9}
        };
        double[][] B = {
            {9, 8, 7},
            {6, 5, 4},
            {3, 2, 1}
        };

        double[][] expected = referenceMultiply(A, B);
        double[][] resultParallel = MatrixMultPar.multiplyParallel(A, B);
        double[][] resultSingle = MatrixMultPar.multiplySingle(A, B);

        assertNotNull(resultParallel, "Parallel result should not be null");
        assertNotNull(resultSingle, "Single result should not be null");
        assertMatrixEquals(expected, resultParallel, "Parallel 3x3 multiplication incorrect");
        assertMatrixEquals(expected, resultSingle, "Single 3x3 multiplication incorrect");
    }

    @Test
    @Order(3)
    @DisplayName("Test 1.3: Non-square matrices (2x3 x 3x2)")
    void testNonSquare2x3_3x2() {
        double[][] A = {
            {1, 2, 3},
            {4, 5, 6}
        };
        double[][] B = {
            {7, 8},
            {9, 10},
            {11, 12}
        };

        double[][] expected = referenceMultiply(A, B);
        double[][] resultParallel = MatrixMultPar.multiplyParallel(A, B);
        double[][] resultSingle = MatrixMultPar.multiplySingle(A, B);

        assertNotNull(resultParallel, "Parallel result should not be null");
        assertMatrixEquals(expected, resultParallel, "Parallel 2x3 x 3x2 multiplication incorrect");
        assertMatrixEquals(expected, resultSingle, "Single 2x3 x 3x2 multiplication incorrect");
    }

    @Test
    @Order(4)
    @DisplayName("Test 1.4: Non-square matrices (3x2 x 2x4)")
    void testNonSquare3x2_2x4() {
        double[][] A = {
            {1, 2},
            {3, 4},
            {5, 6}
        };
        double[][] B = {
            {7, 8, 9, 10},
            {11, 12, 13, 14}
        };

        double[][] expected = referenceMultiply(A, B);
        double[][] resultParallel = MatrixMultPar.multiplyParallel(A, B);
        double[][] resultSingle = MatrixMultPar.multiplySingle(A, B);

        assertNotNull(resultParallel, "Parallel result should not be null");
        assertMatrixEquals(expected, resultParallel, "Parallel 3x2 x 2x4 multiplication incorrect");
        assertMatrixEquals(expected, resultSingle, "Single 3x2 x 2x4 multiplication incorrect");
    }

    @Test
    @Order(5)
    @DisplayName("Test 1.5: Row vector x Column vector (1x4 x 4x1)")
    void testRowVectorTimesColumnVector() {
        double[][] A = {
            {1, 2, 3, 4}
        };
        double[][] B = {
            {5},
            {6},
            {7},
            {8}
        };

        double[][] expected = referenceMultiply(A, B);
        double[][] resultParallel = MatrixMultPar.multiplyParallel(A, B);
        double[][] resultSingle = MatrixMultPar.multiplySingle(A, B);

        assertNotNull(resultParallel, "Parallel result should not be null");
        assertMatrixEquals(expected, resultParallel, "Parallel Row x Column multiplication incorrect");
        assertMatrixEquals(expected, resultSingle, "Single Row x Column multiplication incorrect");
    }

    @Test
    @Order(6)
    @DisplayName("Test 1.6: Column vector x Row vector (3x1 x 1x3)")
    void testColumnVectorTimesRowVector() {
        double[][] A = {
            {2},
            {3},
            {4}
        };
        double[][] B = {
            {5, 6, 7}
        };

        double[][] expected = referenceMultiply(A, B);
        double[][] resultParallel = MatrixMultPar.multiplyParallel(A, B);
        double[][] resultSingle = MatrixMultPar.multiplySingle(A, B);

        assertNotNull(resultParallel, "Parallel result should not be null");
        assertMatrixEquals(expected, resultParallel, "Parallel Column x Row multiplication incorrect");
        assertMatrixEquals(expected, resultSingle, "Single Column x Row multiplication incorrect");
    }

    // Category 2: Thread Count Variations

    @Test
    @Order(10)
    @DisplayName("Test 2.1: Single thread (threadCount=1)")
    void testSingleThread() {
        double[][] A = createRandomMatrix(50, 50);
        double[][] B = createRandomMatrix(50, 50);

        double[][] expected = MatrixMultPar.multiplySingle(A, B);
        double[][] result = MatrixMultPar.multiplyParallel(A, B, 1);

        assertNotNull(result, "Result should not be null");
        assertMatrixEquals(expected, result, "Single thread multiplication incorrect");
    }

    @Test
    @Order(11)
    @DisplayName("Test 2.2: Two threads (threadCount=2)")
    void testTwoThreads() {
        double[][] A = createRandomMatrix(50, 50);
        double[][] B = createRandomMatrix(50, 50);

        double[][] expected = MatrixMultPar.multiplySingle(A, B);
        double[][] result = MatrixMultPar.multiplyParallel(A, B, 2);

        assertNotNull(result, "Result should not be null");
        assertMatrixEquals(expected, result, "Two thread multiplication incorrect");
    }

    @Test
    @Order(12)
    @DisplayName("Test 2.3: Four threads (threadCount=4)")
    void testFourThreads() {
        double[][] A = createRandomMatrix(50, 50);
        double[][] B = createRandomMatrix(50, 50);

        double[][] expected = MatrixMultPar.multiplySingle(A, B);
        double[][] result = MatrixMultPar.multiplyParallel(A, B, 4);

        assertNotNull(result, "Result should not be null");
        assertMatrixEquals(expected, result, "Four thread multiplication incorrect");
    }

    @Test
    @Order(13)
    @DisplayName("Test 2.4: Eight threads (threadCount=8)")
    void testEightThreads() {
        double[][] A = createRandomMatrix(50, 50);
        double[][] B = createRandomMatrix(50, 50);

        double[][] expected = MatrixMultPar.multiplySingle(A, B);
        double[][] result = MatrixMultPar.multiplyParallel(A, B, 8);

        assertNotNull(result, "Result should not be null");
        assertMatrixEquals(expected, result, "Eight thread multiplication incorrect");
    }

    @Test
    @Order(14)
    @DisplayName("Test 2.5: More threads than rows")
    void testMoreThreadsThanRows() {
        double[][] A = createRandomMatrix(5, 50);
        double[][] B = createRandomMatrix(50, 5);

        double[][] expected = MatrixMultPar.multiplySingle(A, B);
        double[][] result = MatrixMultPar.multiplyParallel(A, B, 16);

        assertNotNull(result, "Result should not be null");
        assertMatrixEquals(expected, result, "Should handle more threads than rows");
    }

    @Test
    @Order(15)
    @DisplayName("Test 2.6: Default thread count (system processors)")
    void testDefaultThreadCount() {
        double[][] A = createRandomMatrix(100, 100);
        double[][] B = createRandomMatrix(100, 100);

        double[][] expected = MatrixMultPar.multiplySingle(A, B);
        double[][] result = MatrixMultPar.multiplyParallel(A, B);

        assertNotNull(result, "Result should not be null");
        assertMatrixEquals(expected, result, "Default thread count multiplication incorrect");
    }

    // Category 3: Special Matrices

    @Test
    @Order(20)
    @DisplayName("Test 3.1: Identity matrix multiplication")
    void testIdentityMatrix() {
        double[][] identity = {
            {1, 0, 0},
            {0, 1, 0},
            {0, 0, 1}
        };
        double[][] A = {
            {5, 6, 7},
            {8, 9, 10},
            {11, 12, 13}
        };

        double[][] result = MatrixMultPar.multiplyParallel(identity, A, 2);

        assertNotNull(result, "Result should not be null");
        assertMatrixEquals(A, result, "Identity x A should equal A");
    }

    @Test
    @Order(21)
    @DisplayName("Test 3.2: Zero matrix multiplication")
    void testZeroMatrix() {
        double[][] zero = {
            {0, 0},
            {0, 0}
        };
        double[][] A = {
            {5, 6},
            {7, 8}
        };

        double[][] expected = MatrixMultPar.multiplySingle(zero, A);
        double[][] result = MatrixMultPar.multiplyParallel(zero, A, 2);

        assertNotNull(result, "Result should not be null");
        assertMatrixEquals(expected, result, "Zero x A should equal Zero");
    }

    @Test
    @Order(22)
    @DisplayName("Test 3.3: Single element matrix (1x1)")
    void testSingleElement() {
        double[][] A = {{5}};
        double[][] B = {{3}};

        double[][] expected = MatrixMultPar.multiplySingle(A, B);
        double[][] result = MatrixMultPar.multiplyParallel(A, B, 1);

        assertNotNull(result, "Result should not be null");
        assertMatrixEquals(expected, result, "1x1 multiplication incorrect");
    }

    // Category 4: Numerical Edge Cases

    @Test
    @Order(30)
    @DisplayName("Test 4.1: Negative numbers")
    void testNegativeNumbers() {
        double[][] A = {
            {-1, 2},
            {3, -4}
        };
        double[][] B = {
            {5, -6},
            {-7, 8}
        };

        double[][] expected = MatrixMultPar.multiplySingle(A, B);
        double[][] result = MatrixMultPar.multiplyParallel(A, B, 2);

        assertNotNull(result, "Result should not be null");
        assertMatrixEquals(expected, result, "Negative number multiplication incorrect");
    }

    @Test
    @Order(31)
    @DisplayName("Test 4.2: Decimal/floating point numbers")
    void testDecimalNumbers() {
        double[][] A = {
            {1.5, 2.5},
            {3.5, 4.5}
        };
        double[][] B = {
            {5.5, 6.5},
            {7.5, 8.5}
        };

        double[][] expected = MatrixMultPar.multiplySingle(A, B);
        double[][] result = MatrixMultPar.multiplyParallel(A, B, 2);

        assertNotNull(result, "Result should not be null");
        assertMatrixEquals(expected, result, "Decimal number multiplication incorrect");
    }

    @Test
    @Order(32)
    @DisplayName("Test 4.3: Very large numbers")
    void testVeryLargeNumbers() {
        double[][] A = {
            {1e10, 2e10},
            {3e10, 4e10}
        };
        double[][] B = {
            {5, 6},
            {7, 8}
        };

        double[][] expected = MatrixMultPar.multiplySingle(A, B);
        double[][] result = MatrixMultPar.multiplyParallel(A, B, 2);

        assertNotNull(result, "Result should not be null");
        assertMatrixEquals(expected, result, "Large number multiplication incorrect");
    }

    @Test
    @Order(33)
    @DisplayName("Test 4.4: Very small numbers (near zero)")
    void testVerySmallNumbers() {
        double[][] A = {
            {1e-10, 2e-10},
            {3e-10, 4e-10}
        };
        double[][] B = {
            {5e10, 6e10},
            {7e10, 8e10}
        };

        double[][] expected = MatrixMultPar.multiplySingle(A, B);
        double[][] result = MatrixMultPar.multiplyParallel(A, B, 2);

        assertNotNull(result, "Result should not be null");
        assertMatrixEquals(expected, result, "Small number multiplication incorrect");
    }

    @Test
    @Order(34)
    @DisplayName("Test 4.5: Mixed positive, negative, large, small")
    void testMixedNumbers() {
        double[][] A = {
            {1000, -0.001},
            {0.5, -500}
        };
        double[][] B = {
            {0.002, 2000},
            {-1000, 0.001}
        };

        double[][] expected = MatrixMultPar.multiplySingle(A, B);
        double[][] result = MatrixMultPar.multiplyParallel(A, B, 4);

        assertNotNull(result, "Result should not be null");
        assertMatrixEquals(expected, result, "Mixed number multiplication incorrect");
    }

    // Category 5: Error Handling - Invalid Inputs

    @Test
    @Order(40)
    @DisplayName("Test 5.1: First matrix is null")
    void testFirstMatrixNull() {
        double[][] B = {
            {1, 2},
            {3, 4}
        };

        double[][] result = MatrixMultPar.multiplyParallel(null, B, 2);

        assertNull(result, "Result should be null when first matrix is null");
    }

    @Test
    @Order(41)
    @DisplayName("Test 5.2: Second matrix is null")
    void testSecondMatrixNull() {
        double[][] A = {
            {1, 2},
            {3, 4}
        };

        double[][] result = MatrixMultPar.multiplyParallel(A, null, 2);

        assertNull(result, "Result should be null when second matrix is null");
    }

    @Test
    @Order(42)
    @DisplayName("Test 5.3: Both matrices are null")
    void testBothMatricesNull() {
        double[][] result = MatrixMultPar.multiplyParallel(null, null, 2);

        assertNull(result, "Result should be null when both matrices are null");
    }

    @Test
    @Order(43)
    @DisplayName("Test 5.4: First matrix is empty")
    void testFirstMatrixEmpty() {
        double[][] A = {};
        double[][] B = {
            {1, 2},
            {3, 4}
        };

        double[][] result = MatrixMultPar.multiplyParallel(A, B, 2);

        assertNull(result, "Result should be null when first matrix is empty");
    }

    @Test
    @Order(44)
    @DisplayName("Test 5.5: Second matrix is empty")
    void testSecondMatrixEmpty() {
        double[][] A = {
            {1, 2},
            {3, 4}
        };
        double[][] B = {};

        double[][] result = MatrixMultPar.multiplyParallel(A, B, 2);

        assertNull(result, "Result should be null when second matrix is empty");
    }

    @Test
    @Order(45)
    @DisplayName("Test 5.6: Incompatible dimensions (2x3 x 2x2)")
    void testIncompatibleDimensions() {
        double[][] A = {
            {1, 2, 3},
            {4, 5, 6}
        };
        double[][] B = {
            {1, 2},
            {3, 4}
        };

        double[][] result = MatrixMultPar.multiplyParallel(A, B, 2);

        assertNull(result, "Result should be null when dimensions are incompatible");
    }

    // Category 6: Consistency Tests (Parallel vs Single)

    @Test
    @Order(50)
    @DisplayName("Test 6.1: Parallel result equals single-threaded result (small)")
    void testConsistencySmall() {
        double[][] A = createRandomMatrix(10, 10);
        double[][] B = createRandomMatrix(10, 10);

        double[][] expected = MatrixMultPar.multiplySingle(A, B);

        for (int threads = 1; threads <= 8; threads++) {
            double[][] result = MatrixMultPar.multiplyParallel(A, B, threads);
            assertMatrixEquals(expected, result, "Threads=" + threads + " should match single-threaded");
        }
    }

    @Test
    @Order(51)
    @DisplayName("Test 6.2: Parallel result equals single-threaded result (medium)")
    void testConsistencyMedium() {
        double[][] A = createRandomMatrix(100, 100);
        double[][] B = createRandomMatrix(100, 100);

        double[][] expected = MatrixMultPar.multiplySingle(A, B);

        for (int threads : new int[]{1, 2, 4, 8}) {
            double[][] result = MatrixMultPar.multiplyParallel(A, B, threads);
            assertMatrixEquals(expected, result, "Threads=" + threads + " should match single-threaded");
        }
    }

    @Test
    @Order(52)
    @DisplayName("Test 6.3: Multiple runs produce same result")
    void testDeterminism() {
        double[][] A = createRandomMatrix(50, 50);
        double[][] B = createRandomMatrix(50, 50);

        double[][] result1 = MatrixMultPar.multiplyParallel(A, B, 4);
        double[][] result2 = MatrixMultPar.multiplyParallel(A, B, 4);
        double[][] result3 = MatrixMultPar.multiplyParallel(A, B, 4);

        assertMatrixEquals(result1, result2, "Run 1 and Run 2 should match");
        assertMatrixEquals(result2, result3, "Run 2 and Run 3 should match");
    }

    // Category 7: Matrix Dimensions Variety

    @Test
    @Order(60)
    @DisplayName("Test 7.1: Square matrix 5x5")
    void testSquare5x5() {
        double[][] A = createMatrix(5, 5, 2.0);
        double[][] B = createMatrix(5, 5, 3.0);

        double[][] expected = MatrixMultPar.multiplySingle(A, B);
        double[][] result = MatrixMultPar.multiplyParallel(A, B, 2);

        assertNotNull(result, "Result should not be null");
        assertMatrixEquals(expected, result, "5x5 multiplication incorrect");
    }

    @Test
    @Order(61)
    @DisplayName("Test 7.2: Tall matrix (10x3 x 3x5)")
    void testTallMatrix() {
        double[][] A = createRandomMatrix(10, 3);
        double[][] B = createRandomMatrix(3, 5);

        double[][] expected = MatrixMultPar.multiplySingle(A, B);
        double[][] result = MatrixMultPar.multiplyParallel(A, B, 4);

        assertNotNull(result, "Result should not be null");
        assertEquals(10, result.length, "Result should have 10 rows");
        assertEquals(5, result[0].length, "Result should have 5 columns");
        assertMatrixEquals(expected, result, "Tall matrix multiplication incorrect");
    }

    @Test
    @Order(62)
    @DisplayName("Test 7.3: Wide matrix (3x10 x 10x5)")
    void testWideMatrix() {
        double[][] A = createRandomMatrix(3, 10);
        double[][] B = createRandomMatrix(10, 5);

        double[][] expected = MatrixMultPar.multiplySingle(A, B);
        double[][] result = MatrixMultPar.multiplyParallel(A, B, 2);

        assertNotNull(result, "Result should not be null");
        assertEquals(3, result.length, "Result should have 3 rows");
        assertEquals(5, result[0].length, "Result should have 5 columns");
        assertMatrixEquals(expected, result, "Wide matrix multiplication incorrect");
    }

    @Test
    @Order(63)
    @DisplayName("Test 7.4: Extreme aspect ratio (1x100 x 100x1)")
    void testExtremeAspectRatio() {
        double[][] A = createMatrix(1, 100, 2.0);
        double[][] B = createMatrix(100, 1, 3.0);

        double[][] expected = MatrixMultPar.multiplySingle(A, B);
        double[][] result = MatrixMultPar.multiplyParallel(A, B, 4);

        assertNotNull(result, "Result should not be null");
        assertEquals(1, result.length, "Result should have 1 row");
        assertEquals(1, result[0].length, "Result should have 1 column");
        assertMatrixEquals(expected, result, "Extreme aspect ratio multiplication incorrect");
    }

    // Category 8: Large Matrix Performance

    @Test
    @Order(70)
    @DisplayName("Test 8.1: Medium matrix 100x100")
    @Timeout(30)
    void testMediumMatrix100x100() {
        double[][] A = createRandomMatrix(100, 100);
        double[][] B = createRandomMatrix(100, 100);

        double[][] expected = referenceMultiply(A, B);
        double[][] resultParallel = MatrixMultPar.multiplyParallel(A, B);
        double[][] resultSingle = MatrixMultPar.multiplySingle(A, B);

        assertNotNull(resultParallel, "Parallel result should not be null");
        assertMatrixEquals(expected, resultParallel, "Parallel 100x100 multiplication incorrect");
        assertMatrixEquals(expected, resultSingle, "Single 100x100 multiplication incorrect");
    }

    @Test
    @Order(71)
    @DisplayName("Test 8.2: Large matrix 500x500")
    @Timeout(120)
    void testLargeMatrix500x500() {
        double[][] A = createRandomMatrix(500, 500);
        double[][] B = createRandomMatrix(500, 500);

        double[][] expected = referenceMultiply(A, B);
        double[][] resultParallel = MatrixMultPar.multiplyParallel(A, B);
        double[][] resultSingle = MatrixMultPar.multiplySingle(A, B);

        assertNotNull(resultParallel, "Parallel result should not be null");
        assertMatrixEquals(expected, resultParallel, "Parallel 500x500 multiplication incorrect");
        assertMatrixEquals(expected, resultSingle, "Single 500x500 multiplication incorrect");
    }

    @Test
    @Order(72)
    @DisplayName("Test 8.3: Large matrix 1000x1000")
    @Timeout(600)
    void testLargeMatrix1000x1000() {
        double[][] A = createRandomMatrix(1000, 1000);
        double[][] B = createRandomMatrix(1000, 1000);

        double[][] expected = referenceMultiply(A, B);
        double[][] resultParallel = MatrixMultPar.multiplyParallel(A, B);
        double[][] resultSingle = MatrixMultPar.multiplySingle(A, B);

        assertNotNull(resultParallel, "Parallel result should not be null");
        assertMatrixEquals(expected, resultParallel, "Parallel 1000x1000 multiplication incorrect");
        assertMatrixEquals(expected, resultSingle, "Single 1000x1000 multiplication incorrect");
    }

    @Test
    @Order(73)
    @DisplayName("Test 8.4: Large non-square (500x200 x 200x300)")
    @Timeout(120)
    void testLargeNonSquare() {
        double[][] A = createRandomMatrix(500, 200);
        double[][] B = createRandomMatrix(200, 300);

        double[][] expected = referenceMultiply(A, B);
        double[][] resultParallel = MatrixMultPar.multiplyParallel(A, B);
        double[][] resultSingle = MatrixMultPar.multiplySingle(A, B);

        assertNotNull(resultParallel, "Parallel result should not be null");
        assertEquals(500, resultParallel.length, "Result should have 500 rows");
        assertEquals(300, resultParallel[0].length, "Result should have 300 columns");
        assertMatrixEquals(expected, resultParallel, "Parallel large non-square incorrect");
        assertMatrixEquals(expected, resultSingle, "Single large non-square incorrect");
    }

    // Helper Methods

    // Reference implementation using Apache Commons Math library
    private double[][] referenceMultiply(double[][] A, double[][] B) {
        if (A == null || B == null) return null;
        if (A.length == 0 || B.length == 0) return null;
        if (A[0] == null || B[0] == null) return null;
        if (A[0].length != B.length) return null;

        try {
            RealMatrix matrixA = new Array2DRowRealMatrix(A);
            RealMatrix matrixB = new Array2DRowRealMatrix(B);
            RealMatrix result = matrixA.multiply(matrixB);
            return result.getData();
        } catch (Exception e) {
            return null;
        }
    }

    private double[][] createMatrix(int rows, int cols, double value) {
        double[][] matrix = new double[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                matrix[i][j] = value;
            }
        }
        return matrix;
    }

    private double[][] createRandomMatrix(int rows, int cols) {
        double[][] matrix = new double[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                matrix[i][j] = Math.random() * 10;
            }
        }
        return matrix;
    }

    private void assertMatrixEquals(double[][] expected, double[][] actual, String message) {
        assertNotNull(actual, message + ": Result is null");
        assertEquals(expected.length, actual.length, message + ": Row count mismatch");

        for (int i = 0; i < expected.length; i++) {
            assertNotNull(actual[i], message + ": Row " + i + " is null");
            assertEquals(expected[i].length, actual[i].length,
                    message + ": Column count mismatch at row " + i);

            for (int j = 0; j < expected[i].length; j++) {
                assertEquals(expected[i][j], actual[i][j], DELTA,
                        message + ": Element [" + i + "][" + j + "] mismatch");
            }
        }
    }
}

