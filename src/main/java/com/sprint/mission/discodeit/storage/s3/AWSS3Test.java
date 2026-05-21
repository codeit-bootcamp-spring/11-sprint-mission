package com.sprint.mission.discodeit.storage.s3;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Properties;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

public class AWSS3Test {

    private final String accessKey;
    private final String secretKey;
    private final String region;
    private final String bucket;

    public AWSS3Test(String accessKey, String secretKey, String region, String bucket) {
        this.accessKey = accessKey;
        this.secretKey = secretKey;
        this.region = region;
        this.bucket = bucket;
    }

    private S3Client getS3Client() {
        return S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)))
                .crossRegionAccessEnabled(true)
                .build();
    }

    public void testUpload(String key, byte[] content) {
        try (S3Client s3 = getS3Client()) {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build();
            s3.putObject(request, RequestBody.fromBytes(content));
            System.out.println("[Upload] 성공: " + key);
        }
    }

    public void testDownload(String key) {
        try (S3Client s3 = getS3Client()) {
            GetObjectRequest request = GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build();
            byte[] bytes = s3.getObjectAsBytes(request).asByteArray();
            String content = new String(bytes, StandardCharsets.UTF_8);
            System.out.println("[Download] 성공: " + key + " / 내용: " + content);
        }
    }

    public void testGeneratePresignedUrl(String key, String contentType) {
        try (S3Presigner presigner = S3Presigner.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)))
                .build()) {

            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .responseContentType(contentType)
                    .build();

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofSeconds(600))
                    .getObjectRequest(getObjectRequest)
                    .build();

            PresignedGetObjectRequest presignedRequest = presigner.presignGetObject(presignRequest);
            System.out.println("[PresignedUrl] 성공: " + presignedRequest.url());
        }
    }

    public static void main(String[] args) throws Exception {
        Properties props = new Properties();
        try (BufferedReader reader = new BufferedReader(new FileReader(".env"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                int idx = line.indexOf('=');
                if (idx > 0) {
                    props.setProperty(line.substring(0, idx).trim(), line.substring(idx + 1).trim());
                }
            }
        }

        String accessKey = props.getProperty("AWS_S3_ACCESS_KEY");
        String secretKey = props.getProperty("AWS_S3_SECRET_KEY");
        String region = props.getProperty("AWS_S3_REGION", "ap-northeast-2");
        String bucket = props.getProperty("AWS_S3_BUCKET");

        AWSS3Test test = new AWSS3Test(accessKey, secretKey, region, bucket);

        String testKey = "test/hello.txt";
        byte[] content = "Hello, S3!".getBytes(StandardCharsets.UTF_8);

        test.testUpload(testKey, content);
        test.testDownload(testKey);
        test.testGeneratePresignedUrl(testKey, "text/plain");
    }
}