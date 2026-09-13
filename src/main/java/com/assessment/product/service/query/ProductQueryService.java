package com.assessment.product.service.query;

import com.assessment.product.dto.common.PagedResponse;
import com.assessment.product.dto.product.ProductResponse;

import java.math.BigDecimal;
import java.util.List;

public interface ProductQueryService {

    ProductResponse getProductById(int id);

    List<ProductResponse> getAllProducts();

    PagedResponse<ProductResponse> searchProducts(
            String name,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            int page,
            int size,
            String sortBy,
            String sortDir
    );
}
