package org.numbers;

import org.deeplearning4j.datasets.iterator.impl.MnistDataSetIterator;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;

public class Data {

    private static final int BATCH_SIZE = 64;
    private static final int SEED = 123;

    public DataSetIterator loadTrain() throws Exception {
        return new MnistDataSetIterator(BATCH_SIZE, true, SEED);
    }

    public DataSetIterator loadTest() throws Exception {
        return new MnistDataSetIterator(BATCH_SIZE, false, SEED);
    }
}
