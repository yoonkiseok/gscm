package com.tkg.gscm.sample.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tkg.gscm.sample.service.CommonCodeService;
import com.tkg.gscm.utils.RequestUtil;
import com.tkg.gscm.utils.ThreadLocalUtil;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@Tag(name = "Request 셈플")
public class RequestUtilSampleController {

	@Autowired
	private CommonCodeService codeService;

	/**
	 * 그룹 코드 리스트 가져오기 _ db에서 직접 읽어옴
	 * 
	 * RequestUtil 추가하여 script injection 확인
	 * 
	 * @param paramMap : localeType=en // 영문
	 * @return
	 * @throws Exception
	 */
    @GetMapping("/api/code/reqParamGoupCodeList")
	public ResponseEntity<List<Map<String, Object>>> requestParamGroupCodeList(@RequestParam Map<String,Object> paramMap) throws Exception{

        // 스레드 로컬에 값을 설정: 값설정 가능한 데이타 타입 존재
        ThreadLocalUtil.setThreadLocalValue("Value from Controller");

        //RequestUtil 추가 
        Map<String, Object> reqMap = RequestUtil.getRequestMap(paramMap);
        
        // Service 호출
        List<Map<String,Object>> dataList =  codeService.selectCommonGroupCodeList(reqMap);

        // 스레드 로컬에서 값을 읽어옴
        Object value = ThreadLocalUtil.getThreadLocalValue();

        // 스레드 로컬 값을 정리
        ThreadLocalUtil.removeThreadLocalValue();

        return ResponseEntity.status(HttpStatus.OK).body(dataList);
		
	}
    
    @GetMapping("/api/code/reqGoupCodeList")
	public ResponseEntity<List<Map<String, Object>>> requestGroupCodeList(HttpServletRequest request) throws Exception{

        // 스레드 로컬에 값을 설정: 값설정 가능한 데이타 타입 존재
        ThreadLocalUtil.setThreadLocalValue("Value from Controller");

        //RequestUtil 추가 
        Map<String, Object> reqMap = RequestUtil.getRequestMap(request);
        
        // Service 호출
        List<Map<String,Object>> dataList =  codeService.selectCommonGroupCodeList(reqMap);

        // 스레드 로컬에서 값을 읽어옴
        Object value = ThreadLocalUtil.getThreadLocalValue();

        // 스레드 로컬 값을 정리
        ThreadLocalUtil.removeThreadLocalValue();

        return ResponseEntity.status(HttpStatus.OK).body(dataList);
		
	}    
	
	
}
