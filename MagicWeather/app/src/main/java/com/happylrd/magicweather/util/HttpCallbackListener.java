package com.happylrd.magicweather.util;

public interface HttpCallbackListener {
    void onFinish(String response);

    void onError(Exception e);
}
