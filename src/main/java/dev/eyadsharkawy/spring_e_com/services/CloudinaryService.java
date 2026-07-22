package dev.eyadsharkawy.spring_e_com.services;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.utils.ObjectUtils;
import dev.eyadsharkawy.spring_e_com.exceptions.ImageUploadException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CloudinaryService {
    private final Cloudinary cloudinary;

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
                            "folder", "spring-e-com/products",
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

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> eagerResults = (List<Map<String, Object>>) result.get("eager");
            String croppedUrl = (String) eagerResults.get(0).get("secure_url");

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
