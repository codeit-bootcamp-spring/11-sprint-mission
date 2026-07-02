package com.sprint.mission.discodeit.storage.s3;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.Properties;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

public class AWSS3Test {

  private static UUID uploadedKey;

  private static Properties loadEnv() throws IOException {
    Properties props = new Properties();
    props.load(new FileInputStream(".env"));
    return props;
  }

  private static S3Client buildClient(Properties props) {
    AwsBasicCredentials credentials = AwsBasicCredentials.create(
        props.getProperty("AWS_S3_ACCESS_KEY"),
        props.getProperty("AWS_S3_SECRET_KEY")
    );
    return S3Client.builder()
        .region(Region.of(props.getProperty("AWS_S3_REGION")))
        .credentialsProvider(StaticCredentialsProvider.create(credentials))
        .build();
  }

  @Test
  @DisplayName("AWS S3 upload test")
  void upload() throws IOException {
    Properties props = loadEnv();
    S3Client s3Client = buildClient(props);
    String bucket = props.getProperty("AWS_S3_BUCKET");

    uploadedKey = UUID.randomUUID();
    String content = "hello s3";

    s3Client.putObject(
        PutObjectRequest.builder()
            .bucket(bucket)
            .key(uploadedKey.toString())
            .build(),
        RequestBody.fromString(content)
    );

    System.out.println("uploaded key: " + uploadedKey);
  }

  @Test
  @DisplayName("AWS S3 download test")
  void download() throws IOException {
    Properties props = loadEnv();
    S3Client s3Client = buildClient(props);
    String bucket = props.getProperty("AWS_S3_BUCKET");

    var response = s3Client.getObject(
        GetObjectRequest.builder()
            .bucket(bucket)
            .key(uploadedKey.toString())
            .build()
    );

    String content = new String(response.readAllBytes());
    System.out.println("downloaded content: " + content);
  }

  @Test
  @DisplayName("AWS S3 presignedUrl test")
  void presignedUrl() throws IOException {
    Properties props = loadEnv();
    AwsBasicCredentials credentials = AwsBasicCredentials.create(
        props.getProperty("AWS_S3_ACCESS_KEY"),
        props.getProperty("AWS_S3_SECRET_KEY")
    );
    String bucket = props.getProperty("AWS_S3_BUCKET");

    S3Presigner presigner = S3Presigner.builder()
        .region(Region.of(props.getProperty("AWS_S3_REGION")))
        .credentialsProvider(StaticCredentialsProvider.create(credentials))
        .build();

    var presignedRequest = presigner.presignGetObject(
        GetObjectPresignRequest.builder()
            .signatureDuration(Duration.ofMinutes(10))
            .getObjectRequest(
                GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(uploadedKey.toString())
                    .build()
            )
            .build()
    );

    System.out.println("presigned url: " + presignedRequest.url());
    presigner.close();
  }
}
