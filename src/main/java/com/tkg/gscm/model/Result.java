package com.tkg.gscm.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Splitter;

public final class Result extends HashMap<String, Object> { 
	
	private static final long serialVersionUID = 1L;
	
	public static final String RETURN_MODEL_DATA = "DATA";
	public static final String RETURN_MODEL_LIST = "LIST";
	public static final String RETURN_MODEL_TOTAL_COUNT = "TOTAL_COUNT";
	
	private static final String KEY_LIST = "LIST";
	private static final String KEY_TOTAL_COUNT = "TOTAL_COUNT";
	
	public static final String KEY_SUCCESS = "success";
	public static final String KEY_MESSAGE = "message";

	public static final Result EMPTY = new Result();

	public static final Result SUCCESS = new Result();
	static {
		SUCCESS.put(KEY_SUCCESS, Boolean.TRUE);
	}

	public static final Result FAILURE = new Result();
	static {
		FAILURE.put(KEY_SUCCESS, Boolean.FALSE);
	}

	public static final Result MESSAGE(String message) {
		Result result = new Result();
		result.put(KEY_MESSAGE, message);
		return result;
	}

	public static final Result MESSAGE(String message, boolean success) {
		Result result = new Result();
		result.put(KEY_MESSAGE, message);
		result.put(KEY_SUCCESS, success);
		return result;
	}
	
	public static final Result SUCCESS() {
		Result result = new Result();
		result.put(KEY_SUCCESS, Boolean.TRUE);
		return result;
	}

	public static final Result SUCCESS(String message) {
		Result result = new Result();
		result.put(KEY_MESSAGE, message);
		result.put(KEY_SUCCESS, Boolean.TRUE);
		return result;
	}

	public static final Result FAILURE() {
		Result result = new Result();
		result.put(KEY_SUCCESS, Boolean.FALSE);
		return result;
	}
	
	public static final Result FAILURE(String message) {
		Result result = new Result();
		result.put(KEY_MESSAGE, message);
		result.put(KEY_SUCCESS, Boolean.FALSE);
		return result;
	}

	public static final Result FAILURE(String format, Object... args) {
		Result result = new Result();
		result.put(KEY_MESSAGE, String.format(format, args));
		result.put(KEY_SUCCESS, Boolean.FALSE);
		return result;
	}

	public static final <T> Result LIST(int totalCount, List<T> list) {
		Result result = new Result();
		result.put(KEY_TOTAL_COUNT, totalCount);
		result.put(KEY_LIST, list);
		result.put(KEY_SUCCESS, Boolean.TRUE);
		return result;
	}

	public static final <T> Result LIST(List<T> list) {
		Result result = new Result();
		result.put(KEY_LIST, list);
		result.put(KEY_SUCCESS, Boolean.TRUE);
		return result;
	}

	@SuppressWarnings("unchecked")
	public <T> List<T> LIST() {
		return (List<T>) get(KEY_LIST);
	}

	public static final String KEY_DATA = "data";

	public static final Result DATA(Map<String, Object> data) {
		boolean success = Boolean.TRUE;
		if (data == null || data.isEmpty()) {
			return Result.FAILURE(NO_DATA);
		}

		Result result = new Result();
		result.put(KEY_SUCCESS, success);
		result.put(KEY_DATA, data);
		
		System.out.println("===== > Result success : " + success);
		System.out.println("===== > Result data : " + data);
		return result;
	}

	public static final String NO_DATA = "NO_DATA";

	public static Result RESULT(Map<String, Object> m) {
		Result result = new Result();
		result.putAll(m);
		return result;
	}

	public static Result RESULT(String key, Object value) {
		Result result = new Result();
		result.put(key, value);
		return result;
	}

	@SuppressWarnings("unchecked")
	public <K, V> V getData(String key) {
		return (V) get(key);
	}

	public String getString(String key) {
		return getString(key, StringUtils.EMPTY);
	}

	public String getString(String key, String df) {
		if (get(key) == null) {
			return df;
		}
		return String.valueOf(get(key));
	}

	public int getInteger(String key) {
		return Integer.valueOf(getString(key));
	}

	public boolean getBoolean(String key) {
		return (Boolean.valueOf(getString(key))
				|| "Y".equals(getString(key))
				|| "1".equals(getString(key)));
	}

	public boolean isSuccess() {
		return this.getBoolean(KEY_SUCCESS);
	}

	public String getMessage() {
		return getData(KEY_MESSAGE);
	}

	public void toArray(String key, Splitter splitter) {
		if (this.containsKey(KEY_DATA) == Boolean.FALSE)
			return;

		@SuppressWarnings("unchecked")
		Map<String, Object> data = (Map<String, Object>) this.get(KEY_DATA);
		data.put(key, splitter.omitEmptyStrings().trimResults().splitToList((String) data.get(key)));
		this.put(KEY_DATA, data);
	}
}