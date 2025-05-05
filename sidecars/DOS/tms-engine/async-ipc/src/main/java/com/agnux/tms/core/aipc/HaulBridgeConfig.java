package com.agnux.tms.core.aipc;

import com.agnux.tms.core.config.HaulModule;
import com.agnux.tms.core.mgmt.HaulMgmt;
import com.agnux.tms.repository.IHaulRepo;
import com.google.inject.Guice;
import com.google.inject.Injector;
import javax.sql.DataSource;
import org.postgresql.ds.PGSimpleDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HaulBridgeConfig {

    @Value("${debug.mode:true}")
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
    public Injector guiceInjector(DataSource dataSource) {
        return Guice.createInjector(new HaulModule(dataSource, debugMode));
    }

    @Bean
    public HaulMgmt haulMgmt(Injector injector) {
        return injector.getInstance(HaulMgmt.class);
    }

    @Bean
    public IHaulRepo iHaulRepo(Injector injector) {
        return injector.getInstance(IHaulRepo.class);
    }
}