@echo off
set JAVA_HOME=C:\Program Files\Microsoft\jdk-21.0.10.7-hotspot
set PATH=%JAVA_HOME%\bin;%PATH%
echo === Lab 6: KNN Classifier Tests ===
call gradlew.bat clean test --no-build-cache
