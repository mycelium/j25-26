import jdk.incubator.vector.*;
import java.util.Random;

//Бенчмарк для сравнения Vector API и скалярных операций

public class VectorBenchmark {
    private static final VectorSpecies<Float> SPECIES = FloatVector.SPECIES_PREFERRED;

    //вкторизованное сложение массивов
    public static float[] vectorAdd(float[] a, float[] b) {
        float[] result = new float[a.length];
        int i = 0;

        for (; i < SPECIES.loopBound(a.length); i += SPECIES.length()) {
            FloatVector va = FloatVector.fromArray(SPECIES, a, i);
            FloatVector vb = FloatVector.fromArray(SPECIES, b, i);
            va.add(vb).intoArray(result, i);
        }
        for (; i < a.length; i++) {
            result[i] = a[i] + b[i];
        }
        return result;
    }

    //скалярное сложение массивов
    public static float[] scalarAdd(float[] a, float[] b) {
        float[] result = new float[a.length];
        for (int i = 0; i < a.length; i++) {
            result[i] = a[i] + b[i];
        }
        return result;
    }

    //векторизованное скалярное произведение
    public static float vectorDot(float[] a, float[] b) {
        FloatVector sum = FloatVector.zero(SPECIES);
        int i = 0;

        for (; i < SPECIES.loopBound(a.length); i += SPECIES.length()) {
            FloatVector va = FloatVector.fromArray(SPECIES, a, i);
            FloatVector vb = FloatVector.fromArray(SPECIES, b, i);
            sum = sum.add(va.mul(vb));
        }

        float result = sum.reduceLanes(VectorOperators.ADD);

        for (; i < a.length; i++) {
            result += a[i] * b[i];
        }

        return result;
    }

    //скалярное скалярное произведение
    public static float scalarDot(float[] a, float[] b) {
        float sum = 0;
        for (int i = 0; i < a.length; i++) {
            sum += a[i] * b[i];
        }
        return sum;
    }

    //запуск бенчмарков
    public static void main(String[] args) {
        System.out.println("Бенчмарк Vector API | Скалярные операции\n");
        System.out.println("Длина вектора: " + SPECIES.length() + " float'ов");
        System.out.println("Размер SIMD регистра: " + SPECIES.vectorBitSize() + " бит\n");

        //тестируем
        int[] sizes = {100, 1000, 10000, 78400}; // 78400 = 100 * 784 (размер MNIST * 100)

        for (int size : sizes) {
            System.out.println("Размер массива: " + size);

            //тестовые данные
            Random rand = new Random(42);
            float[] a = new float[size];
            float[] b = new float[size];
            for (int i = 0; i < size; i++) {
                a[i] = rand.nextFloat();
                b[i] = rand.nextFloat();
            }

            //бенчмарк сложения
            long start = System.nanoTime();
            float[] vecAddResult = vectorAdd(a, b);
            long vecAddTime = System.nanoTime() - start;
            start = System.nanoTime();
            float[] scalarAddResult = scalarAdd(a, b);
            long scalarAddTime = System.nanoTime() - start;

            //бенчмарк скалярного произведения
            start = System.nanoTime();
            float vecDotResult = vectorDot(a, b);
            long vecDotTime = System.nanoTime() - start;
            start = System.nanoTime();
            float scalarDotResult = scalarDot(a, b);
            long scalarDotTime = System.nanoTime() - start;

            //проверяем корректность
            boolean addCorrect = true;
            boolean dotCorrect = Math.abs(vecDotResult - scalarDotResult) < 0.001f;

            //результаты
            System.out.printf("  Сложение - Vector: %6.2f мкс, Scalar: %6.2f мкс, Ускорение: %.2fx %s\n",
                    vecAddTime / 1000.0, scalarAddTime / 1000.0,
                    (float) scalarAddTime / vecAddTime,
                    addCorrect ? "✓" : "✗");
            System.out.printf("  Dot Product - Vector: %6.2f мкс, Scalar: %6.2f мкс, Ускорение: %.2fx %s\n",
                    vecDotTime / 1000.0, scalarDotTime / 1000.0,
                    (float) scalarDotTime / vecDotTime,
                    dotCorrect ? "✓" : "✗");
            System.out.println();
        }

        //демонстрация использования Vector API
        System.out.println("Демонстрация Vector API");

        float[] demoA = {1, 2, 3, 4, 5, 6, 7, 8};
        float[] demoB = {8, 7, 6, 5, 4, 3, 2, 1};

        if (demoA.length >= SPECIES.length()) {
            FloatVector va = FloatVector.fromArray(SPECIES, demoA, 0);
            FloatVector vb = FloatVector.fromArray(SPECIES, demoB, 0);
            FloatVector vc = va.add(vb);
            float[] result = new float[SPECIES.length()];
            vc.intoArray(result, 0);

            System.out.print("Пример: [1,2,3,4,5,6,7,8] + [8,7,6,5,4,3,2,1] = [");
            for (int i = 0; i < result.length; i++) {
                System.out.print(result[i]);
                if (i < result.length - 1) System.out.print(", ");
            }
            System.out.println("]");
        }
    }
}