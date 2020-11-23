package com.github.jochenw.ispm.core.svc;

import java.util.Map;

import com.softwareag.util.IDataMap;

public class Data {
	public static String getString(Map<String,Object> pMap, String pParameter) {
		return getString(pMap, pParameter, pParameter);
	}

	public static String getString(Map<String,Object> pMap, String pParameter, String pDisplayParameter) {
		Object value = pMap.get(pParameter);
		if (value == null) {
			return null;
		}
		if (value instanceof String) {
			return (String) value;
		} else {
			throw new IllegalArgumentException("Invalid value for parameter " + pDisplayParameter
					+ ": Expected String, got " + value.getClass().getName());
		}
	}

	public static String requireString(Map<String,Object> pMap, String pParameter) {
		return requireString(pMap, pParameter, pParameter);
	}

	public static String requireString(Map<String,Object> pMap, String pParameter, String pDisplayParameter) {
		String value = getString(pMap, pParameter, pDisplayParameter);
		if (value == null) {
			throw new NullPointerException("Missing parameter: " + pDisplayParameter);
		}
		return value;
	}

}
