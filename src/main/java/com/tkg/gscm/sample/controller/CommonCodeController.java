package com.tkg.gscm.sample.controller;

import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tkg.gscm.sample.service.CommonCodeService;
import com.tkg.gscm.utils.ThreadLocalUtil;

import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Tag(name = "공통코드")
public class CommonCodeController {
	
	@Autowired
	private CommonCodeService codeService;

	/**
	 * 그룹 코드 리스트 가져오기 _ db에서 직접 읽어옴
	 * @param paramMap
	 * @return
	 * @throws Exception
	 */
    @GetMapping("/api/code/gclist")
	public ResponseEntity<List<HashMap<String, Object>>> groupCodeList(@RequestParam HashMap<String,Object> paramMap) throws Exception{

        // 스레드 로컬에 값을 설정: 값설정 가능한 데이타 타입 존재
        ThreadLocalUtil.setThreadLocalValue("Value from Controller");

        // Service 호출
        List<HashMap<String,Object>> dataList =  codeService.selectCommonGroupCodeList(paramMap);

        // 스레드 로컬에서 값을 읽어옴
        Object value = ThreadLocalUtil.getThreadLocalValue();

        // 스레드 로컬 값을 정리
        ThreadLocalUtil.removeThreadLocalValue();

        return ResponseEntity.status(HttpStatus.OK).body(dataList);
		
	}
	
	/**
	 * 코드 리스트 가져오기 _ DB에서 직접 읽어옴
	 * @param paramMap
	 * @return
	 * @throws Exception
	 */
    @GetMapping("/api/code/clist")
	public ResponseEntity<List<HashMap<String, Object>>> codeList(@RequestParam HashMap<String,Object> paramMap) throws Exception{
        // 스레드 로컬에 값을 설정: 값설정 가능한 데이타 타입 존재
        ThreadLocalUtil.setThreadLocalValue("Value from Controller");

        paramMap.put("localeType", "en");
        // Service 호출
        List<HashMap<String,Object>> dataList =  codeService.selectCommonCodeList(paramMap);

        // 스레드 로컬에서 값을 읽어옴
        Object value = ThreadLocalUtil.getThreadLocalValue();

        // 스레드 로컬 값을 정리
        ThreadLocalUtil.removeThreadLocalValue();

        return ResponseEntity.status(HttpStatus.OK).body(dataList);
	}

	/**
	 * 대상 그룹코의 이름 가져오기
	 * @param paramMap
	 * @return
	 * @throws Exception
	 */
	@GetMapping("/api/code/getCodeGroupNm")
	public ResponseEntity<HashMap<String, Object>> getCodeGroupNm(@RequestParam HashMap<String,Object> paramMap) throws Exception{
        // 스레드 로컬에 값을 설정: 값설정 가능한 데이타 타입 존재
        ThreadLocalUtil.setThreadLocalValue("Value from Controller");

        String groupCd = String.valueOf(paramMap.get("gcd"));
        String groupNm = codeService.getCodeGroupNm(groupCd);
        
        // Service 호출
        HashMap<String,Object> dataMap = new HashMap<String,Object>(); 
        dataMap.put("GROUP_NM", groupNm);
        // 스레드 로컬에서 값을 읽어옴
        Object value = ThreadLocalUtil.getThreadLocalValue();

        // 스레드 로컬 값을 정리
        ThreadLocalUtil.removeThreadLocalValue();

        return ResponseEntity.status(HttpStatus.OK).body(dataMap);
	}     
    
	/**
	 * 코드 이름 가져오기
	 * @param paramMap
	 * @return
	 * @throws Exception
	 */
	@GetMapping("/api/code/getCodeNm")
	public ResponseEntity<HashMap<String, Object>> getCodeNm(@RequestParam HashMap<String,Object> paramMap) throws Exception{
        // 스레드 로컬에 값을 설정: 값설정 가능한 데이타 타입 존재
        ThreadLocalUtil.setThreadLocalValue("Value from Controller");

        String groupCd = String.valueOf(paramMap.get("gcd"));
        String subCd = String.valueOf(paramMap.get("scd"));
        
        String cdNm = codeService.getCodeNm(groupCd, subCd);
        
        // Service 호출
        HashMap<String,Object> dataMap = new HashMap<String,Object>(); 
        dataMap.put("CD_NM", cdNm);
        // 스레드 로컬에서 값을 읽어옴
        Object value = ThreadLocalUtil.getThreadLocalValue();

        // 스레드 로컬 값을 정리
        ThreadLocalUtil.removeThreadLocalValue();

        return ResponseEntity.status(HttpStatus.OK).body(dataMap);
	} 	
	
    /**
     * 전체 코드 리스트 가져오기
     * @param paramMap
     * @return
     * @throws Exception
     */
	@GetMapping("/api/code/getCodeAll")
	public ResponseEntity<List<HashMap<String, Object>>> getCodeAll() throws Exception{
        // 스레드 로컬에 값을 설정: 값설정 가능한 데이타 타입 존재
        ThreadLocalUtil.setThreadLocalValue("Value from Controller");

        // Service 호출
        List<HashMap<String,Object>> dataList =  codeService.getCode();

        // 스레드 로컬에서 값을 읽어옴
        Object value = ThreadLocalUtil.getThreadLocalValue();

        // 스레드 로컬 값을 정리
        ThreadLocalUtil.removeThreadLocalValue();

        return ResponseEntity.status(HttpStatus.OK).body(dataList);
	}  
    
    /**
     * 대상 그룹 코드의 자식 리스트 가져오기
     * 
     * @param paramMap : gcd = 그룹 코드
     * @return
     * @throws Exception
     */
	@GetMapping("/api/code/getCodeList")
	public ResponseEntity<List<HashMap<String, Object>>> getCodeList(@RequestParam HashMap<String,Object> paramMap) throws Exception{
        // 스레드 로컬에 값을 설정: 값설정 가능한 데이타 타입 존재
        ThreadLocalUtil.setThreadLocalValue("Value from Controller");

        String groupCd = String.valueOf(paramMap.get("gcd"));
        
        // Service 호출
        List<HashMap<String,Object>> dataList =  codeService.getCode(groupCd);

        // 스레드 로컬에서 값을 읽어옴
        Object value = ThreadLocalUtil.getThreadLocalValue();

        // 스레드 로컬 값을 정리
        ThreadLocalUtil.removeThreadLocalValue();

        return ResponseEntity.status(HttpStatus.OK).body(dataList);
	} 
	
	
}
