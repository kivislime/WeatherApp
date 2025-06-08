package org.kivislime.weather.config;

import jakarta.servlet.Filter;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import org.springframework.web.filter.DelegatingFilterProxy;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

public class WebAppInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {

    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {
        String jvmProfile = System.getProperty("spring.profiles.active");
        if (jvmProfile != null && !jvmProfile.isBlank()) {
            servletContext.setInitParameter("spring.profiles.active", jvmProfile);
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
                DevConfig.class};
    }

    @Override
    protected Class<?>[] getServletConfigClasses() {
        return new Class[]{WebMvcConfig.class};
    }

    @Override
    protected String[] getServletMappings() {
        return new String[]{"/"};
    }

}
