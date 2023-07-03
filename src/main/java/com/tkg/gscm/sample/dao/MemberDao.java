package com.tkg.gscm.sample.dao;

import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;

@Repository(value="memberDao")
public interface MemberDao {
    public List<HashMap<String,Object>> memberList(HashMap<String,Object> paramMap);
    public void memberUpdate(HashMap<String,Object> paramMap);

    public void memberInsert(HashMap<String,Object> paramMap);

    public void memberDelete(HashMap<String,Object> paramMap);
}
