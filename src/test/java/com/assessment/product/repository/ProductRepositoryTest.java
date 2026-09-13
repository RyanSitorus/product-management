package com.assessment.product.repository;

import com.assessment.product.entity.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();

        productRepository.save(Product.builder()
                .name("Gaming Laptop")
                .description("High end laptop")
                .price(new BigDecimal("1500.00"))
                .build());

        productRepository.save(Product.builder()
                .name("Wireless Mouse")
                .description("Ergonomic mouse")
                .price(new BigDecimal("50.00"))
                .build());

        productRepository.save(Product.builder()
                .name("Mechanical Keyboard")
                .description("RGB keyboard")
                .price(new BigDecimal("120.00"))
                .build());
    }

    @Test
    @DisplayName("Should find products by name containing keyword")
    void testFindByName() {
        Specification<Product> spec = ProductSpecification.hasName("laptop");
        List<Product> results = productRepository.findAll(spec);

        assertEquals(1, results.size());
        assertEquals("Gaming Laptop", results.get(0).getName());
    }

    @Test
    @DisplayName("Should return all when name filter is blank")
    void testFindByNameBlank() {
        Specification<Product> spec = ProductSpecification.hasName("");
        List<Product> results = productRepository.findAll(spec);

        assertEquals(3, results.size());
    }

    @Test
    @DisplayName("Should filter products by minimum price")
    void testFindByMinPrice() {
        Specification<Product> spec = ProductSpecification.hasMinPrice(new BigDecimal("100.00"));
        List<Product> results = productRepository.findAll(spec);

        assertEquals(2, results.size());
    }

    @Test
    @DisplayName("Should filter products by maximum price")
    void testFindByMaxPrice() {
        Specification<Product> spec = ProductSpecification.hasMaxPrice(new BigDecimal("100.00"));
        List<Product> results = productRepository.findAll(spec);

        assertEquals(1, results.size());
        assertEquals("Wireless Mouse", results.get(0).getName());
    }

    @Test
    @DisplayName("Should filter products using combined filter specification with pagination")
    void testCombinedFilter() {
        Specification<Product> spec = ProductSpecification.filter("key", new BigDecimal("50.00"), new BigDecimal("200.00"));
        Page<Product> page = productRepository.findAll(spec, PageRequest.of(0, 10));

        assertEquals(1, page.getTotalElements());
        assertEquals("Mechanical Keyboard", page.getContent().get(0).getName());
    }

    @Test
    @DisplayName("Should instantiate ProductSpecification helper")
    void testProductSpecificationInstance() {
        assertNotNull(new ProductSpecification());
    }
}
