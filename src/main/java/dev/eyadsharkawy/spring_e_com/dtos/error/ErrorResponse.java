package dev.eyadsharkawy.spring_e_com.dtos.error;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Standard structure for all error responses returned by the API")
public record ErrorResponse(
        @Schema(description = "HTTP status code of the error", example = "404")
        int status,

        @Schema(description = "Detailed description of what went wrong", example = "Product not found with identifier: xyz")
        String message,

        @Schema(description = "Epoch timestamp of when the error occurred", example = "1785146027823")
        long timestamp
) {
}
