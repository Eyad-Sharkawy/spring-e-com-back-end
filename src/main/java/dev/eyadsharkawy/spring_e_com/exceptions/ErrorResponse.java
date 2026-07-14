package dev.eyadsharkawy.spring_e_com.exceptions;

public record ErrorResponse(
        int status,
        String message,
        long timestamp
) {
}
