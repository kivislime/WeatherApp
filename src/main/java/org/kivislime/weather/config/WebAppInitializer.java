package org.kivislime.weather.config;

import org.springframework.lang.NonNull;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

public class WebAppInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {

    @Override
    protected Class<?>[] getRootConfigClasses() {
        return new Class[]{
                AppConfig.class,
                JpaConfig.class,
                SchedulingConfig.class,
                CacheConfig.class,
                ProdConfig.class,
                DevConfig.class,
                TestConfig.class};
    }

    @Override
    protected Class<?>[] getServletConfigClasses() {
        return new Class[]{WebMvcConfig.class};
    }

    @Override
    protected @NonNull String[] getServletMappings() {
        return new String[]{"/"};
    }

}
