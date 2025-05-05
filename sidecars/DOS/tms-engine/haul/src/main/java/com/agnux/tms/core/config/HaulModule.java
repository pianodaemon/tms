package com.agnux.tms.core.config;

import com.agnux.tms.core.mgmt.HaulMgmt;
import com.agnux.tms.repository.IHaulRepo;
import com.agnux.tms.repository.BasicRepoImpl;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import javax.sql.DataSource;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class HaulModule extends AbstractModule {

    private final DataSource dataSource;
    private final boolean debugMode;

    @Override
    protected void configure() {
        // Interface binding – useful if other classes inject IHaulRepo directly
        bind(IHaulRepo.class).in(Singleton.class);
    }

    @Provides
    @Singleton
    public IHaulRepo provideIHaulRepo() {
        return new BasicRepoImpl(dataSource, debugMode);
    }

    @Provides
    @Singleton
    public HaulMgmt provideHaulMgmt(IHaulRepo haulRepo) {
        return new HaulMgmt(haulRepo);
    }
}