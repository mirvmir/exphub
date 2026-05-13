package io.github.mirvmir;

import io.github.mirvmir.activity.application.ActivityModuleConfig;
import io.github.mirvmir.config.InfraConfig;
import io.github.mirvmir.config.SchedulerConfig;
import io.github.mirvmir.common.properties.CommonModuleConfig;
import io.github.mirvmir.payment.application.PaymentModuleConfig;
import io.github.mirvmir.catalog.application.CatalogModuleConfig;
import io.github.mirvmir.course.application.CourseModuleConfig;
import io.github.mirvmir.enrollment.application.EnrollmentModuleConfig;
import io.github.mirvmir.identity.application.IdentityModuleConfig;
import io.github.mirvmir.media.application.MediaModuleConfig;
import io.github.mirvmir.practice.application.PracticeModuleConfig;
import io.github.mirvmir.profile.application.ProfileModuleConfig;
import io.github.mirvmir.review.application.ReviewModuleConfig;
import io.github.mirvmir.taxonomy.application.TaxonomyModuleConfig;
import io.github.mirvmir.wallet.application.WalletModuleConfig;
import org.springframework.context.annotation.*;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@PropertySource("classpath:application.properties")
@Import({
        InfraConfig.class,
        SchedulerConfig.class,

        ActivityModuleConfig.class,
        CatalogModuleConfig.class,
        CourseModuleConfig.class,
        EnrollmentModuleConfig.class,
        IdentityModuleConfig.class,
        MediaModuleConfig.class,
        PaymentModuleConfig.class,
        PracticeModuleConfig.class,
        ProfileModuleConfig.class,
        ReviewModuleConfig.class,
        TaxonomyModuleConfig.class,
        WalletModuleConfig.class,
        CommonModuleConfig.class
})
@EnableTransactionManagement
@Configuration
public class AppConfig {

    @Bean
    public static PropertySourcesPlaceholderConfigurer configurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }
}