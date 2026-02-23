package org.example;

import java.io.Serializable;

public class Request implements Serializable {
    
    private String reviewText;

    public Request(String reviewText) {
        this.reviewText = reviewText;
    }

    public String getReviewText() {
        return reviewText;
    }
}