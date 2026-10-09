package com.hean.consigueventas.oonabe.profileProfesional.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
@ConditionalOnProperty(name = "app.storage.backend", havingValue = "supabase")
public class SupabaseImageStorageService implements ImageStorageService {

    private static final Set<String> ALLOWED_EXTENSIONS =
            Set.of("jpg", "jpeg", "png");

    private final RestTemplate restTemplate = new RestTemplate();

    private final String supabaseUrl;
    private final String bucket;
    private final String serviceRoleKey;
    private final String publicUrlPrefix;

    public SupabaseImageStorageService(
            @Value("${app.storage.supabase.url}") String supabaseUrl,
            @Value("${app.storage.supabase.bucket:oona-images}") String bucket,
            @Value("${app.storage.supabase.service-role-key}") String serviceRoleKey
    ) {
        if (supabaseUrl == null || supabaseUrl.isBlank()) {
            throw new IllegalStateException(
                    "Falta configurar SUPABASE_URL en el .env (ej. https://abcd1234.supabase.co)."
            );
        }

        if (serviceRoleKey == null || serviceRoleKey.isBlank()) {
            throw new IllegalStateException(
                    "Falta configurar SUPABASE_SERVICE_ROLE_KEY en el .env "
                            + "(Project Settings -> API Keys -> service_role)."
            );
        }

        this.supabaseUrl = supabaseUrl.trim().replaceAll("/$", "");
        this.bucket = bucket.trim();
        this.serviceRoleKey = serviceRoleKey.trim();
        this.publicUrlPrefix = this.supabaseUrl
                + "/storage/v1/object/public/"
                + this.bucket
                + "/";
    }

    @Override
    public String saveProfilePhoto(
            MultipartFile file,
            Long specialistProfileId
    ) {
        validateImage(file, PROFILE_PHOTO_SIZE, PROFILE_PHOTO_SIZE);

        return saveImage(
                file,
                specialistProfileId,
                "photo"
        );
    }

    @Override
    public String saveBanner(
            MultipartFile file,
            Long specialistProfileId
    ) {
        validateImage(file, BANNER_WIDTH, BANNER_HEIGHT);

        return saveImage(
                file,
                specialistProfileId,
                "banner"
        );
    }

    @Override
    public String saveGalleryImage(
            MultipartFile file,
            Long specialistProfileId
    ) {
        validateImage(file, null, null);

        return saveImage(
                file,
                specialistProfileId,
                "gallery"
        );
    }

    @Override
    public String saveEventImage(
            MultipartFile file,
            Long eventId
    ) {
        validateImage(file, COVER_WIDTH, COVER_HEIGHT);

        return saveImage(
                file,
                eventId,
                "event-gallery"
        );
    }

    @Override
    public String saveSessionCoverImage(
            MultipartFile file,
            Long sessionId
    ) {
        validateImage(file, COVER_WIDTH, COVER_HEIGHT);

        return saveImage(
                file,
                sessionId,
                "session-cover"
        );
    }

    @Override
    public void deleteImage(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return;
        }

        if (!imageUrl.startsWith(publicUrlPrefix)) {
            return;
        }

        String objectPath = imageUrl.substring(publicUrlPrefix.length());

        try {
            restTemplate.exchange(
                    supabaseUrl + "/storage/v1/object/" + bucket + "/" + objectPath,
                    HttpMethod.DELETE,
                    new HttpEntity<>(authHeaders()),
                    String.class
            );
        } catch (RestClientException exception) {
            throw new IllegalStateException(
                    "No se pudo eliminar la imagen en Supabase Storage.",
                    exception
            );
        }
    }

    private String saveImage(
            MultipartFile file,
            Long ownerId,
            String imageType
    ) {
        String extension = getExtension(file.getOriginalFilename());

        String fileName = imageType + "-"
                + UUID.randomUUID()
                + "."
                + extension;

        String objectPath = ownerId
                + "/"
                + imageType
                + "/"
                + fileName;

        HttpHeaders headers = authHeaders();

        if (file.getContentType() != null) {
            headers.setContentType(
                    MediaType.parseMediaType(file.getContentType())
            );
        } else {
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        }

        headers.set("x-upsert", "true");

        byte[] bytes;

        try {
            bytes = file.getBytes();
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "No se pudo leer la imagen.",
                    exception
            );
        }

        try {
            restTemplate.exchange(
                    supabaseUrl + "/storage/v1/object/" + bucket + "/" + objectPath,
                    HttpMethod.POST,
                    new HttpEntity<>(bytes, headers),
                    String.class
            );
        } catch (RestClientException exception) {
            throw new IllegalStateException(
                    "No se pudo subir la imagen a Supabase Storage. "
                            + "Verifica SUPABASE_URL, SUPABASE_BUCKET y SUPABASE_SERVICE_ROLE_KEY, "
                            + "y que el bucket exista y sea publico.",
                    exception
            );
        }

        return publicUrlPrefix + objectPath;
    }

    private HttpHeaders authHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(serviceRoleKey);
        return headers;
    }

    private void validateImage(
            MultipartFile file,
            Integer expectedWidth,
            Integer expectedHeight
    ) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(
                    "Debe seleccionar una imagen."
            );
        }

        String contentType = file.getContentType();

        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException(
                    "El archivo debe ser una imagen."
            );
        }

        String extension = getExtension(file.getOriginalFilename());

        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException(
                    "Solo se permiten imágenes JPG, JPEG o PNG."
            );
        }

        if (expectedWidth != null && expectedHeight != null) {
            try {
                BufferedImage image = ImageIO.read(file.getInputStream());

                if (image == null) {
                    throw new IllegalArgumentException(
                            "No se pudo leer la imagen."
                    );
                }

                if (image.getWidth() != expectedWidth
                        || image.getHeight() != expectedHeight) {
                    throw new IllegalArgumentException(
                            "La imagen debe tener medidas exactas de "
                                    + expectedWidth
                                    + "x"
                                    + expectedHeight
                                    + " píxeles."
                    );
                }

            } catch (IOException exception) {
                throw new IllegalArgumentException(
                        "No se pudo validar la imagen.",
                        exception
                );
            }
        }
    }

    private String getExtension(String originalFileName) {
        if (
                originalFileName == null
                        || !originalFileName.contains(".")
        ) {
            throw new IllegalArgumentException(
                    "La imagen debe tener una extensión válida."
            );
        }

        return originalFileName
                .substring(originalFileName.lastIndexOf(".") + 1)
                .toLowerCase(Locale.ROOT);
    }
}