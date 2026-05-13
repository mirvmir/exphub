package io.github.mirvmir;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@EnableWebMvc
@ComponentScan({
        "io.github.mirvmir.activity.web",
        "io.github.mirvmir.payment.web",
        "io.github.mirvmir.catalog.web",
        "io.github.mirvmir.common.exception",
        "io.github.mirvmir.course.web",
        "io.github.mirvmir.enrollment.web",
        "io.github.mirvmir.identity.web",
        "io.github.mirvmir.media.web",
        "io.github.mirvmir.practice.web",
        "io.github.mirvmir.profile.web",
        "io.github.mirvmir.review.web",
        "io.github.mirvmir.taxonomy.web",
        "io.github.mirvmir.wallet.web"
})
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:5173")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        converters.add(0, new MappingJackson2HttpMessageConverter(mapper));
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new CompletedProfileInterceptor());
    }
}