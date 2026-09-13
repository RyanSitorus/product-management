package com.assessment.product.controller.ui;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ProductViewController {

    @GetMapping({"/", "/products"})
    public String productsPage() {
        return "products/index";
    }
}
