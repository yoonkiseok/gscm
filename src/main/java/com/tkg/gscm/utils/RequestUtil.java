package com.tkg.gscm.utils;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.text.StringEscapeUtils;

import jakarta.servlet.http.HttpServletRequest;

public class RequestUtil {

	/**
	 * RequestParam 에 포함된 파라미터 를 Map 으로 변환하고 
	 * injection 체크 후 Map 으로 전달 한다.
	 *  
	 * @param request
	 * @return
	 * @throws Exception
	 */
	public static Map<String, Object> getRequestMap(Map<String, ?> request) throws Exception {
		
		Map<String, Object> result = new HashMap<String, Object>();
		
		for (Map.Entry<String, ?> entry : request.entrySet()) {
			
			String key = entry.getKey();
			Object val = entry.getValue();
			if (val instanceof Object[]) {
				Object[] vals = (Object[])val;
				if (vals.length > 0)
					val = vals[0];
				else
					val = null;
			}
			
			if (val instanceof String) {
				
				String ret = getSafeRequestString((String)val, "content".equals(key) ? 0 : 1);
				
				if ("search".equals(key))
					ret = ret.trim().toLowerCase();
				val = ret;
			}
			
			result.put(key, val);
			
	    }
		
		return result;
	}
	
	/**
	 * request 에 포함된 파라미터 를 Map 으로 변환하고 
	 * injection 체크 후 Map 으로 전달 한다. 
	 * 
	 * @param request
	 * @return
	 * @throws Exception
	 */
	public static Map<String, Object> getRequestMap(HttpServletRequest request) throws Exception
	{
		Map<String, String[]> reqMap = request.getParameterMap();
		
		Map<String, Object> result = new HashMap<String, Object>();
		
		for (Map.Entry<String, String[]> entry : reqMap.entrySet()) {
			
			String key = entry.getKey(), val;
			String[] vals = entry.getValue();
			
			if (vals.length > 0)
				val = vals[0];
			else
				val = null;
			
			if (val instanceof String) {
				String ret = getSafeRequestString((String)val, "content".equals(key) ? 0 : 1);
				if ("search".equals(key))
					ret = ret.trim().toLowerCase();
				val = ret;
			}
			
			result.put(key, val);
	    }
		return result;
	}
	
	
	/**
	 * String 의 InJection 확인
	 * @param str
	 * @param escapeMode
	 * @return
	 * @throws Exception
	 */
	public static String getSafeRequestString(String str, int escapeMode) throws Exception {
		String res = str;
		if (escapeMode == 1) // content가 아닐때
			res = StringEscapeUtils.escapeXml11(StringEscapeUtils.unescapeXml(res));
		else
			res = res.replaceAll("'", "'");

		String find = res.toLowerCase(Locale.ROOT);
		String[] reserved = { "script", "insert", "delete", "update" };

		// String pattern = orPattern(reserved);
		for (String pat : reserved)
			if (find.indexOf(pat) >= 0)
				throw new Exception("script injection!!");

		return res;
	}	
	
}
