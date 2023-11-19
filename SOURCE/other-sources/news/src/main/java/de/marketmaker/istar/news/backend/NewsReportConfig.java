/*
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.news.backend;

import java.util.List;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.PropertySource;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

@Configuration
@PropertySource("file:///${user.home}/produktion/prog/istar-news/conf/default.properties")
@ImportResource("classpath:/de/marketmaker/istar/news/backend/news-report-config.xml")
public class NewsReportConfig {

    @Value("${db.url}")
    private String url;

    @Value("${db.user}")
    private String username;

    @Value("${db.password}")
    private String password;

    @Autowired
    private List<NewsReportItem> configs;

    @Bean
    public DataSource dataSource() {
        SingleConnectionDataSource dataSource = new SingleConnectionDataSource();
        dataSource.setUrl(this.url);
        dataSource.setUsername(this.username);
        dataSource.setPassword(this.password);
        return dataSource;
    }

    @Bean
    public NewsDao newsDao() {
        final NewsDaoDb newsDaoDb = new NewsDaoDb();
        newsDaoDb.setDataSource(dataSource());
        newsDaoDb.afterPropertiesSet();
        return newsDaoDb;
    }

    @Bean
    public NewsReport newsReport() {
        return new NewsReport(newsDao(), this.configs);
    }
}
