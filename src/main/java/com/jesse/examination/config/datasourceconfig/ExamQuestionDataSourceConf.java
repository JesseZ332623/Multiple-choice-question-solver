package com.jesse.examination.config.datasourceconfig;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
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

/*
 * EnableJpaRepositories 注解用于描述这个数据源所有实体仓库类的绝对包地址，
 * 实体管理工厂和事务管理器的名字。
 */
@EnableJpaRepositories(
        basePackages = {
                "com.jesse.examination.question.repository",
                "com.jesse.examination.scorerecord.repository",
                "com.jesse.examination.user.repository",
                "com.jesse.examination.email.repo"
        },
        entityManagerFactoryRef = "examManagerFactory",
        transactionManagerRef   = "examTransactionManager"
)
//@EntityScan(
//        basePackages = {
//                "com.jesse.examination.question.entity.questionentity",
//                "com.jesse.examination.question.entity.optionentity",
//                "com.jesse.examination.scorerecord.entity",
//                "com.jesse.examination.user.entity"
//        }
//)
public class ExamQuestionDataSourceConf
{
    /**
     * 数据源组件配置。
     */
    @Bean(name = "examDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.primary")
    public DataSource examDataSource()
    {
        return DataSourceBuilder.create()
                                .type(HikariDataSource.class)
                                .build();
    }

    /**
     * 实体管理工厂配置。
     */
    @Bean(name = "examManagerFactory")
    public LocalContainerEntityManagerFactoryBean
    examManagerFactory(
            @Qualifier(value = "examDataSource")
            DataSource dataSource
    )
    {
        var entityFacManagerBean
                = new LocalContainerEntityManagerFactoryBean();

        // 设定数据源
        entityFacManagerBean.setDataSource(dataSource);

        // 设定本数据源所有实体所在的包的绝对路径
        entityFacManagerBean.setPackagesToScan(
                "com.jesse.examination.question.entity.questionentity",
                "com.jesse.examination.question.entity.optionentity",
                "com.jesse.examination.scorerecord.entity",
                "com.jesse.examination.user.entity",
                "com.jesse.examination.email.entity"
        );

        // 设定 JPA 提供厂商
        entityFacManagerBean.setJpaVendorAdapter(new HibernateJpaVendorAdapter());

        Map<String, Object> properties = new HashMap<>();

        // 设定 JPA 属性，在启动服务器时对数据表进行验证（不是 update 修改）。
        properties.put("hibernate.hbm2ddl.auto", "validate");

        entityFacManagerBean.setJpaPropertyMap(properties);

        return entityFacManagerBean;
    }

    /**
     * 事务管理器配置。
     */
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
