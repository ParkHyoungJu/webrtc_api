package com.example.webrtc.utils;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;

public class HMapUtils {

	// String 
	public static String getString(final Map<String, Object> map, final String key, final String defaultValue) {
        String res = MapUtils.getString(map, key, defaultValue);
        if (StringUtils.isEmpty(res)) {
        	res = defaultValue;
        }
        return res;
	}
	public static String getString(final Map<String, Object> map, final String key) {
        return getString(map, key, null);
	}

	// int 
	public static int getIntValue(final Map<String, Object> map, final String key, final int defaultValue) {
        final Integer integerObject = MapUtils.getInteger(map, key, defaultValue);
        if (integerObject == null) {
            return defaultValue;
        }
        return integerObject.intValue();
    }

	public static int getIntValue(final Map<String, Object> map, final String key) {
        return getIntValue(map, key, 0);
    }

	// BigDecimal
	public static BigDecimal getBigDValue(final Map<String, Object> map, final String key, final String defaultValue) {
		String src = MapUtils.getString(map, key, defaultValue);
        final BigDecimal object = new BigDecimal(src);
        return object;
    }
	public static BigDecimal getBigDValue(final Map<String, Object> map, final String key) {
        return getBigDValue(map, key, "0");
    }

	// long
	public static long getLongValue(final Map<String, Object> map, final String key, final long defaultValue) {
        final Long longObject = MapUtils.getLong(map, key, defaultValue);
        if (longObject == null) {
            return defaultValue;
        }
        return longObject.longValue();
    }
	public static long getLongValue(final Map<String, Object> map, final String key) {
        return getLongValue(map, key, 0);
    }
	
	// Double
	public static double getDoubleValue(final Map<String, Object> map, final String key, final double defaultValue) {
		final Double doubleObject = MapUtils.getDouble(map, key, defaultValue);
		if (doubleObject == null) {
			return defaultValue;
		}
		return doubleObject.doubleValue();
	}
	public static double getDoubleValue(final Map<String, Object> map, final String key) {
		return getDoubleValue(map, key, 0);
	}
	
	// String Array 
	public static String[] getStringArray(final Map<String, Object> map, final String key, final String[] defaultValue) {
		if (MapUtils.isEmpty(map)) {
			return defaultValue;
	    }

		String[] params = null;
		try {
			params = (String[]) map.get(key);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if( params == null )  return defaultValue;
		
		return params;
	}

	public static String[] getStringArray(final Map<String, Object> map, final String key) {
		String[] tmpArray = {""};
		return getStringArray(map, key, tmpArray);
	}
	
	// List 
	@SuppressWarnings("unchecked")
	public static List<String> getListString(final Map<String, Object> map, final String key, final List<String> defaultValue) {
		if (MapUtils.isEmpty(map)) {
			return defaultValue;
	    }

		List<String> params = null;
		try {
			params = (List<String>) map.get(key);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if( params == null )  return defaultValue;
		
		return params;
	}

	public static List<String> getListString(final Map<String, Object> map, final String key) {
		List<String> tmpArray = new ArrayList<String>();
		return getListString(map, key, tmpArray);
	}
	

	// B 
	public static boolean getBoolean(final Map<String, Object> map, final String key, final boolean defaultValue) {
        return MapUtils.getBoolean(map, key, defaultValue);
	}
	public static boolean getBoolean(final Map<String, Object> map, final String key) {
        return getBoolean(map, key, false);
	}

	
	
	@SuppressWarnings("unchecked")
	public static List<Map<String, Object>> getListMap(final Map<String, Object> map, final String key, final List<Map<String, Object>> defaultValue) {
		if (MapUtils.isEmpty(map)) {
			return defaultValue;
	    }

		List<Map<String, Object>> res = null;
		try {
			res = (List<Map<String, Object>>) map.get(key);
		} catch (Exception e) {
			// e.printStackTrace();
		}

		if( res == null ) return defaultValue;
		
		return res;
	}
	public static List<Map<String, Object>> getListMap(final Map<String, Object> map, final String key) {
		List<Map<String, Object>> tmp = new ArrayList<Map<String, Object>>();
        return getListMap(map, key, tmp);
    }
	
	
	public static boolean eqString(final Map<String, Object> sMap, Map<String, Object> tMap, final String key) {
        return getString(sMap, key, null).equals(getString(tMap, key, null));
	}

	public static Map<String, Object> splitQuery(String query) {
	    Map<String, Object> query_pairs = new LinkedHashMap<String, Object>();
	    try {
	    	String queryUTF8 = URLDecoder.decode(query, "UTF-8");
	    	String[] pairs = queryUTF8.split("&");
	    	for (String pair : pairs) {
	    		int idx = pair.indexOf("=");
	    		query_pairs.put(URLDecoder.decode(pair.substring(0, idx), "UTF-8"), URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
	    	}
		} catch (Exception e) {
			query_pairs = null;
		}
	    return query_pairs;
	}
	/*
	// Validation
	public static boolean vaildation(String param, ) {
		boolean res = false;
		
		
		return res;
	}
	*/

	@SuppressWarnings("rawtypes")
	public static boolean isEmpty(Object obj){
        if( obj instanceof String ) return obj==null || "".equals(obj.toString().trim());
        else if( obj instanceof List ) return obj==null || ((List)obj).isEmpty();
        else if( obj instanceof Map ) return obj==null || ((Map)obj).isEmpty();
        else if( obj instanceof Object[] ) return obj==null || Array.getLength(obj)==0;
        else return obj==null;
	}
	
	public static boolean isNotEmpty(String s){
        return !isEmpty(s);
    }
	
	
	public static boolean checkPhoneNumber(String number) {
		return Pattern.matches("^01(?:0|1|[6-9])-(?:\\d{3}|\\d{4})-\\d{4}$", number);
	}
	public static boolean checkEmail(String email) {
		return Pattern.matches("[\\w\\~\\-\\.]+@[\\w\\~\\-]+(\\.[\\w\\~\\-]+)+", email);
	}
}
