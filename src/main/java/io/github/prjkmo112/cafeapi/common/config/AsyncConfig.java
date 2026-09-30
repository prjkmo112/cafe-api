package io.github.prjkmo112.cafeapi.common.config;

import io.github.prjkmo112.cafeapi.common.config.constant.ConfigConstant;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.function.Supplier;

@Configuration
@EnableAsync
public class AsyncConfig {

    public static final String KAFKA_PUBLISH_EXECUTOR = "kafkaPublishExecutor";
    private static final int CORE_POOL_SIZE = 4;            // 평소 유지하는 스레드 수
    private static final int MAX_POOL_SIZE = 8;             // 최대 스레드 수 (코어를 넘는 스레드는 큐가 가득 찬 뒤에야 생성)
    private static final int QUEUE_CAPACITY = 100;          // 큐 용량 (코어 스레드가 다 바쁠 때 대기시킬 작업 수)

    // 큐가 가득 찼을 때의 처리 정책
    // AbortPolicy: 작업을 거부하고 RejectedExecutionException 발생
    // CallerRunsPolicy: 새로운 스레드를 만들지 않고 호출한 스레드에서 작업을 실행
    // DiscardOldestPolicy: 가장 오래된 작업을 버리고 새 작업을 수락
    // DiscardPolicy: 작업을 버림
    private static final String REJECTED_EXECUTION_HANDLER_TYPE = "DiscardPolicy";

    @Bean(name = KAFKA_PUBLISH_EXECUTOR)
    public ThreadPoolTaskExecutor kafkaPublishExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        executor.setThreadNamePrefix("kafka-pub-");         // 로그에 kafka-pub-~~~ 이런 식으로 붙기 때문에 추적하기 좋음
        executor.setCorePoolSize(CORE_POOL_SIZE);
        executor.setMaxPoolSize(MAX_POOL_SIZE);
        executor.setQueueCapacity(QUEUE_CAPACITY);

        Supplier<RejectedExecutionHandler> supplier = ConfigConstant.REJECTED_HANDLERS.get(REJECTED_EXECUTION_HANDLER_TYPE);
        if (supplier == null) {
            throw new IllegalArgumentException("잘못된 RejectedExecutionHandler 타입입니다.");
        }
        RejectedExecutionHandler handler = supplier.get();
        executor.setRejectedExecutionHandler(handler);

        executor.setWaitForTasksToCompleteOnShutdown(true);     // 모든 작업이 완료될 때까지 스레드 풀을 종료하지 않음
        executor.setAwaitTerminationSeconds(10);                // 최대 10초까지 기다림

        return executor;
    }

}
