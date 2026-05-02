1. Компиляция
   javac *.java
2. Запуск сервера
   java Main

При успешном запуске сервер выведет:
anastasiasarzan@MacBook-Pro-Anastasia лаб2 % java Main
Using Fixed Thread Pool (10 threads)
Server listening on http://localhost:8080

4. GET запрос с параметрами
  anastasiasarzan@MacBook-Pro-Anastasia лаб2 % curl "http://localhost:8080/hello?name=Anastasia"
  Hello, Anastasia!%
5. POST запрос с телом
   anastasiasarzan@MacBook-Pro-Anastasia лаб2 % curl -X POST "http://localhost:8080/data" \
   -H "Content-Type: text/plain" \
   -d "my test data"

   Data received successfully%                                          
В окне сервера также появится лог: [POST] Received: my test data

6. Многопоточность
   anastasiasarzan@MacBook-Pro-Anastasia лаб2 % curl "http://localhost:8080/long" & curl "http://localhost:8080/long" & curl "http://localhost:8080/long"
  [1] 33858
  [2] 33859
  Done! Handled by: pool-1-thread-3Done! Handled by: pool-1-thread-4Done! Handled by: pool-1-thread-5[2]  + done       curl "http://localhost:8080/long"
  [1]  + done       curl "http://localhost:8080/long"

В окне сервера:
[LONG] Processing in thread: pool-1-thread-5
[LONG] Processing in thread: pool-1-thread-4
[LONG] Processing in thread: pool-1-thread-3

7. Multipart form data
   anastasiasarzan@MacBook-Pro-Anastasia лаб2 % curl -X POST "http://localhost:8080/upload" \
  -F "username=Anastasia" \
  -F "file=@test.txt"
  Field: username = Anastasia
  File: test.txt, size: 6 bytes

8. PUT запрос
   anastasiasarzan@MacBook-Pro-Anastasia лаб2 % curl -X PUT "http://localhost:8080/update" -d "full new data"
   Resource fully updated%

   В окне сервера: [PUT] Updated with: full new data

9. PATCH запрос
   anastasiasarzan@MacBook-Pro-Anastasia лаб2 % curl -X PATCH "http://localhost:8080/update" -d "partial update"
   Resource partially updated%   

   В окне сервера: [PATCH] Patched with: partial update

10. DELETE запрос
    anastasiasarzan@MacBook-Pro-Anastasia лаб2 % curl -X DELETE "http://localhost:8080/remove"

    В окне сервера: DELETE] Resource deleted












