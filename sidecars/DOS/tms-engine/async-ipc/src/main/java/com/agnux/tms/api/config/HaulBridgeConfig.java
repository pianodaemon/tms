package com.agnux.tms.api.config;

import com.agnux.tms.core.mgmt.HaulMgmt;
import com.agnux.tms.repository.BasicRepoImpl;
import com.agnux.tms.repository.IHaulRepo;

import javax.sql.DataSource;
import org.postgresql.ds.PGSimpleDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HaulBridgeConfig {

    @Value("${debug.mode:false}")
    private boolean debugMode;

    @Value("${db.url:jdbc:postgresql://localhost:5432/testdb}")
    private String dbUrl;

    @Value("${db.username:test}")
    private String dbUsername;

    @Value("${db.password:test}")
    private String dbPassword;

    @Bean
    public DataSource dataSource() {
        PGSimpleDataSource ds = new PGSimpleDataSource();
        ds.setUrl(dbUrl);
        ds.setUser(dbUsername);
        ds.setPassword(dbPassword);
        return ds;
    }

    @Bean
    public IHaulRepo getRepoImpl(DataSource dataSource) {
        return new BasicRepoImpl(dataSource, debugMode);
    }

    @Bean
    public HaulMgmt haulMgmt(IHaulRepo haulRepo) {
        return new HaulMgmt(haulRepo);
    }
}
