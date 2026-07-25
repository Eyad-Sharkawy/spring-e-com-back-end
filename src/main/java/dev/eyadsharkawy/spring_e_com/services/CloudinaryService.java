package dev.eyadsharkawy.spring_e_com.services;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.utils.ObjectUtils;
import dev.eyadsharkawy.spring_e_com.dtos.product.CloudinarySignatureResponse;
import dev.eyadsharkawy.spring_e_com.exceptions.ImageUploadException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CloudinaryService {
    private static final String PRODUCT_FOLDER = "spring-e-com/products";
    private static final String EAGER_TRANSFORMATION_STRING = "c_fill,g_auto,w_800,h_800,q_auto,f_auto";

    private final Cloudinary cloudinary;

    public CloudinarySignatureResponse generateSignature(String productId) {
        long timestamp = System.currentTimeMillis() / 1000L;
        String publicId = "products_" + productId + "_" + UUID.randomUUID();

        Map<String, Object> params = new HashMap<>(Map.of(
                "timestamp", timestamp,
                "folder", PRODUCT_FOLDER,
                "public_id", publicId,
                "eager", EAGER_TRANSFORMATION_STRING
        ));


        String signature = cloudinary.apiSignRequest(params, cloudinary.config.apiSecret);

        return new CloudinarySignatureResponse(
                signature,
                timestamp,
                cloudinary.config.apiKey,
                cloudinary.config.cloudName,
                publicId,
                PRODUCT_FOLDER,
                EAGER_TRANSFORMATION_STRING
        );
    }

    public UploadResult uploadImage(MultipartFile file) {
        String contentType = file.getContentType();

        if (contentType == null || !contentType.startsWith("image/")) {
            throw new ImageUploadException("Only image files are allowed", null);
        }

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> result = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", PRODUCT_FOLDER,
                            "eager", List.of(
                                    new Transformation<>()
                                            .crop("fill")
                                            .gravity("auto")
                                            .width(800)
                                            .height(800)
                                            .quality("auto")
                                            .fetchFormat("auto")
                            )
                    )
            );

            String secureUrl = (String) result.get("secure_url");
            if (secureUrl == null) {
                secureUrl = (String) result.get("url");
            }

            String croppedUrl = secureUrl;
            if (result.containsKey("eager")) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> eagerResults = (List<Map<String, Object>>) result.get("eager");
                if (eagerResults != null && !eagerResults.isEmpty()) {
                    croppedUrl = (String) eagerResults.getFirst().get("secure_url");
                }
            }

            return new UploadResult(croppedUrl, (String) result.get("public_id"));
        } catch (IOException e) {
            throw new ImageUploadException("Failed to upload image to Cloudinary", e);
        }
    }

    public void deleteImage(String publicId) {
        if (publicId == null) return;

        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        } catch (IOException e) {
            throw new ImageUploadException("Failed to delete image from Cloudinary", e);
        }
    }

    public record UploadResult(String url, String publicId) {
    }
}