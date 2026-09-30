package io.github.prjkmo112.cafeapi.domain.order.service;

import io.github.prjkmo112.cafeapi.common.dto.CacheNames;
import io.github.prjkmo112.cafeapi.common.dto.event.OrderPaidEvent;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class PopularMenuCacheEvictor {

    // 주문 커밋 후 인기 메뉴 캐시 무효화 (본문 없이 어노테이션이 동작을 수행)
    @CacheEvict(cacheNames = CacheNames.POPULAR_MENUS, allEntries = true)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void evictPopularMenuCache(OrderPaidEvent event) {
        //
    }

}
