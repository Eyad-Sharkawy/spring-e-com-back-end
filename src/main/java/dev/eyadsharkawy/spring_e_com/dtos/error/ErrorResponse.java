package dev.eyadsharkawy.spring_e_com.dtos.error;

public record ErrorResponse(
        int status,
        String message,
        long timestamp
) {
}
