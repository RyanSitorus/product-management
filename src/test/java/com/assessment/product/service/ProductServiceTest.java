package com.assessment.product.service;

import com.assessment.product.dto.product.ProductRequest;
import com.assessment.product.dto.product.ProductResponse;
import com.assessment.product.entity.Product;
import com.assessment.product.exception.ResourceNotFoundException;
import com.assessment.product.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private Product sampleProduct;
    private ProductRequest sampleRequest;

    @BeforeEach
    void setUp() {
        sampleProduct = Product.builder()
                .id(1)
                .name("Smartphone")
                .description("Flagship mobile phone")
                .price(new BigDecimal("999.00"))
                .createdAt(new Date())
                .build();

        sampleRequest = ProductRequest.builder()
                .name("Smartphone")
                .description("Flagship mobile phone")
                .price(new BigDecimal("999.00"))
                .build();
    }

    @Test
    @DisplayName("createProduct() - Success")
    void createProduct_Success() {
        when(productRepository.save(any(Product.class))).thenReturn(sampleProduct);

        ProductResponse response = productService.createProduct(sampleRequest);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1);
        assertThat(response.getName()).isEqualTo("Smartphone");
        assertThat(response.getPrice()).isEqualByComparingTo("999.00");

        verify(productRepository).save(any(Product.class));
    }

    @Test
    @DisplayName("getProductById() - Success")
    void getProductById_Success() {
        when(productRepository.findById(1)).thenReturn(Optional.of(sampleProduct));

        ProductResponse response = productService.getProductById(1);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1);
        assertThat(response.getName()).isEqualTo("Smartphone");
    }

    @Test
    @DisplayName("getProductById() - Throws ResourceNotFoundException when not found")
    void getProductById_NotFound() {
        when(productRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> productService.getProductById(99));
    }

    @Test
    @DisplayName("getAllProducts() - Success")
    void getAllProducts_Success() {
        when(productRepository.findAll()).thenReturn(Collections.singletonList(sampleProduct));

        List<ProductResponse> responses = productService.getAllProducts();

        assertThat(responses).isNotEmpty();
        assertThat(responses.size()).isEqualTo(1);
        assertThat(responses.get(0).getName()).isEqualTo("Smartphone");
    }

    @Test
    @DisplayName("updateProduct() - Success")
    void updateProduct_Success() {
        when(productRepository.findById(1)).thenReturn(Optional.of(sampleProduct));
        when(productRepository.save(any(Product.class))).thenReturn(sampleProduct);

        ProductRequest updateRequest = ProductRequest.builder()
                .name("Updated Smartphone")
                .description("Updated specs")
                .price(new BigDecimal("1099.00"))
                .build();

        ProductResponse response = productService.updateProduct(1, updateRequest);

        assertThat(response).isNotNull();
        verify(productRepository).save(any(Product.class));
    }

    @Test
    @DisplayName("deleteProduct() - Success")
    void deleteProduct_Success() {
        when(productRepository.findById(1)).thenReturn(Optional.of(sampleProduct));
        doNothing().when(productRepository).delete(sampleProduct);

        productService.deleteProduct(1);

        verify(productRepository).delete(sampleProduct);
    }

    @Test
    @DisplayName("deleteProduct() - Throws ResourceNotFoundException when not found")
    void deleteProduct_NotFound() {
        when(productRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> productService.deleteProduct(99));
        verify(productRepository, never()).delete(any(Product.class));
    }
}
