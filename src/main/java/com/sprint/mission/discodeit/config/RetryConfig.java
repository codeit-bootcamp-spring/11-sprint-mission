package com.sprint.mission.discodeit.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;

/**
 * 비동기로 처리되는 바이너리 데이터 저장 로직(S3 업로드 등)에 자동 재시도를 적용하기 위한 설정입니다.
 */
@Configuration
@EnableRetry
public class RetryConfig {

}
