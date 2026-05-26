package com.sprint.mission.discodeit.storage.s3;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

public class AWSS3Test {

  // TODO: .env 파일에서 AWS 설정값을 읽어오는 유틸 메서드를 작성하세요.
  // java.util.Properties 클래스의 load() 메서드를 활용합니다.
  private static Properties loadEnv() throws IOException {
    Properties props = new Properties();

    try (FileInputStream fis = new FileInputStream(".env")) {
      props.load(fis);
    }

    return props;
  }

  // TODO: Properties를 받아 S3Client를 생성하는 메서드를 작성하세요.
  // 필요한 정보: region, access-key, secret-key
  // 힌트: S3Client.builder() → .region() → .credentialsProvider() → .build()
  private static S3Client buildClient(Properties props) {

    String accessKey = props.getProperty("AWS_ACCESS_KEY");
    String secretKey = props.getProperty("AWS_SECRET_KEY");
    String regionString = props.getProperty("AWS_REGION");

    AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);

    return S3Client.builder()
        .region(Region.of(regionString)) // 리전 세팅
        .credentialsProvider(StaticCredentialsProvider.create(credentials)) // 인증 정보 세팅
        .build();


  }

}