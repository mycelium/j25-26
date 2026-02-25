package server;

import java.io.Serializable;

public class DigitResponse implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private int recognizedDigit;

    public DigitResponse(int recognizedDigit) {
        this.recognizedDigit = recognizedDigit;
    }

    public int getRecognizedDigit() {
        return recognizedDigit;
    }
}