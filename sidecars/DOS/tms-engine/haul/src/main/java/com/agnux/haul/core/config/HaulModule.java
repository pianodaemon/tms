package com.agnux.haul.core.config;

import com.agnux.haul.core.mgmt.HaulMgmt;
import com.agnux.haul.repository.IHaulRepo;
import com.agnux.haul.repository.BasicRepoImpl;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;

public class HaulModule extends AbstractModule {

    @Override
    protected void configure() {
        // Bind the interface to its implementation if known
        bind(IHaulRepo.class).to(BasicRepoImpl.class).in(Singleton.class);
    }

    @Provides
    @Singleton
    public HaulMgmt provideHaulMgmt(IHaulRepo haulRepo) {
        return new HaulMgmt(haulRepo);
    }
}

