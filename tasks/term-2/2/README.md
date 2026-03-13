Bonus: multipart form data ! порт 8081

curl -i -X GET http://localhost:8081/hello
HTTP/1.1 200 OK
Content-Type: text/plain; charset=utf-8
Content-Length: 15

Hello from GET!

curl -i -X POST http://localhost:8081/data -d "Привет, сервер!"
HTTP/1.1 201 Created
Content-Type: text/plain; charset=utf-8
Content-Length: 42

Data received: Привет, сервер!

curl -i -X POST http://localhost:8081/upload -F "username=Sergey" -F "document=My Secret Text"
HTTP/1.1 200 OK
Content-Type: text/plain; charset=utf-8
Content-Length: 66

Received fields: Username = Sergey; Document text = My Secret Text


curl -i -X DELETE http://localhost:8081/delete
HTTP/1.1 200 OK
Content-Type: text/plain; charset=utf-8
Content-Length: 12

Item deleted

curl -i -X GET http://localhost:8081/error      
HTTP/1.1 404 Not Found
Content-Type: text/plain; charset=utf-8
Content-Length: 15

Route not found

