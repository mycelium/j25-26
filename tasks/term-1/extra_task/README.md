CNN для классификации MNIST с использованием Java Vector API

- Входной слой: 28×28 = 784 нейрона (пиксели MNIST изображения)
- Выходной слой: 10 нейронов (цифры 0-9)
- Функция активации - Softmax
- Функция потерь - Cross-Entropy
- Оптимизатор - SGD (Stochastic Gradient Descent)

Требования к системе: JDK 16 или выше (требуется для Vector API)

## Компиляция и запуск

Компиляция:
```bash
javac --add-modules jdk.incubator.vector MNISTVectorCNN.java
```

Запуск:
```bash
java --add-modules jdk.incubator.vector MNISTVectorCNN
```

1. Синтетические данные - используются упрощенные паттерны вместо реальных MNIST изображений
2. Упрощенная архитектура - только полносвязный слой без сверточных слоев
3. Базовый оптимизатор - простой SGD
