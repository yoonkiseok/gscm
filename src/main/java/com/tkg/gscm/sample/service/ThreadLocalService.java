package com.tkg.gscm.sample.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tkg.gscm.utils.ThreadLocalUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Service
public class ThreadLocalService {

    private final ObjectMapper objectMapper;

    @Autowired
    public ThreadLocalService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String serviceMethod() throws  Exception {
        // Controller에서 설정한 스레드 로컬 값을 읽어옴
        Object value = ThreadLocalUtil.getThreadLocalValue();

        // Service 로직 수행

        // 스레드 로컬 값을 수정
        ThreadLocalUtil.setThreadLocalValue("Modified Value in Service");

//        테스트를 위한 강제 에러처리
//        ExceptionHandlerAspect 에 구현한 쓰레드 로컬 값 출력하는지 확인
        if (1 == 1) {
            throw new Exception("강제 에러 생성");
        }

        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("koreaName", "홍길동");

        String json = objectMapper.writeValueAsString(hashMap);

        return json;

        // 필요한 작업 완료 후에는 ThreadLocal 값을 컨트롤러 또는 exception 에서 처리합니다.
    }


    public HashMap<String,Object> serviceMethod2() throws  Exception {

        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("koreaName", "홍길동");

        return hashMap;

        // 필요한 작업 완료 후에는 ThreadLocal 값을 컨트롤러 또는 exception 에서 처리합니다.
    }
}
