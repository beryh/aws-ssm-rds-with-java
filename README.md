# Java Spring에서 AWS SDK와 Secrets Manager를 이용하여 RDS 연결하기

---
## 사용된 서비스
- [CloudFormation](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/Welcome.html)
- [AWS SDK v2](https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/home.html)
- [Secrets Manager](https://docs.aws.amazon.com/secretsmanager/latest/userguide/getting-started.html)
- [AWS RDS](https://docs.aws.amazon.com/AmazonRDS/latest/UserGuide/Welcome.html)

## 아키텍쳐
![architecture](img/arch.png)

## 인프라 배포
인프라 설정과 배포는 /cfn/core.yml 파일을 통해 설정할 수 있습니다. CloudFormation에서 해당 파일을 업로드 합니다.

해당 설정을 통해 VPC 및 Subnet, Security Group 이외에도 CI/CD 구축과 ECS Fargate를 위한 설정 등이 포함되어 있습니다.

관련한 자세한 내용은 [AWS Modern Application Workshop](https://github.com/aws-samples/aws-modern-application-workshop/tree/java)을 참고해주세요.

이렇게 배포된 인프라를 대상으로 애플리케이션이 실행됩니다.

생성된 VPC의 Private Subnet에서 Java Application을 기동합니다. Fargate 혹은 EC2 인스턴스에 애플리케이션을 배포합니다.

## Credentials
설정을 수행한 계정의 Identity로 배포 환경에 로그인 합니다. AWS Console 에서 Secret Access Key를 생성할 수 있습니다. [참고](https://docs.aws.amazon.com/cli/latest/userguide/cli-configure-quickstart.html#cli-configure-quickstart-creds) 
```bash
$ aws configure
AWS Access Key ID [None]: AKIAIOSFODNN7EXAMPLE
AWS Secret Access Key [None]: wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY
Default region name [None]: us-west-2
Default output format [None]: json
```

## Application
Java Spring Boot 2.x 를 기준으로 작성되었습니다.

해당 애플리케이션은 정상적인 AWS SDK 활용을 위해 구동 환경에 AWS Credentials 설정이 되어있어야 합니다.

아래 코드는 현재 세션의 Default Credential Provider로 설정되며, ```SecretsManagerClientBuilder.credentialsProvider()``` 프로퍼티 설정을 통해 Credential Provider를 설정할 수 있습니다. 

AWS SDK 를 통해 SecretsManager에 저장된 Secret Value (현재 데모에서는 RDS Connection 정보)를 조회하고 가져옵니다.

```Java
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
```
아래와 같이 **기본 DataSource Bean**을 대체하여 데이터베이스 연결 정보를 설정합니다.

Secret Value를 가져오는 과정이 실패한다면, Memory DB를 기동합니다.

```Java
@Bean
@Primary
public DataSource appDataSource() {
try {
    final SecretForDatabase secret = getSecret();
    return DataSourceBuilder
        .create()
        .driverClassName("com.mysql.cj.jdbc.Driver")
        .url("jdbc:" + secret.engine + "://" + secret.host + ":" + secret.port + "/" + secret.dbname)
        .username(secret.username)
        .password(secret.password)
        .build();
    } catch (ResourceNotFoundException e) {
        log.info("exception : " + e.getMessage());
        return new EmbeddedDatabaseBuilder()
            .setName("Myth")
            .setType(EmbeddedDatabaseType.HSQL)
            .addScripts("classpath:schema-h2.sql")
            .addScripts("classpath:data-h2.sql")
            .build();
        }
    }
}
```

## Initial Data Load & Test Application
초기 적재 데이터는 /rds/init.sql 파일을 실행하여 적재할 수 있습니다.

이후 적재된 데이터는 서비스를 기동중인 서버 혹은 Load Balance 주소 뒤에 /api/mythfits 명령어를 통해 데이터를 조회할 수 있습니다.

## What for next step?
1. 본문에 소개된 [워크샵](https://github.com/aws-samples/aws-modern-application-workshop/tree/java)을 참고하여 Fargate 환경에 애플리케이션을 배포해보세요.
2. ECS 환경에서는 Secrets Manager를 통해 저장된 연결 정보를 컨테이너 환경변수 형태로 전달할 수 있습니다. ECS의 Task Definition과 Dockerfile의 ENV 설정을 참고해보세요. 
3. Swagger를 통해 API 명세를 만들고, 이를 활용하여 API Gateway를 만들어 API를 관리할 수 있습니다.
