package com.assessment.product.service.query;

import com.assessment.product.dto.common.PagedResponse;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
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
}
