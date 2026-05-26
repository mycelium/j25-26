package com.httpserver;

public class WebServerSettings {
    private String address = "localhost";
    private int portNumber = 8081;
    private int poolSize = 10;
    private boolean virtualMode = false;

    public WebServerSettings setAddress(String address) {
        this.address = address;
        return this;
    }

    public WebServerSettings setPortNumber(int portNumber) {
        this.portNumber = portNumber;
        return this;
    }

    public WebServerSettings setPoolSize(int poolSize) {
        this.poolSize = poolSize;
        return this;
    }

    public WebServerSettings setVirtualMode(boolean virtualMode) {
        this.virtualMode = virtualMode;
        return this;
    }

    public String getAddress() { return address; }
    public int getPortNumber() { return portNumber; }
    public int getPoolSize() { return poolSize; }
    public boolean isVirtualMode() { return virtualMode; }
}