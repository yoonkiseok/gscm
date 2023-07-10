package com.tkg.gscm.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class DateUtil {
	
	private static final String DATE_FORMAT_DEFAULT = "yyyy-MM-dd";
	private static final String DATE_TIME_FORMAT_DEFAULT = "yyyy-MM-dd HH:mm:ss";
	
	private static final String DATE_FORMAT_CUTOM1 = "yyyyMMddHHmmss";
	private static final String DATE_FORMAT_CUTOM2 = "yyyyMMdd";
	private static final String DATE_FORMAT_CUTOM3 = "yyMMdd";
	
	private static final String DATE_TIME_FORMAT_CUTOM1 = "HHmmss";
	private static final String DATE_TIME_FORMAT_CUTOM2 = "HHmm";

	/**
	 * Date 형인 날짜를 문자열 날짜로 변환한다.
	 * 
	 * @param date : Date 형인 날짜
	 * @return 문자열 날짜
	 */
	public static String getDateFormat(Date date) {
		
       return getDateFormat(date, DATE_FORMAT_DEFAULT);
       
	}


	/**
	 * Object 형인 날짜를 문자열 날짜로 변환한다.
	 * 
	 * @param date : Object 형인 날짜
	 * @return
	 */
	public static String getDateFormat(Object date) {
		
       return getDateFormat(date, DATE_FORMAT_DEFAULT);
       
	}

    /**
     * Date 형인 날짜를 문자열 날짜로 변환한다.
     * DATE_FORMAT_DEFAULT : yyyy-MM-dd
     * 
     * @param date  : Date 형인 날짜
     * @param pattern : 날짜 형식
     * @return 문자열 날짜
     */
	public static String getDateFormat(Date date, String pattern) {
		
		SimpleDateFormat formatter = new SimpleDateFormat(pattern);
		formatter.setLenient(false);
		
		return formatter.format(date);
		
	}	

    /**
     * Object 형인 날짜를 문자열 날짜로 변환한다.
     * 
     * @param date : Object 형인 날짜
     * @param pattern : 날짜 형식
     * @return 문자열 날짜
     */
	public static String getDateFormat(Object date, String pattern) {
		
		SimpleDateFormat formatter = new SimpleDateFormat(pattern);
		formatter.setLenient(false);
		
		return formatter.format(date);
		
	}		
	
	/**
	 * yyyy-MM-dd HH:mm:ss 형태로 변환
	 * milisecond > second 변환
	 * 
	 * @param object
	 * @return
	 */
	public static String getDateTimeConvert(Object object) {
		
		SimpleDateFormat formatter = new SimpleDateFormat(DATE_TIME_FORMAT_DEFAULT);
		Long value = ((Integer)object).longValue();
		
		return formatter.format(value * 1000);
		
	}
	
	/**
	 * Date getTime
	 * second > milisecond 변환
	 * @return
	 */
	public static Long getTimeConvert(){
		
		Date time = new Date();
		Long value = time.getTime();
		
		return value / 1000;
	}
	
	/**
	 * @return 현재일자를 "yyyyMMddHHmmss" 형식으로 리턴
	 */
	public static String getDateString() {
		
		java.text.SimpleDateFormat formatter = 
				new java.text.SimpleDateFormat (DATE_FORMAT_CUTOM1, java.util.Locale.KOREA);
		
		return formatter.format(new java.util.Date());
	}

	/**
	 * @return 현재일자를 "yyyyMMdd" 형식으로 리턴
	 */
	public static String getShortDateString() {
		
		java.text.SimpleDateFormat formatter = 
				new java.text.SimpleDateFormat (DATE_FORMAT_CUTOM2, java.util.Locale.KOREA);
		
		return formatter.format(new java.util.Date());
	}

	/**
	 * @return 현재일자를 "yyMMdd" 형식으로 리턴
	 */
	public static String getShortDateString2() {
		
		java.text.SimpleDateFormat formatter = 
				new java.text.SimpleDateFormat (DATE_FORMAT_CUTOM3, Locale.KOREA);
		
		return formatter.format(new java.util.Date());
	}
		
	/**
	 * @return 현재시간을 "HHmmss" 형식으로 리턴
	 */
	public static String getShortTimeString1() {
		
		java.text.SimpleDateFormat formatter = 
				new java.text.SimpleDateFormat (DATE_TIME_FORMAT_CUTOM1, Locale.KOREA);
		
		return formatter.format(new java.util.Date());
	}	

	/**
	 * @return 현재시간을 "HHmm" 형식으로 리턴
	 */
	public static String getShortTimeString2() {
		
		java.text.SimpleDateFormat formatter = 
				new java.text.SimpleDateFormat (DATE_TIME_FORMAT_CUTOM2, Locale.KOREA);
		
		return formatter.format(new java.util.Date());
	}	

		
	/**
	 * 현재 시간을 "HHMMSS"의 형식으로 반환함
	 * @return String 현재시간 "HHMMSS"
	 */
	public static String getCurrentTime() {
		Calendar cal = Calendar.getInstance();
		String currentTime = null;
		String hour, min, sec;

		int h = cal.get(Calendar.HOUR_OF_DAY);
		int m = cal.get(Calendar.MINUTE);
		int s = cal.get(Calendar.SECOND);

		if (h < 10) {
			hour = "0" + Integer.toString(h);
		} else {
			hour = Integer.toString(h);
		}
		if (m < 10) {
			min = "0" + Integer.toString(m);
		} else {
			min = Integer.toString(m);
		}
		if (s < 10) {
			sec = "0" + Integer.toString(s);
		} else {
			sec = Integer.toString(s);
		}

		currentTime = "" + hour + min + sec;
		return currentTime;
	}	
		
	/**
	 * 현재 년월일을 작성하는 Method
	 * @return  현재 년월일
	 */
	public static  String getToday() {
		Calendar now = Calendar.getInstance();

		int yr = now.get(Calendar.YEAR);
		String strYr = "" + yr;

		int mo = now.get(Calendar.MONTH) + 1;
		String strMo = "" + mo;
		if (mo < 10)
			strMo = "0" + mo;

		int dd = now.get(Calendar.DAY_OF_MONTH);
		String strDd = "" + dd;
		if (dd < 10)
			strDd = "0" + dd;

		String result = strYr + strMo + strDd;
		return result;
	}	
		
	/**
	 * 현재의 날짜로 부터 'n' 일 전후의 날짜를 리턴
	 * @param n : 일 수
	 * @return  현재 년-월-일
	 */	
	public static String getDoDate(int n){
		Calendar cal = new GregorianCalendar(Locale.KOREA);
		cal.setTime(new Date());
		cal.add(Calendar.DAY_OF_YEAR, n); // 하루를 더한다. 
			 
		SimpleDateFormat fm = new SimpleDateFormat(DATE_FORMAT_DEFAULT);
		String strDate = fm.format(cal.getTime());
			
		return strDate;
	}	
	   
	/**
	 * YYYYMMDD 입력된 날짜로 부터 한달 후의 날짜 리턴
	 * @param tdate : 문자열 입력 년월일
	 * @return  현재 년월일
	 */    
	public static String getNextMonthDate(String tdate) {
		int year    = Integer.parseInt(tdate.substring(0,4));
		int month   = Integer.parseInt(tdate.substring(4,6));
		int day     = Integer.parseInt(tdate.substring(6,8));

		month++;

		if(month > 12) { month=1; year++; };
		if(month == 2) {
			int daycnt = 28;
			
			if (year%4 == 0)    daycnt = 29;
			if (year%100 == 0)  daycnt = 28;
			if (year%400 == 0)  daycnt = 29;
			if (daycnt < day)   day    = daycnt;
		};

		String yearstr = "" + year;
	   	String monthstr = ""+month;
	   	if (month < 10) monthstr = "0"+monthstr;
	   	String daystr = ""+day;
	   	if (day < 10) daystr = "0"+daystr;
	   	
	   	return yearstr + monthstr + daystr;
	}

	/**
	 * YYYYMMDD 입력된 날짜로 부터 한달 전의 날짜 리턴
	 * @param tdate : 문자열 입력 년월일
	 * @return  현재 년월일
	 */
	public static String getPrevMonthDate(String tdate) {
		int year    = Integer.parseInt(tdate.substring(0,4));
		int month   = Integer.parseInt(tdate.substring(4,6));
		int day     = Integer.parseInt(tdate.substring(6,8));

		month--;
		if(month < 1) { month=12; year--;};
		if(month == 2) {
			int daycnt = 28;
			if (year%4 == 0)    daycnt = 29;
			if (year%100 == 0)  daycnt = 28;
			if (year%400 == 0)  daycnt = 29;
			if (daycnt < day)   day    = daycnt;
		};

	   	String yearstr = "" + year;
	   	String monthstr = ""+month;
	   	if (month < 10) monthstr = "0"+monthstr;
	   	String daystr = ""+day;
	   	if (day < 10) daystr = "0"+daystr;

	   	return yearstr + monthstr + daystr;
	}    
	   
	/**
	 * YYYYMMDD 입력된 날짜로 부터 1년 전의 날짜 리턴
	 * @param tdate : 문자열 입력 년월일
	 * @return  현재 년월일
	 */
	public static String getPrevYearDate(String tdate) {
		int year    = Integer.parseInt(tdate.substring(0,4));
		int month   = Integer.parseInt(tdate.substring(4,6));
		int day     = Integer.parseInt(tdate.substring(6,8));
		   
		year--;
		if(month < 1) { month=12; year--;};
		if(month == 2) {
			int daycnt = 28;
			if (year%4 == 0)    daycnt = 29;
			if (year%100 == 0)  daycnt = 28;
			if (year%400 == 0)  daycnt = 29;
			if (daycnt < day)   day    = daycnt;
		};
		   
		String yearstr = "" + year;
		String monthstr = ""+month;
		if (month < 10) monthstr = "0"+monthstr;
		String daystr = ""+day;
		if (day < 10) daystr = "0"+daystr;
		   
		return yearstr + monthstr + daystr;
	}    
	   
	/**
	 * 현재 년월의 첫번째 1일(yyyyMMdd) 반환함
	 * 
	 * @return String 현재달의 첫번째 일자(1일) 문자열 "20000901"
	 */
	public static String getFirstDay() {
		Calendar cal = Calendar.getInstance();
		int year, month/*, day*/;
		String today;

		year = cal.get(Calendar.YEAR);
		// calendar class의 MONTH는 0이 1월이므로 +1
		month = cal.get(Calendar.MONTH) + 1;
		/*day = cal.get(Calendar.DATE);*/

		if (month < 10) {
			today = Integer.toString(year) + "0" + month;
		} else {
			today = Integer.toString(year) + month;
		}
		today = today + "01";
		return today;
	}
	
	/**
	 * 현재 년월일(yyyyMMdd)의 n(월) 만큼 더한 년월일(yyyyMMdd) 표시
	 * @param date : 년월일(yyyyMMdd)
	 * @param n : n 월
	 * @return String 년월일(yyyyMMdd)
	 */
	public static String getAddMonth(String date, int n) {
       int year    = Integer.parseInt(date.substring(0,4));
       int month   = Integer.parseInt(date.substring(4,6));
       int day     = Integer.parseInt(date.substring(6,8));

       month = month + n;
       if(month < 1) { month=12+month; year--;};
       if(month == 2) {
	        int daycnt = 28;
	        if (year%4 == 0)    daycnt = 29;
	        if (year%100 == 0)  daycnt = 28;
	        if (year%400 == 0)  daycnt = 29;
	        if (daycnt < day)   day    = daycnt;
       };

	   	String yearstr = "" + year;
	   	String monthstr = ""+month;
	   	if (month < 10) monthstr = "0"+monthstr;
	
	   	return yearstr + monthstr + day;
	}
	
}
