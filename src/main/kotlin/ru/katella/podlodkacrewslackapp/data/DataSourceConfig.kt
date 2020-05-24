package ru.katella.podlodkacrewslackapp.data

import org.springframework.boot.jdbc.DataSourceBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.net.URI
import javax.sql.DataSource


@Configuration
class DataSourceConfig {

    @Bean
    fun getDataSource(): DataSource {
        val connectionString = System.getenv("DATABASE_URL")
        val uri = URI(connectionString)
        val dbUrl = "jdbc:postgresql://${uri.host}:${uri.port}${uri.path}"
        val (user, password) = uri.userInfo.split(":")

        val dataSourceBuilder = DataSourceBuilder.create()
        dataSourceBuilder.driverClassName("org.postgresql.Driver")
        dataSourceBuilder.url(dbUrl)
        dataSourceBuilder.username(user)
        dataSourceBuilder.password(password)
        return dataSourceBuilder.build()
    }
}