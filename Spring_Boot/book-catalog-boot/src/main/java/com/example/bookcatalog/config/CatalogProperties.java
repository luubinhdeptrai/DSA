package com.example.bookcatalog.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties("catalog")
@Validated
public record CatalogProperties (@NotBlank String reportTitle, @Min(0) int lowStockThreshold, boolean importEnabled, @NotBlank String importLocation ) {
    
}
