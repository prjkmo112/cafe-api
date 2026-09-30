package io.github.prjkmo112.cafeapi.common.config.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.Supplier;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ConfigConstant {

    public static final Map<String, Supplier<RejectedExecutionHandler>> REJECTED_HANDLERS = Map.of(
            "AbortPolicy", ThreadPoolExecutor.AbortPolicy::new,
            "CallerRunsPolicy", ThreadPoolExecutor.CallerRunsPolicy::new,
            "DiscardOldestPolicy", () -> withLog(new ThreadPoolExecutor.DiscardOldestPolicy()),
            "DiscardPolicy", () -> withLog(new ThreadPoolExecutor.DiscardPolicy())
    );

    private static RejectedExecutionHandler withLog(RejectedExecutionHandler handler) {
        return (task, executor) -> {
            log.warn("스레드 풀에 자리가 없습니다.");
            handler.rejectedExecution(task, executor);
        };
    }

}
