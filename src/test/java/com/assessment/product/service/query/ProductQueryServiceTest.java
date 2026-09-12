package com.assessment.product.service.query;

import com.assessment.product.dto.common.PagedResponse;
import com.assessment.product.dto.product.ProductMetricsResponse;
import com.assessment.product.dto.product.ProductResponse;
import com.assessment.product.entity.Product;
import com.assessment.product.exception.BadRequestException;
import com.assessment.product.exception.ResourceNotFoundException;
import com.assessment.product.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductQueryServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductQueryServiceImpl queryService;

    private Product product1;
    private Product product2;

    @BeforeEach
    void setUp() {
        product1 = Product.builder()
                .id(1)
                .name("Monitor")
                .description("4K Monitor")
                .price(new BigDecimal("300.00"))
                .createdAt(new Date())
                .build();

        product2 = Product.builder()
                .id(2)
                .name("Mouse")
                .description("Gaming Mouse")
                .price(new BigDecimal("50.00"))
                .createdAt(new Date())
                .build();
    }

    @Test
    @DisplayName("getProductById() - Success")
    void getProductById_Success() {
        when(productRepository.findById(1)).thenReturn(Optional.of(product1));

        ProductResponse response = queryService.getProductById(1);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1);
        assertThat(response.getName()).isEqualTo("Monitor");
    }

    @Test
    @DisplayName("getProductById() - Throws ResourceNotFoundException")
    void getProductById_NotFound() {
        when(productRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> queryService.getProductById(99));
    }

    @Test
    @DisplayName("getAllProducts() - Success")
    void getAllProducts_Success() {
        when(productRepository.findAll()).thenReturn(Arrays.asList(product1, product2));

        List<ProductResponse> list = queryService.getAllProducts();

        assertThat(list).hasSize(2);
    }

    @Test
    @DisplayName("searchProducts() - Success")
    void searchProducts_Success() {
        Page<Product> page = new PageImpl<>(Collections.singletonList(product1));
        when(productRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        PagedResponse<ProductResponse> result = queryService.searchProducts(
                "Monitor", BigDecimal.ZERO, new BigDecimal("500"), 0, 10, "id", "desc");

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("searchProducts() - Throws BadRequestException when min > max")
    void searchProducts_InvalidPrice() {
        assertThrows(BadRequestException.class, () ->
                queryService.searchProducts(null, new BigDecimal("500"), new BigDecimal("100"), 0, 10, "id", "desc"));
    }

    @Test
    @DisplayName("calculateMetricsAsync() - Success with products")
    void calculateMetricsAsync_Success() throws Exception {
        when(productRepository.findAll()).thenReturn(Arrays.asList(product1, product2));

        CompletableFuture<ProductMetricsResponse> future = queryService.calculateMetricsAsync();
        ProductMetricsResponse metrics = future.get();

        assertThat(metrics).isNotNull();
        assertThat(metrics.getTotalProducts()).isEqualTo(2);
        assertThat(metrics.getTotalValuation()).isEqualByComparingTo("350.00");
        assertThat(metrics.getAveragePrice()).isEqualByComparingTo("175.00");
        assertThat(metrics.getMinPrice()).isEqualByComparingTo("50.00");
        assertThat(metrics.getMaxPrice()).isEqualByComparingTo("300.00");
    }

    @Test
    @DisplayName("calculateMetricsAsync() - Empty product list")
    void calculateMetricsAsync_Empty() throws Exception {
        when(productRepository.findAll()).thenReturn(Collections.emptyList());

        CompletableFuture<ProductMetricsResponse> future = queryService.calculateMetricsAsync();
        ProductMetricsResponse metrics = future.get();

        assertThat(metrics).isNotNull();
        assertThat(metrics.getTotalProducts()).isEqualTo(0);
        assertThat(metrics.getTotalValuation()).isEqualTo(BigDecimal.ZERO);
    }
}
