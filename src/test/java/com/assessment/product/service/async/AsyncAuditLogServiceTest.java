package com.assessment.product.service.async;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class AsyncAuditLogServiceTest {

    private final AsyncAuditLogService auditLogService = new AsyncAuditLogService();

    @Test
    @DisplayName("Should execute audit log action asynchronously")
    void testLogAction() {
        CompletableFuture<Void> future = auditLogService.logAction("CREATE", "PRODUCT", "1", "admin");
        assertNotNull(future);
    }
}
