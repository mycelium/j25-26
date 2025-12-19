import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/*
https://www.sfu.ca/~ssurjano/egg.html
http://al-roomi.org/benchmarks/unconstrained/n-dimensions/187-egg-holder-function

 */
public class Main {

    // параметры задачи
    private static final double MIN_X = -600;
    private static final double MAX_X = 600;
    // целевой оптимум (глобальный минимум) для сравнения
    // глобальный минимум функции Эггхолдера находится в точке f(512,404.2319)≈−959.64
    // алгоритм настроен на минимизацию значения функции (максимизацию приспособленности Fitness=−f(x))
    private static final double KNOWN_OPT_X1 = 512;
    private static final double KNOWN_OPT_X2 = 404.2319;
    private static final double KNOWN_OPT_VAL = -959.6407;

    // параметры ГА
    private static final int POPULATION_SIZE = 100; // рассматриваем одновременно 100 точек (x1,x2)
    private static final int MAX_GENERATIONS = 200; // 200 циклов "отбор-скрещивание-мутация"
    private static final int TOURNAMENT_SIZE = 4; // турнирный отбор из 4 случайных особей
    private static final double CROSSOVER_RATE = 0.9; // вероятность скрещивания
    private static final double MUTATION_RATE = 0.15; // вероятность мутации
    private static final double ELITISM_COUNT = 1; // всегда сохраняем 1 лучшую особь без изменений

    // используем количество доступных ядер
    private static final int THREAD_COUNT = Runtime.getRuntime().availableProcessors();

    public static void main(String[] args) throws InterruptedException, ExecutionException {
        System.out.println("Оптимизация функции Эггхолдера (Параллельный ГА)");
        System.out.println("Потоков доступно: " + THREAD_COUNT);
        System.out.println("Размер популяции: " + POPULATION_SIZE);
        System.out.println("Поколений: " + MAX_GENERATIONS);
        System.out.println("--------------------------------------------------------");

        // запуск последовательной версии для сравнения
        long startSeq = System.nanoTime();
        Individual bestSeq = runAlgorithm(false);
        long endSeq = System.nanoTime();
        double timeSeqMs = (endSeq - startSeq) / 1_000_000.0;

        System.out.println("\n--------------------------------------------------------");

        // запуск параллельной версии
        long startPar = System.nanoTime();
        Individual bestPar = runAlgorithm(true);
        long endPar = System.nanoTime();
        double timeParMs = (endPar - startPar) / 1_000_000.0;

        // итоговое сравнение
        printSummary(bestSeq, timeSeqMs, bestPar, timeParMs);
    }


    private static Individual runAlgorithm(boolean parallel) throws InterruptedException, ExecutionException {
        ExecutorService executor = null;
        if (parallel) {
            executor = Executors.newFixedThreadPool(THREAD_COUNT); // создаем "пул потоков"
        }

        // инициализация начальной популяции
        List<Individual> population = new ArrayList<>(POPULATION_SIZE);
        for (int i = 0; i < POPULATION_SIZE; i++) {
            population.add(Individual.createRandom());
        }

        Individual globalBest = population.get(0); // запоминаем первую особь как лучшую пока что

        System.out.printf("Режим: %s\n", parallel ? "ПАРАЛЛЕЛЬНЫЙ" : "ПОСЛЕДОВАТЕЛЬНЫЙ");
        System.out.println("Gen\t| Best Func Value\t| (x1, x2)");

        for (int gen = 0; gen < MAX_GENERATIONS; gen++) {
            // сортировка для элитизма (по убыванию приспособленности -> по возрастанию функции)
            // минимизируем функцию, значит Fitness = -FunctionValue
            // чем больше Fitness, тем лучше.
            Collections.sort(population, Comparator.comparingDouble(Individual::getFitness).reversed());

            // сохраняем лучшего за всю историю
            if (population.get(0).getFitness() > globalBest.getFitness()) {
                globalBest = population.get(0).copy();
            }

            // визуализация прогресса каждые 20 поколений
            if (gen % 20 == 0 || gen == MAX_GENERATIONS - 1) {
                printProgress(gen, globalBest);
            }

            List<Individual> nextGen = new ArrayList<>(POPULATION_SIZE); // создание новой популяции

            // элитизм: переносим лучших без изменений
            for (int i = 0; i < ELITISM_COUNT; i++) {
                nextGen.add(population.get(i).copy());
            }

            int individualsToCreate = POPULATION_SIZE - (int)ELITISM_COUNT;
            // адаптивная мутация нужна как "регулятор громкости"
            double currentMutationScale = 1.0 - ((double) gen / MAX_GENERATIONS); // уменьшаем амплитуду мутации


            if (parallel) {
                // ПАРАЛЛЕЛЬНАЯ ГЕНЕРАЦИЯ
                // разбиваем задачу создания особей на куски (chunks)
                // chunks определяет количество особей, с которыми будет работать каждый поток
                int chunkSize = (individualsToCreate + THREAD_COUNT - 1) / THREAD_COUNT;
                // создаем задачи для потоков
                List<Callable<List<Individual>>> tasks = new ArrayList<>();

                // final переменные можно использовать в лямбдах
                // лямбда-выражения могут использовать только те переменные, которые гарантированно не изменятся
                // final "замораживает" переменные, что обеспечит безопасную работу в параллельных потоках
                final List<Individual> currentPopRef = population;
                final double scaleRef = currentMutationScale;

                // каждая задача создает несколько новых особей независимо от других потоков
                // для генерации случайных чисел внутри потоков используется ThreadLocalRandom.current(), что исключает
                // конкуренцию за ресурс генератора случайных чисел (в отличие от обычного Random или Math.random(),
                // которые синхронизированы)
                for (int t = 0; t < THREAD_COUNT; t++) {
                    final int limit = Math.min(chunkSize, individualsToCreate - (t * chunkSize));
                    if (limit > 0) {
                        tasks.add(() -> {
                            List<Individual> chunk = new ArrayList<>();
                            for (int k = 0; k < limit; k++) {
                                // - берем текущую популяцию (currentPopRef)
                                // - берем текущий масштаб мутации (scaleRef)
                                // - создаем одну новую особь
                                // - возвращаем готовую особь
                                // - добавляем в список "chunk" новую созданную особь
                                chunk.add(createNewIndividual(currentPopRef, scaleRef));
                            }
                            return chunk;
                        });
                    }
                }

                List<Future<List<Individual>>> results = executor.invokeAll(tasks); // запускаем все задачи параллельно
                for (Future<List<Individual>> result : results) {
                    nextGen.addAll(result.get()); // ждем результаты от каждого потока и собираем всех созданных особей
                }

            } else {
                // ПОСЛЕДОВАТЕЛЬНАЯ ГЕНЕРАЦИЯ
                for (int i = 0; i < individualsToCreate; i++) {
                    nextGen.add(createNewIndividual(population, currentMutationScale));
                }
            }

            population = nextGen; // заменяем старое поколение новым
        }

        // закрываем пул потоков и возвращаем лучшее найденное решение
        if (executor != null) {
            executor.shutdown();
        }

        return globalBest;
    }

    // создает одну новую особь (Селекция -> Кроссовер -> Мутация -> Оценка).
    private static Individual createNewIndividual(List<Individual> parentsPop, double mutationScale) {
        // 1 турнирный отбор
        // выбираем двух родителей для скрещивания
        Individual p1 = tournamentSelect(parentsPop);
        Individual p2 = tournamentSelect(parentsPop);

        // 2 кроссовер
        // с вероятностью 90% скрещиваем родителей, иначе просто копируем первого
        Individual child;
        if (ThreadLocalRandom.current().nextDouble() < CROSSOVER_RATE) {
            child = arithmeticCrossover(p1, p2);
        } else {
            child = p1.copy(); // нет кроссовера
        }

        // 3 мутация
        gaussianMutation(child, mutationScale);

        // 4 оценка (сразу вычисляем fitness)
        child.evaluate();

        return child;
    }

    // Генетические операторы
    // выбираем 4 случайных, берем самого "приспособленного"
    private static Individual tournamentSelect(List<Individual> pop) {
        Individual best = null;
        for (int i = 0; i < TOURNAMENT_SIZE; i++) {
            int idx = ThreadLocalRandom.current().nextInt(pop.size());
            Individual ind = pop.get(idx);
            if (best == null || ind.getFitness() > best.getFitness()) {
                best = ind;
            }
        }
        return best; // возвращаем ссылку, но для мутации будем делать копию в методе создания
    }

    // ребенок = среднее арифметическое между родителями
    private static Individual arithmeticCrossover(Individual p1, Individual p2) {
        double alpha = 0.5; // равномерный арифметический кроссовер
        double newX1 = alpha * p1.x1 + (1 - alpha) * p2.x1;
        double newX2 = alpha * p1.x2 + (1 - alpha) * p2.x2;
        return new Individual(newX1, newX2);
    }

    private static void gaussianMutation(Individual ind, double scale) {
        if (ThreadLocalRandom.current().nextDouble() < MUTATION_RATE) {
            // амплитуда мутации зависит от поколения (scale) и разброса области поиска
            // в начале большие мутации, чтобы исследовать всю область поиска
            // в середине средние мутации, чтобы уточнить поиск вокруг хороших решений
            // в конце слабые мутации, чтобы делать тонкую настройку найденного решения
            double sigma = (MAX_X - MIN_X) * 0.1 * scale;

            ind.x1 += ThreadLocalRandom.current().nextGaussian() * sigma;
            ind.x2 += ThreadLocalRandom.current().nextGaussian() * sigma;

            // проверка границ
            ind.clamp();
        }
    }

    // вспомогательные методы вывода

    private static void printProgress(int gen, Individual best) {
        System.out.printf("%d\t| %.4f\t\t| (%.2f, %.2f)\n",
                gen, best.funcValue, best.x1, best.x2);
    }

    private static void printSummary(Individual seq, double timeSeq, Individual par, double timePar) {
        System.out.println("\n ~~~~~~~~~~~~~~~");
        System.out.println("\n ИТОГИ ");
        System.out.println("Известный оптимум: " + KNOWN_OPT_VAL + " в точке (" + KNOWN_OPT_X1 + ", " + KNOWN_OPT_X2 + ")");

        System.out.println("\n--- Последовательный алгоритм ---");
        System.out.printf("Лучшее значение: %.4f\n", seq.funcValue);
        System.out.printf("Точка: (%.4f, %.4f)\n", seq.x1, seq.x2);
        System.out.printf("Время: %.2f ms\n", timeSeq);

        System.out.println("\n--- Параллельный алгоритм ---");
        System.out.printf("Лучшее значение: %.4f\n", par.funcValue);
        System.out.printf("Точка: (%.4f, %.4f)\n", par.x1, par.x2);
        System.out.printf("Время: %.2f ms\n", timePar);

        System.out.println("\nУскорение: " + String.format("%.2f", timeSeq / timePar) + "x");

        double error = Math.abs(par.funcValue - KNOWN_OPT_VAL);
        System.out.println("Абсолютная ошибка (parallel): " + String.format("%.4f", error));
        if (error < 10.0) {
            System.out.println("РЕЗУЛЬТАТ: Оптимум успешно найден!");
        } else {
            System.out.println("РЕЗУЛЬТАТ: Найден локальный экстремум (функция сложная).");
        }
    }

    // класс особи
    // каждая особь - это точка (x1,x2) со значением функции и "приспособленностью"
    static class Individual {
        double x1;
        double x2;
        double funcValue; // значение f(x)
        double fitness;   // значение приспособленности (для максимизации)

        public Individual(double x1, double x2) {
            this.x1 = x1;
            this.x2 = x2;
            clamp(); // гарантируем границы при создании
        }

        public static Individual createRandom() {
            double x1 = ThreadLocalRandom.current().nextDouble(MIN_X, MAX_X);
            double x2 = ThreadLocalRandom.current().nextDouble(MIN_X, MAX_X);
            Individual ind = new Individual(x1, x2);
            ind.evaluate();
            return ind;
        }

        public void evaluate() {
            // функция Эггхолдера
            this.funcValue = -(x2 + 47) * Math.sin(Math.sqrt(Math.abs(x1 / 2 + x2 + 47)))
                    - x1 * Math.sin(Math.sqrt(Math.abs(x1 - (x2 + 47))));

            // так как мы ищем минимум функции (значение -959),
            // а ГА обычно максимизирует Fitness, инвертируем знак.
            this.fitness = -this.funcValue;
        }

        // ограничение координат областью поиска
        public void clamp() {
            if (x1 < MIN_X) x1 = MIN_X;
            if (x1 > MAX_X) x1 = MAX_X;
            if (x2 < MIN_X) x2 = MIN_X;
            if (x2 > MAX_X) x2 = MAX_X;
        }

        public double getFitness() {
            return fitness;
        }

        public Individual copy() {
            Individual clone = new Individual(this.x1, this.x2);
            clone.funcValue = this.funcValue;
            clone.fitness = this.fitness;
            return clone;
        }
    }
}