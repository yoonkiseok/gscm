package com.tkg.gscm.common.db;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository("commonDao")
public class CommonDao extends SqlSessionTemplate{

    @Autowired
    public CommonDao(SqlSessionFactory sqlSessionFactory) {
        super(sqlSessionFactory);
    }

}
