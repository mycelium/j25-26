package org.example;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/*
Важные замечания:
- Для сравнения умножений я использовал built-in библиотеку.
  Чтобы установить ее, необходимо добавить следующую dependency в pom.xml:

<dependency>
    <groupId>org.apache.commons</groupId>
    <artifactId>commons-math3</artifactId>
    <version>3.6.1</version>
    <scope>test</scope>
</dependency>

- Я не тестировал время выполнения, потому что преподаватель сказал мне,
  что нет ограничений на оптимизацию времени выполнения (:
*/

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MatrixMultiplicationTest {

    private static final double DELTA = 0.0001; // Tolerance for floating point comparison

    // Category 1: Basic Functionality

    @Test
    @Order(1)
    @DisplayName("Test 1.1: Basic 2×2 matrix multiplication")
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
        double[][] result = MatrixMult.multiply(A, B);

        assertNotNull(result, "Result should not be null");
        assertMatrixEquals(expected, result, "2×2 multiplication incorrect");
    }

    @Test
    @Order(2)
    @DisplayName("Test 1.2: Basic 3×3 matrix multiplication")
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
        double[][] result = MatrixMult.multiply(A, B);

        assertNotNull(result, "Result should not be null");
        assertMatrixEquals(expected, result, "3×3 multiplication incorrect");
    }

    @Test
    @Order(3)
    @DisplayName("Test 1.3: Non-square matrices (2×3 × 3×2)")
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
        double[][] result = MatrixMult.multiply(A, B);

        assertNotNull(result, "Result should not be null");
        assertMatrixEquals(expected, result, "2×3 × 3×2 multiplication incorrect");
    }

    @Test
    @Order(4)
    @DisplayName("Test 1.4: Non-square matrices (3×2 × 2×4)")
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
        double[][] result = MatrixMult.multiply(A, B);

        assertNotNull(result, "Result should not be null");
        assertMatrixEquals(expected, result, "3×2 × 2×4 multiplication incorrect");
    }

    @Test
    @Order(5)
    @DisplayName("Test 1.5: Row vector × Column vector (1×4 × 4×1)")
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
        double[][] result = MatrixMult.multiply(A, B);

        assertNotNull(result, "Result should not be null");
        assertMatrixEquals(expected, result, "Row × Column vector multiplication incorrect");
    }

    @Test
    @Order(6)
    @DisplayName("Test 1.6: Column vector × Row vector (3×1 × 1×3)")
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
        double[][] result = MatrixMult.multiply(A, B);

        assertNotNull(result, "Result should not be null");
        assertMatrixEquals(expected, result, "Column × Row vector multiplication incorrect");
    }

    // Category 2: Special Matrices

    @Test
    @Order(10)
    @DisplayName("Test 2.1: Identity matrix multiplication")
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

        double[][] expected = referenceMultiply(identity, A);
        double[][] result = MatrixMult.multiply(identity, A);

        assertNotNull(result, "Result should not be null");
        assertMatrixEquals(expected, result, "Identity × A should equal A");
    }

    @Test
    @Order(11)
    @DisplayName("Test 2.2: Zero matrix multiplication")
    void testZeroMatrix() {
        double[][] zero = {
            {0, 0},
            {0, 0}
        };
        double[][] A = {
            {5, 6},
            {7, 8}
        };

        double[][] expected = referenceMultiply(zero, A);
        double[][] result = MatrixMult.multiply(zero, A);

        assertNotNull(result, "Result should not be null");
        assertMatrixEquals(expected, result, "Zero × A should equal Zero");
    }

    @Test
    @Order(12)
    @DisplayName("Test 2.3: Single element matrix (1×1)")
    void testSingleElement() {
        double[][] A = {{5}};
        double[][] B = {{3}};

        double[][] expected = referenceMultiply(A, B);
        double[][] result = MatrixMult.multiply(A, B);

        assertNotNull(result, "Result should not be null");
        assertMatrixEquals(expected, result, "1×1 multiplication incorrect");
    }

    // Category 3: Numerical Edge Cases

    @Test
    @Order(20)
    @DisplayName("Test 3.1: Negative numbers")
    void testNegativeNumbers() {
        double[][] A = {
            {-1, 2},
            {3, -4}
        };
        double[][] B = {
            {5, -6},
            {-7, 8}
        };

        double[][] expected = referenceMultiply(A, B);
        double[][] result = MatrixMult.multiply(A, B);

        assertNotNull(result, "Result should not be null");
        assertMatrixEquals(expected, result, "Negative number multiplication incorrect");
    }

    @Test
    @Order(21)
    @DisplayName("Test 3.2: Decimal/floating point numbers")
    void testDecimalNumbers() {
        double[][] A = {
            {1.5, 2.5},
            {3.5, 4.5}
        };
        double[][] B = {
            {5.5, 6.5},
            {7.5, 8.5}
        };

        double[][] expected = referenceMultiply(A, B);
        double[][] result = MatrixMult.multiply(A, B);

        assertNotNull(result, "Result should not be null");
        assertMatrixEquals(expected, result, "Decimal number multiplication incorrect");
    }

    @Test
    @Order(22)
    @DisplayName("Test 3.3: Very large numbers")
    void testVeryLargeNumbers() {
        double[][] A = {
            {1e10, 2e10},
            {3e10, 4e10}
        };
        double[][] B = {
            {5, 6},
            {7, 8}
        };

        double[][] expected = referenceMultiply(A, B);
        double[][] result = MatrixMult.multiply(A, B);

        assertNotNull(result, "Result should not be null");
        assertMatrixEquals(expected, result, "Large number multiplication incorrect");
    }

    @Test
    @Order(23)
    @DisplayName("Test 3.4: Very small numbers (near zero)")
    void testVerySmallNumbers() {
        double[][] A = {
            {1e-10, 2e-10},
            {3e-10, 4e-10}
        };
        double[][] B = {
            {5e10, 6e10},
            {7e10, 8e10}
        };

        double[][] expected = referenceMultiply(A, B);
        double[][] result = MatrixMult.multiply(A, B);

        assertNotNull(result, "Result should not be null");
        assertMatrixEquals(expected, result, "Small number multiplication incorrect");
    }

    @Test
    @Order(24)
    @DisplayName("Test 3.5: Mixed positive, negative, large, small")
    void testMixedNumbers() {
        double[][] A = {
            {1000, -0.001},
            {0.5, -500}
        };
        double[][] B = {
            {0.002, 2000},
            {-1000, 0.001}
        };

        double[][] expected = referenceMultiply(A, B);
        double[][] result = MatrixMult.multiply(A, B);

        assertNotNull(result, "Result should not be null");
        assertEquals(2, result.length);
        assertEquals(2, result[0].length);
        assertMatrixEquals(expected, result, "Mixed number multiplication incorrect");
    }

    // Category 4: Error Handling - Invalid Inputs

    @Test
    @Order(30)
    @DisplayName("Test 4.1: First matrix is null")
    void testFirstMatrixNull() {
        double[][] B = {
            {1, 2},
            {3, 4}
        };

        double[][] result = MatrixMult.multiply(null, B);

        assertNull(result, "Result should be null when first matrix is null");
    }

    @Test
    @Order(31)
    @DisplayName("Test 4.2: Second matrix is null")
    void testSecondMatrixNull() {
        double[][] A = {
            {1, 2},
            {3, 4}
        };

        double[][] result = MatrixMult.multiply(A, null);

        assertNull(result, "Result should be null when second matrix is null");
    }

    @Test
    @Order(32)
    @DisplayName("Test 4.3: Both matrices are null")
    void testBothMatricesNull() {
        double[][] result = MatrixMult.multiply(null, null);

        assertNull(result, "Result should be null when both matrices are null");
    }

    @Test
    @Order(33)
    @DisplayName("Test 4.4: First matrix is empty")
    void testFirstMatrixEmpty() {
        double[][] A = {};
        double[][] B = {
            {1, 2},
            {3, 4}
        };

        double[][] result = MatrixMult.multiply(A, B);

        assertNull(result, "Result should be null when first matrix is empty");
    }

    @Test
    @Order(34)
    @DisplayName("Test 4.5: Second matrix is empty")
    void testSecondMatrixEmpty() {
        double[][] A = {
            {1, 2},
            {3, 4}
        };
        double[][] B = {};

        double[][] result = MatrixMult.multiply(A, B);

        assertNull(result, "Result should be null when second matrix is empty");
    }

    @Test
    @Order(35)
    @DisplayName("Test 4.6: First matrix has null row")
    void testFirstMatrixNullRow() {
        double[][] A = {
            {1, 2},
            null
        };
        double[][] B = {
            {1, 2},
            {3, 4}
        };

        double[][] result = MatrixMult.multiply(A, B);

        assertNull(result, "Result should be null when first matrix has null row");
    }

    @Test
    @Order(36)
    @DisplayName("Test 4.7: Second matrix has null row")
    void testSecondMatrixNullRow() {
        double[][] A = {
            {1, 2},
            {3, 4}
        };
        double[][] B = {
            {1, 2},
            null
        };

        double[][] result = MatrixMult.multiply(A, B);

        assertNull(result, "Result should be null when second matrix has null row");
    }

    @Test
    @Order(37)
    @DisplayName("Test 4.8: First matrix has empty row")
    void testFirstMatrixEmptyRow() {
        double[][] A = {{1, 2}, {}};
        double[][] B = {
            {1, 2},
            {3, 4}
        };

        double[][] result = MatrixMult.multiply(A, B);

        assertNull(result, "Result should be null when first matrix has empty row");
    }

    @Test
    @Order(38)
    @DisplayName("Test 4.9: Incompatible dimensions (2×3 × 2×2)")
    void testIncompatibleDimensions() {
        double[][] A = {
            {1, 2, 3},
            {4, 5, 6}
        };
        double[][] B = {
            {1, 2},
            {3, 4}
        };

        double[][] result = MatrixMult.multiply(A, B);

        assertNull(result, "Result should be null when dimensions are incompatible");
    }

    @Test
    @Order(39)
    @DisplayName("Test 4.10: Irregular first matrix (different row lengths)")
    void testIrregularFirstMatrix() {
        double[][] A = {
            {1, 2, 3},
            {4, 5}
        };
        double[][] B = {
            {1, 2},
            {3, 4},
            {5, 6}
        };

        double[][] result = MatrixMult.multiply(A, B);

        assertNull(result, "Result should be null when first matrix is irregular");
    }

    @Test
    @Order(40)
    @DisplayName("Test 4.11: Irregular second matrix (different row lengths)")
    void testIrregularSecondMatrix() {
        double[][] A = {
            {1, 2},
            {3, 4}
        };
        double[][] B = {
            {1, 2, 3},
            {4, 5}
        };

        double[][] result = MatrixMult.multiply(A, B);

        assertNull(result, "Result should be null when second matrix is irregular");
    }

    // Category 5: Matrix Dimensions Variety

    @Test
    @Order(50)
    @DisplayName("Test 5.1: Square matrix 5×5")
    void testSquare5x5() {
        double[][] A = createMatrix(5, 5, 2.0);
        double[][] B = createMatrix(5, 5, 3.0);

        double[][] result = MatrixMult.multiply(A, B);

        assertNotNull(result, "Result should not be null");
        assertEquals(5, result.length, "Result should have 5 rows");
        assertEquals(5, result[0].length, "Result should have 5 columns");
        assertEquals(30.0, result[0][0], DELTA, "Element value incorrect");
    }

    @Test
    @Order(51)
    @DisplayName("Test 5.2: Tall matrix (10×3 × 3×5)")
    void testTallMatrix() {
        double[][] A = createMatrix(10, 3, 1.0);
        double[][] B = createMatrix(3, 5, 1.0);

        double[][] result = MatrixMult.multiply(A, B);

        assertNotNull(result, "Result should not be null");
        assertEquals(10, result.length, "Result should have 10 rows");
        assertEquals(5, result[0].length, "Result should have 5 columns");
    }

    @Test
    @Order(52)
    @DisplayName("Test 5.3: Wide matrix (3×10 × 10×5)")
    void testWideMatrix() {
        double[][] A = createMatrix(3, 10, 1.0);
        double[][] B = createMatrix(10, 5, 1.0);

        double[][] result = MatrixMult.multiply(A, B);

        assertNotNull(result, "Result should not be null");
        assertEquals(3, result.length, "Result should have 3 rows");
        assertEquals(5, result[0].length, "Result should have 5 columns");
    }

    @Test
    @Order(53)
    @DisplayName("Test 5.4: Extreme aspect ratio (1×100 × 100×1)")
    void testExtremeAspectRatio() {
        double[][] A = createMatrix(1, 100, 2.0);
        double[][] B = createMatrix(100, 1, 3.0);

        double[][] result = MatrixMult.multiply(A, B);

        assertNotNull(result, "Result should not be null");
        assertEquals(1, result.length, "Result should have 1 row");
        assertEquals(1, result[0].length, "Result should have 1 column");
        assertEquals(600.0, result[0][0], DELTA, "Dot product incorrect");
    }

    // Category 6: Large Matrix Performance

    @Test
    @Order(60)
    @DisplayName("Test 6.1: Medium matrix 100×100")
    @Timeout(30)
    void testMediumMatrix100x100() {
        System.out.println("\n=== Testing 100×100 matrices ===");
        double[][] A = createRandomMatrix(100, 100);
        double[][] B = createRandomMatrix(100, 100);

        double[][] expected = referenceMultiply(A, B);
        double[][] result = MatrixMult.multiply(A, B);

        assertNotNull(result, "Result should not be null");
        assertMatrixEquals(expected, result, "multiplication incorrect");


    }

    @Test
    @Order(61)
    @DisplayName("Test 6.2: Medium matrix 500×500")
    @Timeout(120)
    void testLargeMatrix500x500() {
        System.out.println("\n=== Testing 500×500 matrices ===");
        double[][] A = createRandomMatrix(500, 500);
        double[][] B = createRandomMatrix(500, 500);

        double[][] expected = referenceMultiply(A, B);
        double[][] result = MatrixMult.multiply(A, B);

        assertNotNull(result, "Result should not be null");
        assertMatrixEquals(expected, result, "multiplication incorrect");
    }

    @Test
    @Order(62)
    @DisplayName("Test 6.3: Large matrix 1000×1000")
    @Timeout(600)
    void testExtremeMatrix5000x5000() {
        System.out.println("\n=== Testing 1000×1000 matrices ===");
        double[][] A = createRandomMatrix(1000, 1000);
        double[][] B = createRandomMatrix(1000, 1000);

        double[][] expected = referenceMultiply(A, B);
        double[][] result = MatrixMult.multiply(A, B);

        assertNotNull(result, "Result should not be null");
        assertMatrixEquals(expected, result, "multiplication incorrect");
    }

    @Test
    @Order(63)
    @DisplayName("Test 6.4: Very Large matrix 10000×10000 ")
    @Timeout(1200)
    @Disabled("Takes long time")
    void testMaximumMatrix10000x10000() {
        System.out.println("\n=== Testing 10000×10000 matrices ===");
        double[][] A = createRandomMatrix(10000, 10000);
        double[][] B = createRandomMatrix(10000, 10000);

        double[][] result = MatrixMult.multiply(A, B);

        assertNotNull(result, "Result should not be null");
        assertEquals(10000, result.length, "Result should have 10000 rows");
        assertEquals(10000, result[0].length, "Result should have 10000 columns");
    }

    @Test
    @Order(64)
    @DisplayName("Test 6.5: Large non-square (2000×500 × 500×3000)")
    @Timeout(1200)
    @Disabled("Takes long time")
    void testLargeNonSquare() {
        System.out.println("\n=== Testing 2000×500 × 500×3000 matrices ===");
        double[][] A = createRandomMatrix(2000, 500);
        double[][] B = createRandomMatrix(500, 3000);

        double[][] expected = referenceMultiply(A, B);
        double[][] result = MatrixMult.multiply(A, B);

        assertNotNull(result, "Result should not be null");
        assertMatrixEquals(expected, result, "multiplication incorrect");
    }

    // Helper Methods
    
      //Reference implementation using Apache Commons Math library

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
 
     // Creates a matrix filled with specified
    double[][] createMatrix(int rows, int cols, double value) {
        double[][] matrix = new double[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                matrix[i][j] = value;
            }
        }
        return matrix;
    }
    
     // Creates a matrix with random values between 0 and 10
    private double[][] createRandomMatrix(int rows, int cols) {
        double[][] matrix = new double[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                matrix[i][j] = Math.random() * 10;
            }
        }
        return matrix;
    }
 
     //Compares two matrices with tolerance for floating point errors
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

