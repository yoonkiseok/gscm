package com.tkg.gscm.utils;

public class StringUtil {

	public static boolean isNull(Object obj) {
		
		return (obj == null);
		
	}

	public static boolean isNotNull(Object obj) {
		
		return (isNull(obj) == Boolean.FALSE);
		
	}
	
	/**
     * 문자열이 있는지 확인한다.<br>
     * null 일 경우, "" (빈스트링)을 리턴한다.
     *
     * @param String val 대상 문자열
     * @return String
     */
	public static String nvl(String val) {
		
		return isNull(val) ? "" : val.trim();
		
    }
	
	public static String nvl(Object val) {
		
		return isNull(val) ? "" : val.toString().trim();
		
	}
	
    /**
     * 비교대상 문자열이 null 일 경우 대체값을 리턴한다.
     *
     * @param String val 대상 문자열
     * @param String rep 대체값
     * @return String
     */
	public static String nvl( String val, String rep ) {
        return isNull(val) ? rep : val.trim();
    }	
	
}
