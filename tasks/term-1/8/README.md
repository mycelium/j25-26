# HSAI 25-26 Java course - 1

## 8. Java: Image Classification with DeepLearning4j (DL4J)

Programm uses `resources\` subfolder in `app\src\main\` directory. First folder in it is `MNIST dataset` which contains, as name suggests, MNIST dataset. Second folder in it is `test data` which contains test data. 

Example test data located in `test data` folder are: images of two, six and seven named accordingly.

## How to run

Download MNIST dataset and put it in the resources folder, as described earlier. 

In the `String[] imageExamples` variable you can change what test data image names will be used (or even used at all), by default value is `{"two", "six", "seven"}`.

There are class consts `private static final` to tweek the configuration: 
- `String PROJECT_PATH` (path to the `src\main` folder of the project, by default: `"tasks\\term-1\\8\\app\\src\\main\\"`)
- `String EXAMPLE_IMAGE_FORMAT` (format of the example image format, by default: `".png"`)
- `String EXAMPLE_IMAGE_PATH` (path to the test folder with example images to classify, by default: `PROJECT_PATH + "resources\\test data\\"`)
- `String MNIST_PATH` (path to the folder with MNIST dataset, by default: `PROJECT_PATH + "resources\\MNIST dataset\\"`).

## `MNIST dataset` format

- **Files**: ending with -ubyte
- **Train images**: `train-images.idx3-ubyte`
- **Train images labels**: `train-labels.idx1-ubyte`
- **Test images**: `t10k-images.idx3-ubyte`
- **Test images labels**: `t10k-labels.idx1-ubyte`

## `test data` images format

- **Size**: 28×28px
- **Color**: grayscale
- **Background**: dark (black)
- **Number**: light (white)
- **Image** format: .png
