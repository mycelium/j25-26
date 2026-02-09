По нескольким прогонам программы было выяснено, что самым оптимальным количеством потоков для выполнения программы является 
количество доступных логических процессоров.

Для наглядности вывел чуть меньшее и чуть большее количество потоков при 8 доступных логических процессоров, 
а также количество потоков равное количеству строк в матрице:
```aiignore
Count of processors: 8
for 6 threads:
	Parallel execution time: 1324 ms
for 7 threads:
	Parallel execution time: 1175 ms
for 8 threads:
	Parallel execution time: 1097 ms
for 9 threads:
	Parallel execution time: 1148 ms
for 10 threads:
	Parallel execution time: 1155 ms
for 1000 threads:
	Parallel execution time: 1424 ms

Multiply execution time: 4155 ms
MultiplyOpt execution time: 4055 ms
```

Как видно самое оптимальное вычисление происходит когда количество потоков равно количеству доступных процессоров.