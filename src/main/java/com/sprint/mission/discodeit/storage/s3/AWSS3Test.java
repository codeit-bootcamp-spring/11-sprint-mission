package com.sprint.mission.discodeit.storage.s3;

// 의존성 추가: implementation 'software.amazon.awssdk:s3:2.31.7'

import org.junit.jupiter.api.Test;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.Properties;
import java.util.UUID;

public class AWSS3Test {

    // TODO: .env 파일에서 AWS 설정값을 읽어오는 유틸 메서드를 작성하세요.
    // java.util.Properties 클래스의 load() 메서드를 활용합니다.
    private static Properties loadEnv() throws IOException {
        Properties props = new Properties();
        props.load(new FileInputStream(
                "C:/Users/dkskr/Downloads/0-sprint-mission-s8-v1-base/0-sprint-mission-s8-v1-base/.env"
        ));
        return props;
    }

    // TODO: Properties를 받아 S3Client를 생성하는 메서드를 작성하세요.
    // 필요한 정보: region, access-key, secret-key
    // 힌트: S3Client.builder() → .region() → .credentialsProvider() → .build()
    private static S3Client buildClient(Properties props) {
        return S3Client.builder()
                .region(Region.of(props.getProperty("AWS_S3_REGION")))
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(
                                        props.getProperty("AWS_S3_ACCESS_KEY"),
                                        props.getProperty("AWS_S3_SECRET_KEY")
                                )
                        )
                )
                .build();
    }

    // TODO: 파일 업로드 테스트를 작성하세요.
    // UUID를 키로 사용해 S3에 텍스트 파일을 업로드합니다.
    // 힌트: s3Client.putObject(PutObjectRequest, RequestBody)
    @Test
    void upload() throws IOException {
        Properties props = loadEnv();
        S3Client s3 = buildClient(props);
        String bucket = props.getProperty("AWS_S3_BUCKET");

        String key = UUID.randomUUID().toString();
        byte[] content = "Hello S3!".getBytes();

        s3.putObject(
                PutObjectRequest.builder()
                        .bucket(bucket)
                        .key(key)
                        .build(),
                RequestBody.fromBytes(content)
        );
        System.out.println("업로드 성공 key: " + key);
    }

    // TODO: 파일 다운로드 테스트를 작성하세요.
    // 업로드한 UUID 키로 파일을 내려받아 내용을 출력합니다.
    // 힌트: s3Client.getObject(GetObjectRequest) → ResponseInputStream
    @Test
    void download() throws IOException {
        Properties props = loadEnv();
        S3Client s3 = buildClient(props);
        String bucket = props.getProperty("AWS_S3_BUCKET");

        // 다운로드 전 업로드 먼저
        String key = UUID.randomUUID().toString();
        s3.putObject(
                PutObjectRequest.builder()
                        .bucket(bucket)
                        .key(key)
                        .build(),
                RequestBody.fromBytes("Hello S3!".getBytes())
        );

        // 다운로드
        ResponseInputStream<GetObjectResponse> response = s3.getObject(
                GetObjectRequest.builder()
                        .bucket(bucket)
                        .key(key)
                        .build()
        );
        System.out.println("다운로드 성공: " + new String(response.readAllBytes()));
    }

    // TODO: PresignedUrl 생성 테스트를 작성하세요.
    // 10분간 유효한 URL을 생성하고 콘솔에 출력합니다.
    // S3Client가 아닌 S3Presigner를 별도로 생성해야 합니다.
    // 생성된 URL을 브라우저에 붙여넣어서 실제로 파일이 다운로드되는지 확인해보세요.
    @Test
    void presignedUrl() throws IOException {
        Properties props = loadEnv();
        S3Client s3 = buildClient(props);
        String bucket = props.getProperty("AWS_S3_BUCKET");

        // PresignedUrl 전 업로드 먼저
        String key = UUID.randomUUID().toString();
        s3.putObject(
                PutObjectRequest.builder()
                        .bucket(bucket)
                        .key(key)
                        .build(),
                RequestBody.fromBytes("Hello S3!".getBytes())
        );

        // PresignedUrl 생성
        S3Presigner presigner = S3Presigner.builder()
                .region(Region.of(props.getProperty("AWS_S3_REGION")))
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(
                                        props.getProperty("AWS_S3_ACCESS_KEY"),
                                        props.getProperty("AWS_S3_SECRET_KEY")
                                )
                        )
                )
                .build();

        String url = presigner.presignGetObject(
                GetObjectPresignRequest.builder()
                        .signatureDuration(Duration.ofMinutes(10))
                        .getObjectRequest(
                                GetObjectRequest.builder()
                                        .bucket(bucket)
                                        .key(key)
                                        .build()
                        )
                        .build()
        ).url().toString();

        System.out.println("PresignedUrl: " + url);
    }
}