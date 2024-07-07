package com.B0BO.smart_internship_assistant;

public class Result<DATA> {

    private static final int HTTP_REQUEST_SUCCESS_CODE = 600;

    public int code;
    public String msg;
    public DATA data;


    public boolean isSuccessful() {
        return code == HTTP_REQUEST_SUCCESS_CODE;
    }

    @Override
    public String toString() {
        return "Result{" +
                "code=" + code +
                ", message='" + msg + '\'' +
                ", data=" + data +
                '}';
    }

}


