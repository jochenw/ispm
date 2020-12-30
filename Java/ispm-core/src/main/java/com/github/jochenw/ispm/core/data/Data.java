package com.github.jochenw.ispm.core.data;

import java.util.Map;

import com.softwareag.util.IDataMap;
import com.wm.data.IData;
import com.wm.data.IDataCursor;
import com.wm.data.IDataFactory;
import com.wm.data.IDataUtil;

/** Utility class, which provides static methods for working with Data objects.
 */
public class Data {
	public static Object getValue(Map<String,Object> pMap, String pParameter) {
		return pMap.get(pParameter);
	}

	public static String getString(Map<String,Object> pMap, String pParameter) {
		return getString(pMap, pParameter, pParameter);
	}

	public static String getString(Map<String,Object> pMap, String pParameter, String pErrorParameter) {
		final Object value = getValue(pMap, pParameter);
		if (value == null) {
			return null;
		} else if (value instanceof String) {
			return (String) value;
		} else {
			throw new IllegalArgumentException("Expected String value for parameter "
					+ pErrorParameter + ", got " + value.getClass().getName());
		}
	}

	public static IData getIData(Map<String,Object> pMap, String pParameter) {
		return getIData(pMap, pParameter, pParameter);
	}

	public static IData getIData(Map<String,Object> pMap, String pParameter, String pErrorParameter) {
		final Object value = getValue(pMap, pParameter);
		if (value == null) {
			return null;
		} else if (value instanceof IData) {
			return (IData) value;
		} else {
			throw new IllegalArgumentException("Expected IData value for parameter "
					+ pErrorParameter + ", got " + value.getClass().getName());
		}
	}

	public static IDataMap getIDataMap(Map<String,Object> pMap, String pParameter) {
		return getIDataMap(pMap, pParameter, pParameter);
	}

	public static IDataMap getIDataMap(Map<String,Object> pMap, String pParameter, String pErrorParameter) {
		final Object value = getValue(pMap, pParameter);
		if (value == null) {
			return null;
		} else if (value instanceof IData) {
			return new IDataMap((IData) value);
		} else {
			throw new IllegalArgumentException("Expected IData value for parameter "
					+ pErrorParameter + ", got " + value.getClass().getName());
		}
	}

	public static String requireString(Map<String,Object> pMap, String pParameter) {
		return requireString(pMap, pParameter, pParameter);
	}

	public static String requireString(Map<String,Object> pMap, String pParameter, String pErrorParameter) {
		final String s = getString(pMap, pParameter, pErrorParameter);
		if (s == null) {
			throw new NullPointerException("Missing parameter: " + pErrorParameter);
		}
		if (s.length() == 0) {
			throw new IllegalArgumentException("Empty parameter: " + pErrorParameter);
		}
		return s;
	}

	public static IData requireIData(Map<String,Object> pMap, String pParameter) {
		return requireIData(pMap, pParameter, pParameter);
	}

	public static IData requireIData(Map<String,Object> pMap, String pParameter, String pErrorParameter) {
		final IData data = getIData(pMap, pParameter, pErrorParameter);
		if (data == null) {
			throw new NullPointerException("Missing parameter: " + pErrorParameter);
		}
		return data;
	}

	public static IDataMap requireIDataMap(Map<String,Object> pMap, String pParameter) {
		return requireIDataMap(pMap, pParameter, pParameter);
	}

	public static IDataMap requireIDataMap(Map<String,Object> pMap, String pParameter, String pErrorParameter) {
		final IData data = getIData(pMap, pParameter, pErrorParameter);
		if (data == null) {
			throw new NullPointerException("Missing parameter: " + pErrorParameter);
		}
		return new IDataMap(data);
	}

	public static DocumentList list(IData... pDocuments) {
		if (pDocuments == null  ||  pDocuments.length == 0) {
			return new DocumentList();
		} else if (pDocuments.length == 1) {
			final DocumentList list = new DocumentList();
			list.add(pDocuments[0]);
			return list;
		} else {
			final DocumentList list = new DocumentList(pDocuments.length);
			for (IData document : pDocuments) {
				list.add(document);
			}
			return list;
		}
	}

	public static StringList list(String... pStrings) {
		if (pStrings == null  ||  pStrings.length == 0) {
			return new StringList();
		} else if (pStrings.length == 1) {
			final StringList list = new StringList();
			list.add(pStrings[0]);
			return list;
		} else {
			final StringList list = new StringList(pStrings.length);
			for (String string : pStrings) {
				list.add(string);
			}
			return list;
		}
	}

	public static IDataMap asIDataMap(Object... pValues) {
		final IDataMap map = new IDataMap();
		if (pValues != null) {
			for (int i = 0;  i < pValues.length;  i += 2) {
				map.put((String) pValues[i], pValues[i+1]);
			}
		}
		return map;
	}

	public static IData asIData(Object... pValues) {
		final IData data = IDataFactory.create();
		if (pValues != null) {
			final IDataCursor crsr = data.getCursor();
			for (int i = 0;  i < pValues.length;  i += 2) {
				IDataUtil.put(crsr, (String) pValues[i], pValues[i+1]);
			}
			crsr.destroy();
		}
		return data;
	}
}
