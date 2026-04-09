## Quick Start

### 1. Clone and build

```powershell
git clone <repo-url>
cd http
.\gradlew.bat build
```

### 2. Run the demo server

```powershell
.\gradlew.bat run
```
### OR build JAR
```powershell
.\gradlew.bat clean jar
java -jar build\libs\http-1.0-SNAPSHOT.jar
```


Expected output:
```
=================================================
  HTTP server running
  Host:            localhost
  Port:            9090
  Threads:         4
  Virtual threads: false
=================================================
  curl.exe http://localhost:9090/hello
  curl.exe "http://localhost:9090/greet?name=World"
  curl.exe -X POST -d "hello" http://localhost:9090/echo
  curl.exe -X DELETE http://localhost:9090/delete
=================================================
  Press Ctrl+C to stop
=================================================
```

### 3. Test endpoints

Open a second PowerShell window and run:

```powershell
# GET
curl.exe http://localhost:9090/hello
# → Hello from server!

# GET with query parameter
curl.exe "http://localhost:9090/greet?name=World"
# → Hello, World!

# POST with body
curl.exe -X POST -d "hello" http://localhost:9090/echo
# → Body: hello

# PUT
curl.exe -X PUT -d "new data" http://localhost:9090/update
# → Updated with: new data

# PATCH
curl.exe -X PATCH -d "partial" http://localhost:9090/patch
# → Patched with: partial

# DELETE
curl.exe -X DELETE http://localhost:9090/delete
# → Deleted resource

```
### 4. Stop the server

Press `Ctrl+C` in the terminal where the server is running.

