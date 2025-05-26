package com.agnux.tms.api.config;

import com.agnux.tms.core.mgmt.HaulMgmt;
import com.agnux.tms.repository.pg.PgRepo;
import com.agnux.tms.repository.IHaulRepo;

import javax.sql.DataSource;
import org.postgresql.ds.PGSimpleDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataSourceConfig {

    @Value("${debug.mode:false}")
    private boolean debugMode;

    @Value("${db.url:jdbc:postgresql://localhost:5432/aipctestdb}")
    private String dbUrl;

    @Value("${db.username:aipctestdb}")
    private String dbUsername;

    @Value("${db.password:aipctestdb}")
    private String dbPassword;

    @Bean
    public DataSource getDataSource() {
        PGSimpleDataSource ds = new PGSimpleDataSource();
        ds.setUrl(dbUrl);
        ds.setUser(dbUsername);
        ds.setPassword(dbPassword);
        return ds;
    }

    @Bean
    public IHaulRepo getPgRepo(DataSource dataSource) {
        return new PgRepo(dataSource, debugMode);
    }

    @Bean
    public HaulMgmt haulMgmt(IHaulRepo haulRepo) {
        return new HaulMgmt(haulRepo);
    }
}
