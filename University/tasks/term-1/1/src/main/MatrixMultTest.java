package main;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

public class MatrixMultTest {

    private Matrix matrix2x3;
    private Matrix matrix3x2;
    private Matrix identity2x2;
    private Matrix identity3x3;
    private Matrix zero3x3;

    @Before
    public void setUp() {
        matrix2x3 = new Matrix(new double[][]{
                {1, 2, 3},
                {4, 5, 6}
        });

        matrix3x2 = new Matrix(new double[][]{
                {7, 8},
                {9, 10},
                {11, 12}
        });

        identity2x2 = new Matrix(new double[][]{
                {1, 0},
                {0, 1}
        });

        identity3x3 = new Matrix(new double[][]{
                {1, 0, 0},
                {0, 1, 0},
                {0, 0, 1}
        });

        zero3x3 = new Matrix(new double[][]{
                {0, 0, 0},
                {0, 0, 0},
                {0, 0, 0}
        });
    }

    @Test
    public void testValidMatrixCreation() {
        Matrix m = new Matrix(new double[][]{{1, 2}, {3, 4}});
        assertEquals(2, m.getRows());
        assertEquals(2, m.getCols());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullMatrixCreation() {
        new Matrix(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmptyMatrixCreation() {
        new Matrix(new double[0][0]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmptyRowMatrixCreation() {
        new Matrix(new double[][]{new double[0]});
    }

    @Test(expected = IllegalArgumentException.class)
    public void testJaggedMatrixCreation() {
        new Matrix(new double[][]{
                {1, 2},
                {3}
        });
    }

    @Test
    public void testValidMultiplication() {
        double[][] resultData = matrix2x3.multiply(matrix3x2);
        double[][] expected = {{58, 64}, {139, 154}};

        assertArrayEquals(expected[0], resultData[0], 0.001);
        assertArrayEquals(expected[1], resultData[1], 0.001);
    }

    @Test
    public void testMultiplicationWithIdentity() {
        double[][] result1 = matrix2x3.multiply(identity3x3);

        assertArrayEquals(matrix2x3.getData()[0], result1[0], 0.001);
        assertArrayEquals(matrix2x3.getData()[1], result1[1], 0.001);

        Matrix square2x2 = new Matrix(new double[][]{{1, 2}, {3, 4}});
        double[][] result2 = square2x2.multiply(identity2x2);
        assertArrayEquals(square2x2.getData()[0], result2[0], 0.001);
        assertArrayEquals(square2x2.getData()[1], result2[1], 0.001);
    }

    @Test
    public void testMultiplicationWithZeroMatrix() {
        double[][] resultData = matrix2x3.multiply(zero3x3);

        assertEquals(0, resultData[0][0], 0.001);
        assertEquals(0, resultData[0][1], 0.001);
        assertEquals(0, resultData[1][0], 0.001);
        assertEquals(0, resultData[1][1], 0.001);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMultiplicationWithNull() {
        matrix2x3.multiply(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMultiplicationIncompatibleDimensions() {
        Matrix incompatible = new Matrix(new double[][]{{1, 2}}); // 1x2
        matrix2x3.multiply(incompatible); // 2x3 * 1x2 - несовместимо
    }

    @Test
    public void testSingleElementMatrixMultiplication() {
        Matrix a = new Matrix(new double[][]{{2}});
        Matrix b = new Matrix(new double[][]{{3}});

        double[][] result = a.multiply(b);
        assertEquals(6.0, result[0][0], 0.001);
    }

    @Test
    public void testRowVectorMultiplication() {
        Matrix rowVector = new Matrix(new double[][]{{1, 2, 3}});
        Matrix colVector = new Matrix(new double[][]{{4}, {5}, {6}});

        double[][] result = rowVector.multiply(colVector);

        assertEquals(32.0, result[0][0], 0.001);
    }

    @Test
    public void testGetRowsAndCols() {
        assertEquals(2, matrix2x3.getRows());
        assertEquals(3, matrix2x3.getCols());
        assertEquals(3, matrix3x2.getRows());
        assertEquals(2, matrix3x2.getCols());
    }

    @Test
    public void testGetDataReturnsCopy() {
        double[][] originalData = {{1, 2}, {3, 4}};
        Matrix matrix = new Matrix(originalData);
        double[][] copy = matrix.getData();

        copy[0][0] = 100;
        assertNotEquals(100, matrix.getData()[0][0], 0.001);
    }

    @Test
    public void testMatrixImmutableAfterCreation() {
        double[][] originalData = {{1, 2}, {3, 4}};
        Matrix matrix = new Matrix(originalData);

        originalData[0][0] = 100;
        assertNotEquals(100, matrix.getData()[0][0], 0.001);
    }

    @Test
    public void testSymmetricMultiplication() {
        Matrix symmetric = new Matrix(new double[][]{
                {1, 2},
                {2, 1}
        });
        double[][] result = symmetric.multiply(symmetric);

        double[][] expected = {{5, 4}, {4, 5}};

        assertArrayEquals(expected[0], result[0], 0.001);
        assertArrayEquals(expected[1], result[1], 0.001);
    }

    @Test
    public void testLargeNumberMultiplication() {
        Matrix large1 = new Matrix(new double[][]{{1e10}});
        Matrix large2 = new Matrix(new double[][]{{1e20}});
        double[][] result = large1.multiply(large2);

        assertEquals(1e30, result[0][0], 1e20);
    }

    @Test
    public void testDecimalMultiplication() {
        Matrix dec1 = new Matrix(new double[][]{{0.1, 0.2}});
        Matrix dec2 = new Matrix(new double[][]{{0.3}, {0.4}});
        double[][] result = dec1.multiply(dec2);

        assertEquals(0.11, result[0][0], 0.000001);
    }

    @Test
    public void testNegativeNumbersMultiplication() {
        Matrix neg1 = new Matrix(new double[][]{{-1, -2}});
        Matrix neg2 = new Matrix(new double[][]{{-3}, {4}});
        double[][] result = neg1.multiply(neg2);

        assertEquals(-5, result[0][0], 0.001);
    }

    @Test
    public void testNonSquareMatrixMultiplication() {
        Matrix rect1 = new Matrix(new double[][]{
                {1, 2, 3},
                {4, 5, 6}
        });
        Matrix rect2 = new Matrix(new double[][]{
                {1, 2},
                {3, 4},
                {5, 6}
        });
        double[][] result = rect1.multiply(rect2);

        double[][] expected = {{22, 28}, {49, 64}};

        assertArrayEquals(expected[0], result[0], 0.001);
        assertArrayEquals(expected[1], result[1], 0.001);
    }

    @Test
    public void testZeroMatrixMultiplication() {
        Matrix zero1 = new Matrix(new double[][]{{0, 0}, {0, 0}});
        Matrix zero2 = new Matrix(new double[][]{{0, 0}, {0, 0}});
        double[][] result = zero1.multiply(zero2);

        double[][] expected = {{0, 0}, {0, 0}};

        assertArrayEquals(expected[0], result[0], 0.001);
        assertArrayEquals(expected[1], result[1], 0.001);
    }

    @Test
    public void testSpecialCaseBlockMultiplication() {
        Matrix A = new Matrix(new double[][]{
                {1, 2, 3, 4},
                {5, 6, 7, 8},
                {9, 10, 11, 12}
        });
        Matrix B = new Matrix(new double[][]{
                {1, 2},
                {3, 4},
                {5, 6},
                {7, 8}
        });

        double[][] result = A.multiply(B);
        double[][] expected = {
                {50, 60},
                {114, 140},
                {178, 220}
        };

        assertArrayEquals(expected[0], result[0], 0.001);
        assertArrayEquals(expected[1], result[1], 0.001);
        assertArrayEquals(expected[2], result[2], 0.001);
    }

    @Test
    public void testPerformanceMeasurement() {
        double result = Matrix.measureMultiplicationPerformance(1_000, 10);
        assertTrue(result >= 0);
    }

}
