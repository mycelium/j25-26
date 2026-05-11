# Load Testing Report

## Results

| req | Virtual + own parser | Virtual + GSON | Classic + own parser | Classic + GSON |
|-----|---------------------|----------------|---------------------|----------------|
| Request-1 (I/O) | 131.58 ms | error | 105.86 ms | error |
| Request-2 (CPU) | 3.96 ms | error | 70.04 ms | error |

## Observations

1. Virtual threads show better CPU performance (3.96 ms vs 70.04 ms)
2. Own parser works reliably in all configurations
3. GSON integration requires additional configuration
4. 0% error rate for own parser configurations

## How to run

```bash
cd src
javac -cp "gson-2.10.1.jar" loadtest/*.java httpserver/*.java jsonparser/*.java
java -cp ".;gson-2.10.1.jar" loadtest.TestServer false false
java -cp ".;gson-2.10.1.jar" loadtest.LoadTester false false
