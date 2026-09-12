package com.assessment.product.service.command;

import com.assessment.product.dto.product.ProductRequest;
import com.assessment.product.dto.product.ProductResponse;
import com.assessment.product.entity.Product;
import com.assessment.product.exception.ResourceNotFoundException;
import com.assessment.product.repository.ProductRepository;
import com.assessment.product.service.async.AsyncAuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductCommandServiceImpl implements ProductCommandService {

    private final ProductRepository productRepository;
    private final AsyncAuditLogService auditLogService;

    @Override
    @Transactional
    @CacheEvict(value = {"products", "product-search"}, allEntries = true)
    public ProductResponse createProduct(ProductRequest request, String initiatedBy) {
        log.info("Creating product: {}", request.getName());

        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .build();

        Product savedProduct = productRepository.save(product);
        auditLogService.logAction("CREATE", "Product", String.valueOf(savedProduct.getId()), initiatedBy);

        return ProductResponse.fromEntity(savedProduct);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"products", "product-search"}, allEntries = true)
    public ProductResponse updateProduct(int id, ProductRequest request, String initiatedBy) {
        log.info("Updating product id: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());

        Product updatedProduct = productRepository.save(product);
        auditLogService.logAction("UPDATE", "Product", String.valueOf(id), initiatedBy);

        return ProductResponse.fromEntity(updatedProduct);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"products", "product-search"}, allEntries = true)
    public void deleteProduct(int id, String initiatedBy) {
        log.info("Deleting product id: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));

        productRepository.delete(product);
        auditLogService.logAction("DELETE", "Product", String.valueOf(id), initiatedBy);
    }
}
