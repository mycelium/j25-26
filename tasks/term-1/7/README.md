На основе тестирования был определен оптимальный порог для использования многопоточности PARALLEL_THRESHOLD = 130 (примерно, иногда граница чуть смещается в большую или меньшую сторону. Но для 130 с точностью до милисекунд время равное.).
При размерах матриц ниже 130×130 накладные расходы на создание и синхронизацию потоков превышают выгоду от параллелизма.

Количество потоков: min(availableProcessors, rows).
Процессоры: 4 ядра.
Порог многопоточности: 130×130.

Закомментировано в коде для более просматриваемого вывода, но логично сделать так:
1. Для матриц < 130×130 - однопоточная версия.
2. Для матриц ≥ 130×130 - многопоточная версия.
3. При 1 доступном процессоре - однопоточная версия.


Matrix size: 10*10
Original             - Time:    0 ms
Parallel             - Time:    1 ms --параллельное медленнее.
Speedup: NaNx 

Matrix size: 100*100
Original             - Time:    3 ms
Parallel             - Time:    5 ms --параллельное умножение занимает больше времени из-за затрат на создание потоков и объединение.
Speedup: 0,60x

Matrix size: 130*130
Original             - Time:    1 ms
Parallel             - Time:    1 ms
Speedup: 1,00x --с этого момента есть смысл использовать параллелизацию

Matrix size: 150*150
Original             - Time:    2 ms
Parallel             - Time:    3 ms
Speedup: 1,50x

Matrix size: 160*160
Original             - Time:    5 ms
Parallel             - Time:    4 ms
Speedup: 1,25x

Matrix size: 200*200
Original             - Time:   11 ms
Parallel             - Time:    4 ms
Speedup: 2,75x

Matrix size: 500*500
Original             - Time:  244 ms
Parallel             - Time:   61 ms
Speedup: 4,00x

Matrix size: 1000*1000
Original             - Time: 1385 ms
Parallel             - Time:  379 ms
Speedup: 3,65x

Matrix size: 1500*1500
Original             - Time: 3921 ms
Parallel             - Time: 1366 ms
Speedup: 2,87x

Максимальное ускорение достигает 4.00x.
Стабильное ускорение в диапазоне 2.87x-4.00x.
