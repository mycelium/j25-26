Если при первом запуске возникает ошибка, связанная с CUDA, необходимо очистить локальные кэши и запустить программу с явным флагом CPU:

### Linux / macOS
```bash
rm -rf ~/.gradle/caches
rm -rf ~/.javacpp/cache

gradle clean build -Dorg.nd4j.linalg.defaultbackend=cpu
gradle run -Dorg.nd4j.linalg.defaultbackend=cpu
```
### Windows
```
rmdir /s /q "$env:USERPROFILE\.gradle\caches"
rmdir /s /q "$env:USERPROFILE\.javacpp\cache"
gradle clean build -Dorg.nd4j.linalg.defaultbackend=cpu
gradle run -Dorg.nd4j.linalg.defaultbackend=cpu
```
Вместо $env:USERPROFILE нужно подставить путь к домашней папке, например  C:\Users\myName
