package com.assessment.product.service.command;

import com.assessment.product.dto.product.ProductRequest;
import com.assessment.product.dto.product.ProductResponse;
import com.assessment.product.entity.Product;
import com.assessment.product.exception.ResourceNotFoundException;
import com.assessment.product.repository.ProductRepository;
import com.assessment.product.service.async.AsyncAuditLogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductCommandServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private AsyncAuditLogService auditLogService;

    @InjectMocks
    private ProductCommandServiceImpl commandService;

    private Product product;
    private ProductRequest request;

    @BeforeEach
    void setUp() {
        product = Product.builder()
                .id(1)
                .name("Keyboard")
                .description("Mechanical keyboard")
                .price(new BigDecimal("79.99"))
                .createdAt(new Date())
                .build();

        request = ProductRequest.builder()
                .name("Keyboard")
                .description("Mechanical keyboard")
                .price(new BigDecimal("79.99"))
                .build();
    }

    @Test
    @DisplayName("createProduct() - Success")
    void createProduct_Success() {
        when(productRepository.save(any(Product.class))).thenReturn(product);

        ProductResponse response = commandService.createProduct(request, "admin");

        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo("Keyboard");
        verify(productRepository).save(any(Product.class));
        verify(auditLogService).logAction("CREATE", "Product", "1", "admin");
    }

    @Test
    @DisplayName("updateProduct() - Success")
    void updateProduct_Success() {
        when(productRepository.findById(1)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        ProductRequest updateReq = ProductRequest.builder()
                .name("Wireless Keyboard")
                .description("Updated specs")
                .price(new BigDecimal("89.99"))
                .build();

        ProductResponse response = commandService.updateProduct(1, updateReq, "admin");

        assertThat(response).isNotNull();
        verify(productRepository).save(any(Product.class));
        verify(auditLogService).logAction("UPDATE", "Product", "1", "admin");
    }

    @Test
    @DisplayName("updateProduct() - Throws ResourceNotFoundException")
    void updateProduct_NotFound() {
        when(productRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                commandService.updateProduct(99, request, "admin"));
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("deleteProduct() - Success")
    void deleteProduct_Success() {
        when(productRepository.findById(1)).thenReturn(Optional.of(product));
        doNothing().when(productRepository).delete(product);

        commandService.deleteProduct(1, "admin");

        verify(productRepository).delete(product);
        verify(auditLogService).logAction("DELETE", "Product", "1", "admin");
    }

    @Test
    @DisplayName("deleteProduct() - Throws ResourceNotFoundException")
    void deleteProduct_NotFound() {
        when(productRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                commandService.deleteProduct(99, "admin"));
        verify(productRepository, never()).delete(any(Product.class));
    }
}
