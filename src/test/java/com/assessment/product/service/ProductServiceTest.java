package com.assessment.product.service;

import com.assessment.product.dto.common.PagedResponse;
import com.assessment.product.dto.product.ProductRequest;
import com.assessment.product.dto.product.ProductResponse;
import com.assessment.product.service.command.ProductCommandService;
import com.assessment.product.service.query.ProductQueryService;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductCommandService commandService;

    @Mock
    private ProductQueryService queryService;

    @InjectMocks
    private ProductService productService;

    private ProductResponse sampleResponse;
    private ProductRequest sampleRequest;

    @BeforeEach
    void setUp() {
        sampleResponse = ProductResponse.builder()
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
    @DisplayName("createProduct() - Delegates to commandService")
    void createProduct_Delegates() {
        when(commandService.createProduct(eq(sampleRequest), anyString())).thenReturn(sampleResponse);

        ProductResponse response = productService.createProduct(sampleRequest);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1);
        verify(commandService).createProduct(eq(sampleRequest), anyString());
    }

    @Test
    @DisplayName("getProductById() - Delegates to queryService")
    void getProductById_Delegates() {
        when(queryService.getProductById(1)).thenReturn(sampleResponse);

        ProductResponse response = productService.getProductById(1);

        assertThat(response).isNotNull();
        verify(queryService).getProductById(1);
    }

    @Test
    @DisplayName("getAllProducts() - Delegates to queryService")
    void getAllProducts_Delegates() {
        when(queryService.getAllProducts()).thenReturn(Collections.singletonList(sampleResponse));

        List<ProductResponse> responses = productService.getAllProducts();

        assertThat(responses).hasSize(1);
        verify(queryService).getAllProducts();
    }

    @Test
    @DisplayName("searchProducts() - Delegates to queryService")
    void searchProducts_Delegates() {
        PagedResponse<ProductResponse> pagedResponse = PagedResponse.<ProductResponse>builder()
                .content(Collections.singletonList(sampleResponse))
                .pageNumber(0)
                .pageSize(10)
                .totalElements(1L)
                .totalPages(1)
                .last(true)
                .build();

        when(queryService.searchProducts("Smart", BigDecimal.ZERO, new BigDecimal("1000"), 0, 10, "id", "desc"))
                .thenReturn(pagedResponse);

        PagedResponse<ProductResponse> result = productService.searchProducts(
                "Smart", BigDecimal.ZERO, new BigDecimal("1000"), 0, 10, "id", "desc");

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(queryService).searchProducts("Smart", BigDecimal.ZERO, new BigDecimal("1000"), 0, 10, "id", "desc");
    }

    @Test
    @DisplayName("updateProduct() - Delegates to commandService")
    void updateProduct_Delegates() {
        when(commandService.updateProduct(eq(1), eq(sampleRequest), anyString())).thenReturn(sampleResponse);

        ProductResponse response = productService.updateProduct(1, sampleRequest);

        assertThat(response).isNotNull();
        verify(commandService).updateProduct(eq(1), eq(sampleRequest), anyString());
    }

    @Test
    @DisplayName("deleteProduct() - Delegates to commandService")
    void deleteProduct_Delegates() {
        doNothing().when(commandService).deleteProduct(eq(1), anyString());

        productService.deleteProduct(1);

        verify(commandService).deleteProduct(eq(1), anyString());
    }
}
