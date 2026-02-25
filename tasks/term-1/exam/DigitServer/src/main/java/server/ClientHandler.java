package server;

import org.datavec.image.loader.NativeImageLoader;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private final MultiLayerNetwork model;

    public ClientHandler(Socket clientSocket, MultiLayerNetwork model) {
        this.clientSocket = clientSocket;
        this.model = model;
    }

    @Override
    public void run() {
        try (
            ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream())
        ) {
            
            ImageRequest request = (ImageRequest) in.readObject();
            byte[] imageBytes = request.getImageData();
            System.out.println("Получено изображение. Размер: " + imageBytes.length + " байт");

            int recognizedDigit;

            
            synchronized (model) {
                
                ByteArrayInputStream bais = new ByteArrayInputStream(imageBytes);
                
                
                NativeImageLoader loader = new NativeImageLoader(28, 28, 1);
                INDArray image = loader.asMatrix(bais);
                image.divi(255.0); 

               
                INDArray output = model.output(image);
                
                
                recognizedDigit = Nd4j.argMax(output, 1).getInt(0);
            }

            System.out.println("Нейросеть распознала цифру: " + recognizedDigit);

           
            DigitResponse response = new DigitResponse(recognizedDigit);
            out.writeObject(response);
            out.flush();

        } catch (Exception e) {
            System.err.println("Ошибка при работе с клиентом: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}