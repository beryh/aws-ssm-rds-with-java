package io.hochya.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;
import software.amazon.awssdk.services.secretsmanager.model.ResourceNotFoundException;

import javax.sql.DataSource;

@Slf4j
@Configuration
public class DataSourceConfiguration {
    @Value("${rds-secret}")
    private String secretName;

    @Bean
    @Primary
    public DataSource appDataSource() {
        try {
            final SecretForDatabase secret = getSecret();
            return DataSourceBuilder
                    .create()
                    .driverClassName("com.mysql.jdbc.Driver")
                    .url("jdbc:" + secret.engine + "://" + secret.host + ":" + secret.port + "/" + secret.dbname)
                    .username(secret.username)
                    .password(secret.password)
                    .build();
        } catch (ResourceNotFoundException e) {
            log.info("exception : " + e.getMessage());
            return new EmbeddedDatabaseBuilder()
//                    .setName("mythfits")
                    .setType(EmbeddedDatabaseType.HSQL)
                    .addScripts("classpath:schema-h2.sql")
                    .addScripts("classpath:data-h2.sql")
                    .build();
        }
    }

    private SecretForDatabase getSecret() {
        SecretsManagerClient client = SecretsManagerClient.builder()
                .build();

        GetSecretValueRequest valueRequest = GetSecretValueRequest.builder()
                .secretId(secretName)
                .build();
        GetSecretValueResponse valueResponse = client.getSecretValue(valueRequest);

        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(valueResponse.secretString(), SecretForDatabase.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class SecretForDatabase {
        private String username;
        private String password;
        private String engine;
        private String host;
        private Integer port;
        private String dbClusterIdentifier;
        private String dbname;
    }
}
