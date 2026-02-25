package server;

import java.io.Serializable;

public class ImageRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private byte[] imageData;

    public ImageRequest(byte[] imageData) {
        this.imageData = imageData;
    }

    public byte[] getImageData() {
        return imageData;
    }
}