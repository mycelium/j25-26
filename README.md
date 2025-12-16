# K-Nearest Neighbors (KNN) Classifier - Laboratory Work #6

This Java application implements a K-Nearest Neighbors classifier for classifying 2D points into different classes.

## Features

- **Point Classification**: Classifies 2D points using the KNN algorithm
- **Data Generation**: Generates sample training data with multiple classes
- **Visualization**: Creates PNG plots showing training data and classification results
- **Demo Classification**: Demonstrates classification with sample test points

## Classes

### Core Classes
- `Point`: Represents a 2D point with coordinates and class label
- `KNNClassifier`: Implements the KNN classification algorithm
- `DataGenerator`: Generates sample training and test data
- `PlotGenerator`: Creates PNG visualizations using Java2D
- `App`: Main application class with demo and interactive mode

## Usage

### Running the Application

```bash
./gradlew run
```

The application will:
1. Generate training data with 3 classes (A, B, C)
2. Create a KNN classifier with k=3
3. Generate and classify test points
4. Create visualization plots (`training_data.png` and `classification_results.png`)
5. Demonstrate classification with sample test points

## Algorithm Details

The KNN classifier works by:
1. Calculating Euclidean distance from the test point to all training points
2. Finding the k nearest neighbors
3. Using majority voting to determine the predicted class

## Visualization

Two PNG files are generated:
- `training_data.png`: Shows the training data points colored by class
- `classification_results.png`: Shows training data plus classified test points

## Testing

Run unit tests:
```bash
./gradlew test
```

## Requirements

- Java 23
- Gradle (included in project)

## Implementation Notes

- No external ML libraries are used - pure Java implementation
- Uses Java2D for PNG generation
- Thread-safe for single-threaded usage
- Deterministic results with fixed random seed
