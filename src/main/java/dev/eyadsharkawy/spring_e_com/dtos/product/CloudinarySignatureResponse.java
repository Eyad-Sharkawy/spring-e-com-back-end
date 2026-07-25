package dev.eyadsharkawy.spring_e_com.dtos.product;

public record CloudinarySignatureResponse(
        String signature,
        long timestamp,
        String apiKey,
        String cloudName,
        String publicId,
        String folder,
        String eager
) {
}
