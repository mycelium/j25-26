package org.example;

public class Result {
    public String status;
    public int    value;

    public Result() {}

    public Result(String status, int value) {
        this.status = status;
        this.value  = value;
    }

    public String status() { return status; }
    public int    value()  { return value; }
}
