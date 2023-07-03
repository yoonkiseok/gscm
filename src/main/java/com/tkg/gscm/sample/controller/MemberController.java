package com.tkg.gscm.sample.controller;

import com.tkg.gscm.sample.service.MemberService;
import com.tkg.gscm.sample.service.ThreadLocalService;
import com.tkg.gscm.utils.ThreadLocalUtil;
import com.tkg.gscm.utils.VauleCheckUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;

@RestController
@Tag(name = "멤버")
public class MemberController {

    @Autowired
    MemberService memberService;

    @Autowired
    ThreadLocalService threadLocalService;


    @GetMapping("/member/list")
    public ResponseEntity<List<HashMap<String,Object>>> list(@RequestParam HashMap<String,Object> paramMap) throws Exception{
        // 스레드 로컬에 값을 설정: 값설정 가능한 데이타 타입 존재
        ThreadLocalUtil.setThreadLocalValue("Value from Controller");

        // Service 호출
        List<HashMap<String,Object>> dataList =  memberService.memberList(paramMap);

        // 스레드 로컬에서 값을 읽어옴
        Object value = ThreadLocalUtil.getThreadLocalValue();

        // 스레드 로컬 값을 정리
        ThreadLocalUtil.removeThreadLocalValue();

//        return ResponseEntity.ok("정상적으로 처리 되었습니다.");
        return ResponseEntity.status(HttpStatus.OK).body(dataList);


    }


    @GetMapping("/member/update")
    @Operation(summary = "멤버정보 업데이트")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "정상적으로 처리되었습니다.")
    })
    public ResponseEntity<String> update(
            @Parameter(description = "멤버 정보", required = true, example = "{\"id\":\"\", \"name\":\"\"}")
            @RequestParam HashMap<String,Object> paramMap) throws Exception{

        // 스레드 로컬에 값을 설정: 값설정 가능한 데이타 타입 존재
        ThreadLocalUtil.setThreadLocalValue(paramMap);

//        필수파라미터 확인
        VauleCheckUtil.checkValueThrowException(paramMap, "id^name");


        // Service 호출
        memberService.memberUpdate(paramMap);

        return ResponseEntity.status(HttpStatus.OK).body("정상적으로 처리 되었습니다.");


    }


    @GetMapping("/member/insert")
    public ResponseEntity<String> insert(@RequestParam HashMap<String,Object> paramMap) throws Exception{

        // 스레드 로컬에 값을 설정: 값설정 가능한 데이타 타입 존재
        ThreadLocalUtil.setThreadLocalValue(paramMap);

//        필수파라미터 확인
        VauleCheckUtil.checkValueThrowException(paramMap, "id^name");


        // Service 호출
        memberService.memberInsert(paramMap);

        return ResponseEntity.status(HttpStatus.OK).body("정상적으로 처리 되었습니다.");


    }


    @GetMapping("/member/delete")
    public ResponseEntity<String> delete(@RequestParam HashMap<String,Object> paramMap) throws Exception{

        // 스레드 로컬에 값을 설정: 값설정 가능한 데이타 타입 존재
        ThreadLocalUtil.setThreadLocalValue(paramMap);

//        필수파라미터 확인
        VauleCheckUtil.checkValueThrowException(paramMap, "id");


        // Service 호출
        memberService.memberDelete(paramMap);

        return ResponseEntity.status(HttpStatus.OK).body("정상적으로 처리 되었습니다.");


    }


}
