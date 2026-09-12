package com.assessment.product.service;

import com.assessment.product.dto.common.PagedResponse;
import com.assessment.product.dto.product.ProductMetricsResponse;
import com.assessment.product.dto.product.ProductRequest;
import com.assessment.product.dto.product.ProductResponse;
import com.assessment.product.service.command.ProductCommandService;
import com.assessment.product.service.query.ProductQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductCommandService commandService;
    private final ProductQueryService queryService;

    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getName())) {
            return authentication.getName();
        }
        return "system";
    }

    public ProductResponse createProduct(ProductRequest request) {
        return commandService.createProduct(request, getCurrentUsername());
    }

    public ProductResponse createProduct(ProductRequest request, String initiatedBy) {
        return commandService.createProduct(request, initiatedBy);
    }

    public ProductResponse getProductById(int id) {
        return queryService.getProductById(id);
    }

    public List<ProductResponse> getAllProducts() {
        return queryService.getAllProducts();
    }

    public PagedResponse<ProductResponse> searchProducts(
            String name,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            int page,
            int size,
            String sortBy,
            String sortDir) {
        return queryService.searchProducts(name, minPrice, maxPrice, page, size, sortBy, sortDir);
    }

    public ProductResponse updateProduct(int id, ProductRequest request) {
        return commandService.updateProduct(id, request, getCurrentUsername());
    }

    public ProductResponse updateProduct(int id, ProductRequest request, String initiatedBy) {
        return commandService.updateProduct(id, request, initiatedBy);
    }

    public void deleteProduct(int id) {
        commandService.deleteProduct(id, getCurrentUsername());
    }

    public void deleteProduct(int id, String initiatedBy) {
        commandService.deleteProduct(id, initiatedBy);
    }

    public CompletableFuture<ProductMetricsResponse> calculateMetricsAsync() {
        return queryService.calculateMetricsAsync();
    }
}
