public class Matrix {

    private final double[][] data;

    private final int rows;
    private final int cols;

    private boolean checkMatrix(double[][] data) {
        return data != null && data.length != 0 && data[0].length != 0;
    }

    public Matrix(double[][] data) {
        if (!checkMatrix(data)) {
            throw new IllegalArgumentException("Matrix can not be empty.");
        }

        this.rows = data.length;
        this.cols = data[0].length;
        this.data = new double[this.rows][this.cols];

        for (int i = 0; i < this.rows; i++) {
            if (data[i].length != this.cols) {
                throw new IllegalArgumentException("Rows does not have the same length.");
            }

            System.arraycopy(data[i], 0, this.data[i], 0, this.cols);
        }
    }

    public Matrix multiply(Matrix other) {
        if (!checkMatrix(other.data)) {
            throw new IllegalArgumentException("Matrix can not be empty.");
        }

        int this_cols = this.getCols();
        int other_rows = other.getRows();

        if (this_cols != other_rows) {
            throw new IllegalArgumentException(
                    "Multiplication is not possible due to size differences " +
                            "(columns " + this_cols + " not equal to rows " + other_rows + ")."
            );
        }

        int this_rows = this.getRows();
        int other_cols = other.getCols();

        double[][] resultData = new double[this_rows][other_cols];

        for (int i = 0; i < this_rows; i++) {
            for (int j = 0; j < other_cols; j++) {
                for (int k = 0; k < this_cols; k++) {
                    resultData[i][j] += this.data[i][k] * other.data[k][j];
                }
            }
        }

        return new Matrix(resultData);
    }

    public void displayMatrix() {
        System.out.println("Matrix " + getRows() + "x" + getCols());
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                System.out.printf("%8.2f", data[i][j]);
            }
            System.out.println();
        }
    }

    public int getRows() {
        return rows;
    }

    public int getCols() {
        return cols;
    }
}
