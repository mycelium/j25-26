package org.example.util;

import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.example.config.ModelConfig;
import org.deeplearning4j.util.ModelSerializer;

import java.io.File;

/**
 * Сохранение и загрузка модели.
 */
public class ModelIO {

    /**
     * Сохраняет модель в файл.
     *
     * @param model обученная модель
     */
    public static void save(MultiLayerNetwork model) throws Exception {
        ModelSerializer.writeModel( model, new File(ModelConfig.MODEL_PATH), true);
    }

    /**
     * Загружает модель из файла.
     *
     * @return загруженная модель
     */
    public static MultiLayerNetwork load() throws Exception {
        return ModelSerializer.restoreMultiLayerNetwork( new File(ModelConfig.MODEL_PATH));
    }
}