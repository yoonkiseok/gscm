package com.tkg.gscm.sample.controller;

import com.tkg.gscm.sample.service.ThreadLocalService;
import com.tkg.gscm.utils.ThreadLocalUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ThreadLocalController {

    @Autowired
    ThreadLocalService threadLocalService;


    @GetMapping("/threadlocal")
    public ResponseEntity<String> controllerMethod() throws Exception{
        // 스레드 로컬에 값을 설정: 값설정 가능한 데이타 타입 존재
        ThreadLocalUtil.setThreadLocalValue("Value from Controller");

        // Service 호출
        String data =  threadLocalService.serviceMethod();

        // 스레드 로컬에서 값을 읽어옴
        Object value = ThreadLocalUtil.getThreadLocalValue();

        // 스레드 로컬 값을 정리
        ThreadLocalUtil.removeThreadLocalValue();

        return ResponseEntity.ok("정상적으로 처리 되었습니다.");
    }

}
