package com.assessment.product.service.async;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class AsyncAuditLogService {

    @Async("taskExecutor")
    public CompletableFuture<Void> logAction(String action, String resource, String identifier, String username) {
        log.info("Audit: {} {} (id: {}) by {}", action, resource, identifier, username);
        return CompletableFuture.completedFuture(null);
    }
}
