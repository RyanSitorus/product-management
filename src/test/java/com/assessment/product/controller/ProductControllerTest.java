package com.assessment.product.controller;

import com.assessment.product.config.SecurityConfig;
import com.assessment.product.dto.product.ProductRequest;
import com.assessment.product.dto.product.ProductResponse;
import com.assessment.product.exception.ResourceNotFoundException;
import com.assessment.product.security.JwtAuthenticationEntryPoint;
import com.assessment.product.security.JwtAuthenticationFilter;
import com.assessment.product.security.JwtTokenProvider;
import com.assessment.product.security.UserDetailsServiceImpl;
import com.assessment.product.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
@Import(SecurityConfig.class)
@AutoConfigureMockMvc(addFilters = false)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductService productService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    @MockBean
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private ProductRequest validRequest;
    private ProductResponse sampleResponse;

    @BeforeEach
    void setUp() {
        validRequest = ProductRequest.builder()
                .name("Test Laptop")
                .description("High-performance laptop")
                .price(new BigDecimal("1299.99"))
                .build();

        sampleResponse = ProductResponse.builder()
                .id(1)
                .name("Test Laptop")
                .description("High-performance laptop")
                .price(new BigDecimal("1299.99"))
                .createdAt(new Date())
                .build();
    }

    @Test
    @DisplayName("POST /api/v1/products - Should create product successfully")
    void createProduct_Success() throws Exception {
        when(productService.createProduct(any(ProductRequest.class), anyString())).thenReturn(sampleResponse);

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("Test Laptop"));
    }

    @Test
    @DisplayName("POST /api/v1/products - Should return 400 when input validation fails")
    void createProduct_ValidationFailure() throws Exception {
        ProductRequest invalidRequest = ProductRequest.builder()
                .name("")
                .price(new BigDecimal("-10.00"))
                .build();

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.validationErrors.name").exists())
                .andExpect(jsonPath("$.validationErrors.price").exists());
    }

    @Test
    @DisplayName("GET /api/v1/products - Should retrieve all products")
    void getAllProducts_Success() throws Exception {
        when(productService.getAllProducts()).thenReturn(Collections.singletonList(sampleResponse));

        mockMvc.perform(get("/api/v1/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value(1));
    }

    @Test
    @DisplayName("GET /api/v1/products/{id} - Should return product when found")
    void getProductById_Success() throws Exception {
        when(productService.getProductById(1)).thenReturn(sampleResponse);

        mockMvc.perform(get("/api/v1/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("Test Laptop"));
    }

    @Test
    @DisplayName("GET /api/v1/products/{id} - Should return 404 when product not found")
    void getProductById_NotFound() throws Exception {
        when(productService.getProductById(99))
                .thenThrow(new ResourceNotFoundException("Product not found with ID: 99"));

        mockMvc.perform(get("/api/v1/products/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Product not found with ID: 99"));
    }

    @Test
    @DisplayName("PUT /api/v1/products/{id} - Should update product successfully")
    void updateProduct_Success() throws Exception {
        ProductResponse updatedResponse = ProductResponse.builder()
                .id(1)
                .name("Updated Laptop")
                .description("Updated description")
                .price(new BigDecimal("1499.99"))
                .createdAt(new Date())
                .build();

        when(productService.updateProduct(eq(1), any(ProductRequest.class), anyString())).thenReturn(updatedResponse);

        mockMvc.perform(put("/api/v1/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Updated Laptop"));
    }

    @Test
    @DisplayName("DELETE /api/v1/products/{id} - Should delete product successfully")
    void deleteProduct_Success() throws Exception {
        doNothing().when(productService).deleteProduct(eq(1), anyString());

        mockMvc.perform(delete("/api/v1/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Product deleted successfully"));
    }

    @Test
    @DisplayName("GET /api/v1/products/search - Should search products with filters")
    void searchProducts_Success() throws Exception {
        com.assessment.product.dto.common.PagedResponse<ProductResponse> pagedResponse =
                com.assessment.product.dto.common.PagedResponse.<ProductResponse>builder()
                        .content(Collections.singletonList(sampleResponse))
                        .pageNumber(0)
                        .pageSize(10)
                        .totalElements(1L)
                        .totalPages(1)
                        .last(true)
                        .build();

        when(productService.searchProducts(eq("Laptop"), any(), any(), eq(0), eq(10), eq("id"), eq("desc")))
                .thenReturn(pagedResponse);

        mockMvc.perform(get("/api/v1/products/search")
                        .param("name", "Laptop")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @Test
    @DisplayName("GET /api/v1/products/search - Should return 400 when minPrice > maxPrice")
    void searchProducts_InvalidPriceRange() throws Exception {
        when(productService.searchProducts(any(), any(), any(), anyInt(), anyInt(), anyString(), anyString()))
                .thenThrow(new com.assessment.product.exception.BadRequestException("Minimum price cannot be greater than maximum price"));

        mockMvc.perform(get("/api/v1/products/search")
                        .param("minPrice", "1000")
                        .param("maxPrice", "500"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Minimum price cannot be greater than maximum price"));
    }
}
