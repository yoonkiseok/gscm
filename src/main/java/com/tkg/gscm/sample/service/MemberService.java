package com.tkg.gscm.sample.service;

import com.tkg.gscm.sample.dao.MemberDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;

@Service
public class MemberService {

    @Qualifier("memberDao")
    @Autowired
    MemberDao memberDao;

    public List<HashMap<String,Object>> memberList(HashMap<String,Object> paramMap) throws Exception{
//        if (1==1) {
//            throw new Exception("테스트 에럽말생");
//        }
        return memberDao.memberList(paramMap);

    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor={Exception.class})
    public void memberUpdate(HashMap<String,Object> paramMap) throws Exception{

        memberDao.memberUpdate(paramMap);

//        에러발생으로 트랜잭션이 적용이 되는가 테스트 하는 구문
//        if (1==1) {
//            throw new Exception("테스트 에러말생");
//        }
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor={Exception.class})
    public void memberDelete(HashMap<String,Object> paramMap) throws Exception{

        memberDao.memberDelete(paramMap);

//        에러발생으로 트랜잭션이 적용이 되는가 테스트 하는 구문
//        if (1==1) {
//            throw new Exception("테스트 에러말생");
//        }
    }


    @Transactional(propagation = Propagation.REQUIRED, rollbackFor={Exception.class})
    public void memberInsert(HashMap<String,Object> paramMap) throws Exception{

        memberDao.memberInsert(paramMap);

//        에러발생으로 트랜잭션이 적용이 되는가 테스트 하는 구문
//        if (1==1) {
//            throw new Exception("테스트 에러말생");
//        }
    }

}
