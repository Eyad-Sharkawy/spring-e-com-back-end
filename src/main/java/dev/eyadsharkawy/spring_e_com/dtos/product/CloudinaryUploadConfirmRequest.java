package dev.eyadsharkawy.spring_e_com.dtos.product;

import jakarta.validation.constraints.NotBlank;

public record CloudinaryUploadConfirmRequest(
        @NotBlank String url,
        @NotBlank String publicId
) {
}
