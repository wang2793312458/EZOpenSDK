package com.videogo;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.Thread.UncaughtExceptionHandler;

import android.content.Context;
import android.util.Log;

public class CustomExceptionHandler implements UncaughtExceptionHandler {
	private UncaughtExceptionHandler defaultUEH;
	private Context context;
	
	public CustomExceptionHandler(Context context) {
		this.context = context;
		this.defaultUEH = Thread.getDefaultUncaughtExceptionHandler();
	}
	
	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
		Log.e("com.videogo", ex.getMessage());
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ex.printStackTrace(new PrintStream(bos));
		try {
			bos.flush();
			bos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		Log.e("com.videogo", ex.getStackTrace().toString());
		Log.e("com.videogo", bos.toString());
		defaultUEH.uncaughtException(thread, ex);;
	}

}
