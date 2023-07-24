package com.tkg.gscm.sample.service;



import com.tkg.gscm.common.db.CommonDao;
import com.tkg.gscm.common.util.TkQueryGenerator;
import com.tkg.gscm.common.vo.CommonInVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MemberService {

    private final CommonDao commonDao;

    @Autowired
    public MemberService(CommonDao commonDao){
        this.commonDao = commonDao;
    }

    @Autowired
    private TkQueryGenerator queryGenerator;




    public List<HashMap<String,Object>> memberList(HashMap<String,Object> paramMap) throws Exception{
//        if (1==1) {
//            throw new Exception("테스트 에럽말생");
//        }
//        return memberDao.memberList(paramMap);
        return commonDao.selectList("com.tkg.gscm.sample.dao.MemberDao.memberList");
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor={Exception.class})
    public void memberUpdate(HashMap<String,Object> paramMap) throws Exception{

        commonDao.update("com.tkg.gscm.sample.dao.MemberDao.memberUpdate",paramMap);

//        에러발생으로 트랜잭션이 적용이 되는가 테스트 하는 구문
//        if (1==1) {
//            throw new Exception("테스트 에러말생");
//        }
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor={Exception.class})
    public void memberDelete(HashMap<String,Object> paramMap) throws Exception{

        commonDao.delete("com.tkg.gscm.sample.dao.MemberDao.memberDelete",paramMap);


//        에러발생으로 트랜잭션이 적용이 되는가 테스트 하는 구문
//        if (1==1) {
//            throw new Exception("테스트 에러말생");
//        }
    }


    @Transactional(propagation = Propagation.REQUIRED, rollbackFor={Exception.class})
    public void memberInsert(HashMap<String,Object> paramMap) throws Exception{

        commonDao.insert("com.tkg.gscm.sample.dao.MemberDao.memberInsert",paramMap);


//        에러발생으로 트랜잭션이 적용이 되는가 테스트 하는 구문
//        if (1==1) {
//            throw new Exception("테스트 에러말생");
//        }
    }


    public void commonQuery(CommonInVo inVo) throws Exception{

//        AS-IS CommonService.java 파일의 아래 함수부분을 참고
//        public CommonOutVo selectCommonQgList(CommonInVo inVo) throws Exception {}



//        CommonOutVo outVo = new CommonOutVo();

        String queryId = (String)inVo.getInParam("userQueryID");

//        테스트시 SessionUtil 에 로그인시 데이타를 넣어 주어야 된다.

        Map<String, Object> paramMap = queryGenerator.getQgParamMap(inVo);
        paramMap.put("dsBucket", inVo.getInList("dsBucket"));

        List<Map<String,Object>> dsList = commonDao.selectList(queryId, paramMap);

    }

}
