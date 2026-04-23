Bonus: multipart form data

curl -i -X GET http://127.0.0.1:8082/api/ping
HTTP/1.1 200 OK
Connection: close
Content-Length: 29
Content-Type: application/json; charset=utf-8

{"status": "Server is alive"}

curl -i -X POST http://127.0.0.1:8082/api/store -d "Some data"
HTTP/1.1 201 Created
Connection: close
Content-Length: 33
Content-Type: application/json; charset=utf-8

{"result": "Successfully stored"}

curl -i -X POST http://127.0.0.1:8082/api/upload -F "title=MyDocument"
HTTP/1.1 200 OK
Connection: close
Content-Length: 26
Content-Type: application/json; charset=utf-8

{"savedTitle": "No Title"}
curl -i -X PUT http://127.0.0.1:8082/api/update -d "New full data"
HTTP/1.1 200 OK
Connection: close
Content-Length: 30
Content-Type: application/json; charset=utf-8

{"result": "Resource updated"}
curl -i -X PATCH http://127.0.0.1:8082/api/modify -d "Small change"
HTTP/1.1 200 OK
Connection: close
Content-Length: 30
Content-Type: application/json; charset=utf-8

{"result": "Resource patched"}