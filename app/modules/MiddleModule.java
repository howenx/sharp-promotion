package modules;

import com.google.inject.AbstractModule;
import play.Configuration;
import play.Environment;

/**
 * 中间层module
 * Created by howen on 16/2/28.
 */
public class MiddleModule extends AbstractModule {

    private final Environment environment;
    private final Configuration configuration;

    public MiddleModule(
            Environment environment,
            Configuration configuration) {
        this.environment = environment;
        this.configuration = configuration;
    }

    protected void configure() {
        bind(SysParCom.class).asEagerSingleton();
    }
}