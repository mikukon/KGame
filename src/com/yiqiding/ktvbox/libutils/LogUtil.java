package com.yiqiding.ktvbox.libutils;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.util.Log;

public class LogUtil {
	private static final String PROP_TAG = "log_dog";
	
	public static final int LEVEL_V = 2;
	public static final int LEVEL_D = 3;
	public static final int LEVEL_I = 4;
	public static final int LEVEL_W = 5;
	public static final int LEVEL_E = 6;
	public static final int LEVEL_S = 1;
	
	public static int DEBUG_LEVEL = LEVEL_I;
	
	public static void d(String TAG, String method, String msg) {
		Log.d(TAG, "[" + method + "]" + msg);
	}
	
	public static int d(String tag, String msg, Throwable tr)
	{
		return Log.d(tag, msg, tr);
	}
	
	public static int e(String tag, String msg, Throwable tr)
	{
		return Log.e(tag, msg, tr);
	}
	
	public static int w(String tag, String msg, Throwable tr)
	{
		return Log.w(tag, msg, tr);
	}
	
	public static void i(String TAG, String msg) {
		Log.i(TAG, msg);
	}
	
	public static void w(String TAG, String msg) {
		Log.w(TAG, msg);
	}
	
	public static void v(String TAG, String msg) {
		Log.v(TAG, msg);
	}
	
	public static void d(String TAG, String msg){
		if (DEBUG_LEVEL <= LEVEL_D || Log.isLoggable(PROP_TAG, Log.DEBUG)) {
			Log.d(TAG, "[" + getFileLineMethod() + "]" + msg);
		}
	}
	
	public static void d(String msg){
		if (DEBUG_LEVEL <= LEVEL_D || Log.isLoggable(PROP_TAG, Log.DEBUG)) {
			Log.d(getFileName(), "[" + getLineMethod() + "]" + msg);
		}
	}
	
	public static void i(String msg){
		if (DEBUG_LEVEL <= LEVEL_I || Log.isLoggable(PROP_TAG, Log.INFO)) {
			Log.i(getFileName(), "[" + getLineMethod() + "]" + msg);
		}
	}
	
	public static void w(String msg){
		if (DEBUG_LEVEL <= LEVEL_W || Log.isLoggable(PROP_TAG, Log.WARN)) {
			Log.w(getFileName(), "[" + getLineMethod() + "]" + msg);
		}
	}
	
	public static void v(String msg){
		if (DEBUG_LEVEL <= LEVEL_V || Log.isLoggable(PROP_TAG, Log.VERBOSE)) {
			Log.v(getFileName(), "[" + getLineMethod() + "]" + msg);
		}
	}
	
	public static void e(String msg){
		if (DEBUG_LEVEL <= LEVEL_E || Log.isLoggable(PROP_TAG, Log.ERROR)) {
			Log.e(getFileName(), getLineMethod() + msg);
		}
	}
	
	public static void e(String TAG, String msg){
		if (DEBUG_LEVEL <= LEVEL_E || Log.isLoggable(PROP_TAG, Log.ERROR)) {
			Log.e(TAG, getLineMethod() + msg);
		}
	}

	public static String getFileLineMethod() {
		StackTraceElement traceElement = ((new Exception()).getStackTrace())[2];
		StringBuffer toStringBuffer = new StringBuffer("[")
				.append(traceElement.getFileName()).append(" | ")
				.append(traceElement.getLineNumber()).append(" | ")
				.append(traceElement.getMethodName()).append("]");
		return toStringBuffer.toString();
	}
	
	public static String getLineMethod() {
		StackTraceElement traceElement = ((new Exception()).getStackTrace())[2];
		StringBuffer toStringBuffer = new StringBuffer("[")
				.append(traceElement.getLineNumber()).append(" | ")
				.append(traceElement.getMethodName()).append("]");
		return toStringBuffer.toString();
	}

	public static String getFileName() {
		StackTraceElement traceElement = ((new Exception()).getStackTrace())[2];
		return traceElement.getFileName();
	}

	public static String _FUNC_() {
		StackTraceElement traceElement = ((new Exception()).getStackTrace())[1];
		return traceElement.getMethodName();
	}

	public static int _LINE_() {
		StackTraceElement traceElement = ((new Exception()).getStackTrace())[1];
		return traceElement.getLineNumber();
	}

	public static String _TIME_() {
		Date now = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		return sdf.format(now);
	}
}
