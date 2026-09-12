package com.assessment.product.service.command;

import com.assessment.product.dto.product.ProductRequest;
import com.assessment.product.dto.product.ProductResponse;

public interface ProductCommandService {

    ProductResponse createProduct(ProductRequest request, String initiatedBy);

    ProductResponse updateProduct(int id, ProductRequest request, String initiatedBy);

    void deleteProduct(int id, String initiatedBy);
}
