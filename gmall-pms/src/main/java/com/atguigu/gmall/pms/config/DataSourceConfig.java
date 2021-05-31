package com.atguigu.gmall.pms.config;

import com.alibaba.druid.pool.DruidDataSource;
import com.zaxxer.hikari.HikariDataSource;
import io.seata.rm.datasource.DataSourceProxy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;


@Configuration
public class DataSourceConfig {

	/**
	 * 需要将 DataSourceProxy 设置为主数据源，否则事务无法回滚
	 *
	 * @return The default datasource
	 */
	@Primary
	@Bean("dataSource")
	public DataSource dataSource(@Value("${spring.datasource.password}")String password,
								 @Value("${spring.datasource.username}")String username,
								 @Value("${spring.datasource.driver-class-name}")String driverClassName,
								 @Value("${spring.datasource.url}")String url) {
		HikariDataSource hikariDataSource = new HikariDataSource();
		hikariDataSource.setUsername(username);
		hikariDataSource.setPassword(password);
		hikariDataSource.setJdbcUrl(url);
		hikariDataSource.setDriverClassName(driverClassName);
		return new DataSourceProxy(hikariDataSource);
	}
}