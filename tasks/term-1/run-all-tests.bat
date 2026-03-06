@echo off
set JAVA_HOME=C:\Program Files\Microsoft\jdk-21.0.10.7-hotspot
set PATH=%JAVA_HOME%\bin;C:\Program Files (x86)\sbt\bin;%PATH%

set ROOT=%~dp0
set PASS=0
set FAIL=0

echo  Running all lab tests

echo.
echo [Lab 1] Matrix Multiplication
cd /d "%ROOT%1"
call gradlew.bat test >"%ROOT%1\test-output.log" 2>&1
if %ERRORLEVEL%==0 (echo   PASSED & set /a PASS+=1) else (echo   FAILED - see 1\test-output.log & set /a FAIL+=1)

echo.
echo [Lab 2] Recursive Functions (sbt)
cd /d "%ROOT%2"
call sbt test >"%ROOT%2\test-output.log" 2>&1
if %ERRORLEVEL%==0 (echo   PASSED & set /a PASS+=1) else (echo   FAILED - see 2\test-output.log & set /a FAIL+=1)

echo.
echo [Lab 3] Functional Sets (sbt)
cd /d "%ROOT%3"
call sbt test >"%ROOT%3\test-output.log" 2>&1
if %ERRORLEVEL%==0 (echo   PASSED & set /a PASS+=1) else (echo   FAILED - see 3\test-output.log & set /a FAIL+=1)

echo.
echo [Lab 4] Word Frequency Counter
cd /d "%ROOT%4"
call gradlew.bat test >"%ROOT%4\test-output.log" 2>&1
if %ERRORLEVEL%==0 (echo   PASSED & set /a PASS+=1) else (echo   FAILED - see 4\test-output.log & set /a FAIL+=1)

echo.
echo [Lab 5] Sentiment Analyzer
cd /d "%ROOT%5"
call gradlew.bat test >"%ROOT%5\test-output.log" 2>&1
if %ERRORLEVEL%==0 (echo   PASSED & set /a PASS+=1) else (echo   FAILED - see 5\test-output.log & set /a FAIL+=1)

echo.
echo [Lab 6] KNN Classifier
cd /d "%ROOT%6"
call gradlew.bat test >"%ROOT%6\test-output.log" 2>&1
if %ERRORLEVEL%==0 (echo   PASSED & set /a PASS+=1) else (echo   FAILED - see 6\test-output.log & set /a FAIL+=1)

echo.
echo [Lab 7] Parallel Matrix Multiplication
cd /d "%ROOT%7"
call gradlew.bat test >"%ROOT%7\test-output.log" 2>&1
if %ERRORLEVEL%==0 (echo   PASSED & set /a PASS+=1) else (echo   FAILED - see 7\test-output.log & set /a FAIL+=1)

echo.
echo [Lab 8] MNIST Classifier
cd /d "%ROOT%8"
call gradlew.bat test >"%ROOT%8\test-output.log" 2>&1
if %ERRORLEVEL%==0 (echo   PASSED & set /a PASS+=1) else (echo   FAILED - see 8\test-output.log & set /a FAIL+=1)

echo.
echo  Results: %PASS% passed, %FAIL% failed
cd /d "%ROOT%"
