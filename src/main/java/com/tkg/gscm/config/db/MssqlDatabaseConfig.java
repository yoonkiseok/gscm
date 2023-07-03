package com.tkg.gscm.config.db;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

@Configuration
@MapperScan("com.**.dao")
@EnableTransactionManagement
public class MssqlDatabaseConfig {

//      yml에서 설정하여 주석처리
//    @Bean
//    public DataSource dataSource() {
//        DriverManagerDataSource dataSource = new DriverManagerDataSource();
//        dataSource.setDriverClassName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
//        dataSource.setUrl("jdbc:sqlserver://localhost:1433;databaseName=mydatabase");
//        dataSource.setUsername("dbuser");
//        dataSource.setPassword("dbpass");
//        return dataSource;
//    }

    @Bean
    public SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception {
        final SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
        sessionFactory.setDataSource(dataSource);

        Resource configLocation = new PathMatchingResourcePatternResolver().getResource("classpath:/config/db/mybatis-config.xml");
        Resource[] mapperLocations =  new PathMatchingResourcePatternResolver().getResources("classpath:/mapper/**/*.xml");

        sessionFactory.setConfigLocation(configLocation);
        sessionFactory.setMapperLocations(mapperLocations);

        return sessionFactory.getObject();
    }

//    SqlSessionFactory와 SqlSession의 래퍼 역할을 하며, 트랜잭션 관리와 세션의 생명주기를 자동으로 처리
    @Bean
    public SqlSessionTemplate sqlSessionTemplate(SqlSessionFactory sqlSessionFactory) throws Exception {
        final SqlSessionTemplate sqlSessionTemplate = new SqlSessionTemplate(sqlSessionFactory);
        return sqlSessionTemplate;
    }


    @Bean
    public PlatformTransactionManager transactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }


}
