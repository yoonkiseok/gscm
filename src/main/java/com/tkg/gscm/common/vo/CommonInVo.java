package com.tkg.gscm.common.vo;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//


import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class CommonInVo {
    private Map<String, String> inParamMap;
    private Map<String, List<Map<String, Object>>> inListMap;

    public CommonInVo() {
        this.inParamMap = new HashMap();
        this.inListMap = new HashMap();
    }

    public CommonInVo(Map<String, String> inParamMap, Map<String, List<Map<String, Object>>> inListMap) {
        if (inParamMap == null) {
            this.inParamMap = new HashMap();
        } else {
            this.inParamMap = inParamMap;
        }

        if (inListMap == null) {
            this.inListMap = new HashMap();
        } else {
            this.inListMap = inListMap;
        }

    }

    public Iterator<String> getInParamIterator() {
        return this.inParamMap.keySet().iterator();
    }

    public Iterator<String> getInListIterator() {
        return this.inListMap.keySet().iterator();
    }

    public Map<String, String> getInParamMap() {
        return this.inParamMap;
    }

    public Map<String, List<Map<String, Object>>> getInListMap() {
        return this.inListMap;
    }

    public String getInParam(String key) {
        return (String)this.inParamMap.get(key);
    }

    public List<Map<String, Object>> getInList(String inListName) {
        return (List)this.inListMap.get(inListName);
    }

    public void setInParamMap(Map<String, String> inParamMap) {
        this.inParamMap = inParamMap;
    }

    public void setInListMap(Map<String, List<Map<String, Object>>> inListMap) {
        this.inListMap = inListMap;
    }

    public void putInParamMap(Map<String, String> inParamMap) {
        this.inParamMap.putAll(inParamMap);
    }

    public void putInListMap(Map<String, List<Map<String, Object>>> inListMap) {
        this.inListMap.putAll(inListMap);
    }

    public void putInParam(String key, String value) {
        this.inParamMap.put(key, value);
    }

    public void putInList(String inListName, List<Map<String, Object>> inList) {
        this.inListMap.put(inListName, inList);
    }

    public void clearInParamMap() {
        this.inParamMap.clear();
    }

    public void clearInListMap() {
        this.inListMap.clear();
    }

    public String toString() {
        StringBuffer toString = new StringBuffer("== CommonInVo ==");
        toString.append("\n\t-- paramMap --");
        Iterator<String> paramIter = this.getInParamIterator();

        while(paramIter.hasNext()) {
            String key = (String)paramIter.next();
            Object val = this.inParamMap.get(key);
            toString.append("\n\t\t").append(key).append("=").append(val);
        }

        toString.append("\n\t---------------");
        toString.append("\n\t-- listMap --");
        Iterator<String> listIter = this.getInListIterator();

        while(listIter.hasNext()) {
            String key = (String)listIter.next();
            Object val = this.inListMap.get(key);
            toString.append("\n\t\t").append(key).append("=").append(val);
        }

        toString.append("\n\t---------------");
        toString.append("\n=================");
        return toString.toString();
    }
}
