package com.github.openwebnet.component;

import com.github.openwebnet.OpenWebNetApplication;
import com.github.openwebnet.component.module.ApplicationContextModule;
import com.github.openwebnet.component.module.DatabaseModule;
import com.github.openwebnet.component.module.DomoticModule;
import com.github.openwebnet.component.module.RepositoryModule;

import static com.google.common.base.Preconditions.checkNotNull;

public class Injector {

    private static ApplicationComponent applicationComponent;

    private Injector() {}

    public static void initializeApplicationComponent(OpenWebNetApplication application) {
        applicationComponent = DaggerApplicationComponent.builder()
            .applicationContextModule(new ApplicationContextModule(application))
            .databaseModule(new DatabaseModule())
            .repositoryModule(new RepositoryModule())
            .domoticModule(new DomoticModule())
            .build();
    }

    public static ApplicationComponent getApplicationComponent() {
        checkNotNull(applicationComponent, "applicationComponent is null");
        return applicationComponent;
    }
}
