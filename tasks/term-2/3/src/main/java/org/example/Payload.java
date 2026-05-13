package org.example;

public class Payload {
    public String data;
    public int    n;

    public Payload() {}

    public Payload(String data, int n) {
        this.data = data;
        this.n    = n;
    }

    public String data() { return data; }
    public int    n()    { return n; }
}
