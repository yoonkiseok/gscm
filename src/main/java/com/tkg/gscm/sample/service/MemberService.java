package com.tkg.gscm.sample.service;


import com.tkg.gscm.common.db.CommonDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;

@Service
public class MemberService {

    private final CommonDao commonDao;

    @Autowired
    public MemberService(CommonDao commonDao){
        this.commonDao = commonDao;
    }




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

}
