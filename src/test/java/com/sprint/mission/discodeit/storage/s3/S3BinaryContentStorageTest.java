package com.sprint.mission.discodeit.storage.s3;

import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "discodeit.storage.type=s3",
        "discodeit.storage.s3.access-key=dummy-access-key",
        "discodeit.storage.s3.secret-key=dummy-secret-key",
        "discodeit.storage.s3.region=ap-northeast-2",
        "discodeit.storage.s3.bucket=dummy-bucket",
        "discodeit.storage.s3.presigned-url-expiration=600"
})
@ActiveProfiles("test")
class S3BinaryContentStorageTest {

    @Autowired
    BinaryContentStorage binaryContentStorage;

    @Test
    @DisplayName("storage type이 s3이면 S3BinaryContentStorage가 Bean으로 등록된다")
    void s3BinaryContentStorageBean_success() {
        assertThat(binaryContentStorage).isInstanceOf(S3BinaryContentStorage.class);
    }
}