package com.xcompwiz.lookingglass.log;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.xcompwiz.lookingglass.LookingGlass;

public final class LoggerUtils {
	private static Logger	log	= null;

	/**
	 * Configure the logger
	 */
	private static void configureLogging() {
		log = LogManager.getLogger(LookingGlass.MODID);
	}

	public static void log(Level level, String message, Object... params) {
		if (log == null) {
			configureLogging();
		}
		if (message == null) {
			log.log(level, "Attempted to log null message.");
		} else {
			try {
				message = String.format(message, params);
			} catch (Exception e) {
			}
			log.log(level, message);
		}
	}

	public static void info(String message, Object... params) {
		log(Level.INFO, message, params);
	}

	public static void warn(String message, Object... params) {
		log(Level.WARN, message, params);
	}

	public static void error(String message, Object... params) {
		log(Level.ERROR, message, params);
	}

	public static void debug(String message, Object... params) {
		//NOPE
	}
}
