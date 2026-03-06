@echo off
set JAVA_HOME=C:\Program Files\Microsoft\jdk-21.0.10.7-hotspot
set PATH=%JAVA_HOME%\bin;C:\Program Files (x86)\sbt\bin;%PATH%
echo === Lab 3: Functional Sets Tests (sbt) ===
call sbt test
