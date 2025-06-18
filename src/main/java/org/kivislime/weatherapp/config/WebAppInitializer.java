package org.kivislime.weatherapp.config;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

public class WebAppInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {

    @Override
    public void onStartup(@NonNull ServletContext servletContext) throws ServletException {
        String profiles = System.getProperty("spring.profiles.active");
        if (profiles == null || profiles.isEmpty()) {
            profiles = System.getenv("SPRING_PROFILES_ACTIVE");
        }
        if (profiles != null && !profiles.isEmpty()) {
            servletContext.setInitParameter("spring.profiles.active", profiles);
        } else {
            servletContext.setInitParameter("spring.profiles.active", "dev");
        }
        super.onStartup(servletContext);
    }

    @Override
    protected Class<?>[] getRootConfigClasses() {
        return new Class[]{
                AppConfig.class,
                JpaConfig.class,
                SchedulingConfig.class,
                CacheConfig.class,
                ProdConfig.class,
                DevConfig.class,};
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
