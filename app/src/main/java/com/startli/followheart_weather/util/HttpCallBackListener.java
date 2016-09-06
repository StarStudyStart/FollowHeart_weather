package com.startli.followheart_weather.util;

public interface HttpCallBackListener {
	void onFinish(String response);

	void onError(Exception e);
}
