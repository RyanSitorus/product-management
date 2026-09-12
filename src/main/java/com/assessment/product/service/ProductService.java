package com.assessment.product.service;

import com.assessment.product.dto.product.ProductRequest;
import com.assessment.product.dto.product.ProductResponse;
import com.assessment.product.entity.Product;
import com.assessment.product.exception.ResourceNotFoundException;
import com.assessment.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        log.info("Creating new product with name: {}", request.getName());

        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .build();

        Product savedProduct = productRepository.save(product);
        log.info("Product created with ID: {}", savedProduct.getId());

        return ProductResponse.fromEntity(savedProduct);
    }

    @Transactional(readOnly = true)
    public ProductResponse getProductById(int id) {
        log.info("Fetching product with ID: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));

        return ProductResponse.fromEntity(product);
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getAllProducts() {
        log.info("Fetching all products");
        return productRepository.findAll().stream()
                .map(ProductResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public ProductResponse updateProduct(int id, ProductRequest request) {
        log.info("Updating product with ID: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());

        Product updatedProduct = productRepository.save(product);
        log.info("Product updated successfully with ID: {}", updatedProduct.getId());

        return ProductResponse.fromEntity(updatedProduct);
    }

    @Transactional
    public void deleteProduct(int id) {
        log.info("Deleting product with ID: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));

        productRepository.delete(product);
        log.info("Product deleted successfully with ID: {}", id);
    }
}
