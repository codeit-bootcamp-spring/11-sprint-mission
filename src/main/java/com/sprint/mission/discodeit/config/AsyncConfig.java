package com.sprint.mission.discodeit.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskDecorator;
import org.springframework.core.task.TaskExecutor;
import org.springframework.core.task.support.CompositeTaskDecorator;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.List;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "binaryContentTaskExecutor")
    public TaskExecutor binaryContentTaskExecutor(
            @Value("${discodeit.async.binary-content.core-pool-size}") int corePoolSize,
            @Value("${discodeit.async.binary-content.max-pool-size}") int maxPoolSize,
            @Value("${discodeit.async.binary-content.queue-capacity}") int queueCapacity,
            TaskDecorator taskDecorator
    ) {
        return createTaskExecutor(
                corePoolSize,
                maxPoolSize,
                queueCapacity,
                "binary-content-",
                taskDecorator
        );
    }

    @Bean(name = "eventTaskExecutor")
    public TaskExecutor eventTaskExecutor(
            @Value("${discodeit.async.event.core-pool-size}") int corePoolSize,
            @Value("${discodeit.async.event.max-pool-size}") int maxPoolSize,
            @Value("${discodeit.async.event.queue-capacity}") int queueCapacity,
            TaskDecorator taskDecorator
    ) {
        return createTaskExecutor(
                corePoolSize,
                maxPoolSize,
                queueCapacity,
                "event-",
                taskDecorator
        );
    }

    @Bean
    public TaskDecorator taskDecorator() {
        return new CompositeTaskDecorator(
                List.of(
                        new MdcTaskDecorator(),
                        new SecurityContextTaskDecorator()
                )
        );
    }

    private TaskExecutor createTaskExecutor(
            int corePoolSize,
            int maxPoolSize,
            int queueCapacity,
            String threadNamePrefix,
            TaskDecorator taskDecorator
    ) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix(threadNamePrefix);
        executor.setTaskDecorator(taskDecorator);
        executor.initialize();

        return executor;
    }
}
