package com.finke.pgtransit;

public class AsyncTaskResult<T> {

    private T mData;
    private Exception mException;

    public AsyncTaskResult(T data) {
        mData = data;
    }

    public T getData() {
        return mData;
    }

    public boolean hasException() {
        return mException != null;
    }

    public Exception getException() {
        return mException;
    }

    public void setException(Exception exception) {
        mException = exception;
    }

}
