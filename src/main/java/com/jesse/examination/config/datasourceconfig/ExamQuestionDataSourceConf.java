package com.jesse.examination.config.datasourceconfig;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 主数据源配置类，为将来多数据源做准备。
 */
@Primary
@Configuration
@EnableJpaRepositories(
        basePackages = {
                "com.jesse.examination.question.repository",
                "com.jesse.examination.scorerecord.repository",
                "com.jesse.examination.user.repository"
        },
        entityManagerFactoryRef = "examManagerFactory",
        transactionManagerRef   = "examTransactionManager"
)
@EntityScan(
        basePackages = {
                "com.jesse.examination.question.entity.questionentity",
                "com.jesse.examination.question.entity.optionentity",
                "com.jesse.examination.scorerecord.entity",
                "com.jesse.examination.user.entity"
        }
)
public class ExamQuestionDataSourceConf
{
    @Bean(name = "examDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.primary")
    public DataSource examDataSource()
    {
        return DataSourceBuilder.create()
                                .type(HikariDataSource.class)
                                .build();
    }

    @Bean(name = "examManagerFactory")
    public LocalContainerEntityManagerFactoryBean
    examManagerFactory(
            @Qualifier(value = "examDataSource")
            DataSource dataSource
    )
    {
        var entityFacManagerBean
                = new LocalContainerEntityManagerFactoryBean();

        entityFacManagerBean.setDataSource(dataSource);
        entityFacManagerBean.setPackagesToScan(
                "com.jesse.examination.question.entity.questionentity",
                "com.jesse.examination.question.entity.optionentity",
                "com.jesse.examination.scorerecord.entity",
                "com.jesse.examination.user.entity"
        );
        entityFacManagerBean.setJpaVendorAdapter(new HibernateJpaVendorAdapter());

        Map<String, Object> properties = new HashMap<>();

        properties.put("hibernate.hbm2ddl.auto", "validate");

        entityFacManagerBean.setJpaPropertyMap(properties);

        return entityFacManagerBean;
    }

    @Bean(name = "examTransactionManager")
    public PlatformTransactionManager
    examTransactionManager(
            @Qualifier(value = "examManagerFactory")
            LocalContainerEntityManagerFactoryBean
            entityFacManagerBean
    )
    {
        return new JpaTransactionManager(
                Objects.requireNonNull(
                        entityFacManagerBean.getObject()
                )
        );
    }
}
