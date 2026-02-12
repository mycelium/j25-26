package ru.spbstu.hsai.java.l0205;

import java.io.Serializable;

public class Response implements Serializable{

	private String textResponse;
	private boolean resultFlag;
	
	public Response(String textResponse, boolean resultFlag) {
		super();
		this.textResponse = textResponse;
		this.resultFlag = resultFlag;
	}

	public String getTextResponse() {
		return textResponse;
	}

	public void setTextResponse(String textResponse) {
		this.textResponse = textResponse;
	}

	public boolean isResultFlag() {
		return resultFlag;
	}

	public void setResultFlag(boolean resultFlag) {
		this.resultFlag = resultFlag;
	}
	
	
	
}
