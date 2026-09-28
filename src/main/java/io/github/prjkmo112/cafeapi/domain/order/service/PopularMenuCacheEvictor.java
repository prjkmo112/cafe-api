package io.github.prjkmo112.cafeapi.domain.order.service;

import io.github.prjkmo112.cafeapi.common.dto.CacheNames;
import io.github.prjkmo112.cafeapi.common.dto.event.OrderPaidEvent;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class PopularMenuCacheEvictor {

    @Async
    @CacheEvict(cacheNames = CacheNames.POPULAR_MENUS, allEntries = true)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void evictPopularMenuCache(OrderPaidEvent event) {
        //
    }

}
