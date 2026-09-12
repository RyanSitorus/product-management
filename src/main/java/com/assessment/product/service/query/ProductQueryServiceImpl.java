package com.assessment.product.service.query;

import com.assessment.product.dto.common.PagedResponse;
import com.assessment.product.dto.product.ProductMetricsResponse;
import com.assessment.product.dto.product.ProductResponse;
import com.assessment.product.entity.Product;
import com.assessment.product.exception.BadRequestException;
import com.assessment.product.exception.ResourceNotFoundException;
import com.assessment.product.repository.ProductRepository;
import com.assessment.product.repository.ProductSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductQueryServiceImpl implements ProductQueryService {

    private final ProductRepository productRepository;

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "products", key = "#id")
    public ProductResponse getProductById(int id) {
        log.info("Fetching product id: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));

        return ProductResponse.fromEntity(product);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "products", key = "'all'")
    public List<ProductResponse> getAllProducts() {
        log.info("Fetching all products");
        return productRepository.findAll().stream()
                .map(ProductResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "product-search", key = "{#name, #minPrice, #maxPrice, #page, #size, #sortBy, #sortDir}")
    public PagedResponse<ProductResponse> searchProducts(
            String name,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            int page,
            int size,
            String sortBy,
            String sortDir) {
        log.info("Searching products: name={}, minPrice={}, maxPrice={}", name, minPrice, maxPrice);

        if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
            throw new BadRequestException("Minimum price cannot be greater than maximum price");
        }

        List<String> allowedSortFields = List.of("id", "name", "description", "price", "createdAt");
        String validatedSortBy = (sortBy != null && allowedSortFields.contains(sortBy)) ? sortBy : "id";
        Sort sort = "asc".equalsIgnoreCase(sortDir) ? Sort.by(validatedSortBy).ascending() : Sort.by(validatedSortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Specification<Product> spec = ProductSpecification.filter(name, minPrice, maxPrice);
        Page<Product> productPage = productRepository.findAll(spec, pageable);

        Page<ProductResponse> responsePage = productPage.map(ProductResponse::fromEntity);
        return PagedResponse.fromPage(responsePage);
    }

    @Override
    @Async("taskExecutor")
    @Transactional(readOnly = true)
    public CompletableFuture<ProductMetricsResponse> calculateMetricsAsync() {
        log.info("Calculating product metrics");

        List<Product> products = productRepository.findAll();
        long count = products.size();

        if (count == 0) {
            ProductMetricsResponse emptyMetrics = ProductMetricsResponse.builder()
                    .totalProducts(0)
                    .averagePrice(BigDecimal.ZERO)
                    .minPrice(BigDecimal.ZERO)
                    .maxPrice(BigDecimal.ZERO)
                    .totalValuation(BigDecimal.ZERO)
                    .build();
            return CompletableFuture.completedFuture(emptyMetrics);
        }

        BigDecimal sum = products.stream()
                .map(Product::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal min = products.stream()
                .map(Product::getPrice)
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

        BigDecimal max = products.stream()
                .map(Product::getPrice)
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

        BigDecimal avg = sum.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP);

        ProductMetricsResponse metrics = ProductMetricsResponse.builder()
                .totalProducts(count)
                .averagePrice(avg)
                .minPrice(min)
                .maxPrice(max)
                .totalValuation(sum)
                .build();

        return CompletableFuture.completedFuture(metrics);
    }
}
