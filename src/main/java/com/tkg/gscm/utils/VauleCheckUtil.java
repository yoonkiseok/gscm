package com.tkg.gscm.utils;

import java.util.HashMap;
import java.util.List;

public  class  VauleCheckUtil {

    public static void checkValueThrowException(HashMap<String, Object> map, List<String> list) {
        for (String key : list) {
            if (!map.containsKey(key)) {
                throw new IllegalArgumentException("파라미터  " + key + " 가 존재하지 않습니다.");
            }

            String value = (String)map.get(key);
            if (value == null || value.isEmpty()) {
                throw new IllegalArgumentException("파라미터 " + key + "  에 해당되는 값이 존재하지 않습니다.");
            }
        }
    }

    public static void checkValueThrowException(HashMap<String, Object> map, String delimitedString) {
        String[] keys = delimitedString.split("\\^");

        for (String key : keys) {
            if (!map.containsKey(key)) {
                throw new IllegalArgumentException("파라미터에 " + key + " 가 존재하지 않습니다.");
            }

            String value = (String)map.get(key);
            if (value == null || value.isEmpty()) {
                throw new IllegalArgumentException("파라미터에 " + key + " 에 해당되는 값이 존재하지 않습니다.");
            }
        }
    }

}
