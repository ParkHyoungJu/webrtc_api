package com.example.webrtc.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogUtils {

	protected final static Logger logger = LoggerFactory.getLogger(LogUtils.class);
	protected final static boolean isLoging = true;
	
	public static String getLogTag() {
		int level = 3;
		
		StackTraceElement trace = Thread.currentThread().getStackTrace()[level];
		
		String fullClassName = trace.getClassName();
	    String className = fullClassName.substring(fullClassName.lastIndexOf(".") + 1);
	    // System.out.println("fullClassName["+level+"] = " + fullClassName);
	    // System.out.println("className["+level+"] = " + className);
	    
	    while( className.equals("LogUtils") ) {
	    	level++;
	    	trace = Thread.currentThread().getStackTrace()[level];
	    	fullClassName = trace.getClassName();
	    	className = fullClassName.substring(fullClassName.lastIndexOf(".") + 1);

		    // System.out.println("fullClassName["+level+"] = " + fullClassName);
		    // System.out.println("className["+level+"] = " + className);
	    }

	    String methodName = trace.getMethodName();
	    int lineNumber = trace.getLineNumber();

	    // String prefix = DateTimeUtils.getFormatedDT("yyyy-MM-dd HH:mm:ss") + " [" + className + "." + methodName + "():" + lineNumber + "] ";
	    String prefix = "[" + className + "." + methodName + "():" + lineNumber + "] ";
	    return prefix;
	}

	public static void dStart() {
		if (!isLoging) return;
		logger.info(getLogTag() + ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> START LOG" );
	}

	public static void dEnd() {
		if (!isLoging) return;
		logger.info(getLogTag() + "<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< END LOG" );
	}
	
	public static void d(String format, Object... args) {
		if (!isLoging) return;
		logger.debug(getLogTag() + String.format(format, args));
	}

	public static void dLine() {
		if (!isLoging) return;
		logger.debug(getLogTag() + "----------------------------------------------------");
	}

	public static void i(String format, Object... args) {
		if (!isLoging) return;
		logger.info(getLogTag() + String.format(format, args));
	}

	public static void e(Exception paramException) {
		if (!isLoging || paramException==null) return;
		logger.error(getLogTag(), paramException.getMessage());
	}

	public static void e(String paramString) {
		if (!isLoging || paramString==null) return;
		logger.error(getLogTag() + paramString);
	}

	public static void memory() {
		if (!isLoging) return;
		logger.info(getLogTag() + "totalMemory : "
				+ Runtime.getRuntime().totalMemory() + ", maxMemory : "
				+ Runtime.getRuntime().maxMemory() + ", freeMemory :"
				+ Runtime.getRuntime().freeMemory());
	}
}