package model.digitPredictionModel;

import org.datavec.api.io.filters.BalancedPathFilter;
import org.datavec.api.io.labels.ParentPathLabelGenerator;
import org.datavec.api.split.FileSplit;
import org.datavec.api.split.InputSplit;
import org.datavec.image.loader.NativeImageLoader;
import org.datavec.image.recordreader.ImageRecordReader;
import org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator;
import org.deeplearning4j.datasets.iterator.MultipleEpochsIterator;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.common.io.ClassPathResource;

public class DataLoader {
    private static final int NUM_CLASSES = 10;
    private static final int BATCH_SIZE = 64;

    public static class MnistData {
        private final DataSetIterator trainData;
        private final DataSetIterator testData;

        public MnistData(DataSetIterator trainData, DataSetIterator testData) {
            this.trainData = trainData;
            this.testData = testData;
        }

        public DataSetIterator getTrainData() {
            return trainData;
        }

        public DataSetIterator getTestData() {
            return testData;
        }
    }

    public MnistData loadData() throws Exception {
        var mnistTrain = new org.deeplearning4j.datasets.fetchers.MnistDataFetcher(true);
        var mnistTest = new org.deeplearning4j.datasets.fetchers.MnistDataFetcher(false);

        var trainIter = new org.deeplearning4j.datasets.iterator.impl.MnistDataSetIterator(BATCH_SIZE, true, 12345);
        var testIter = new org.deeplearning4j.datasets.iterator.impl.MnistDataSetIterator(BATCH_SIZE, false, 12345);

        return new MnistData(trainIter, testIter);
    }
}