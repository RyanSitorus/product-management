package com.assessment.product.dto.common;

import lombok.*;

import java.util.Date;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorResponse {

    private boolean success;
    private int status;
    private String error;
    private String message;
    private String path;
    @Builder.Default
    private Date timestamp = new Date();
    private Map<String, String> validationErrors;
}
