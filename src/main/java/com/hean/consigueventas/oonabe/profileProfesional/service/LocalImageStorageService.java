package com.hean.consigueventas.oonabe.profileProfesional.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class LocalImageStorageService {

    public static final int PROFILE_PHOTO_SIZE = 512;
    public static final int BANNER_WIDTH = 1248;
    public static final int BANNER_HEIGHT = 256;
    public static final int COVER_WIDTH = 1200;
    public static final int COVER_HEIGHT = 900;

    private static final Set<String> ALLOWED_EXTENSIONS =
            Set.of("jpg", "jpeg", "png");

    private final Path storageDirectory;
    private final String urlPrefix;

    public LocalImageStorageService(
            @Value("${app.storage.profile-images-directory:images/profile-images}")
            String storageDirectory,
            @Value("${app.storage.profile-images-url-prefix:/images/profile-images}")
            String urlPrefix
    ) {
        this.storageDirectory = Paths.get(storageDirectory)
                .toAbsolutePath()
                .normalize();

        this.urlPrefix = urlPrefix;

        try {
            Files.createDirectories(this.storageDirectory);
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "No se pudo crear la carpeta de imágenes.",
                    exception
            );
        }
    }

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

    public void deleteImage(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return;
        }

        String prefix = urlPrefix + "/";

        if (!imageUrl.startsWith(prefix)) {
            return;
        }

        String relativePath = imageUrl.substring(prefix.length());

        Path imagePath = storageDirectory
                .resolve(relativePath)
                .normalize();

        if (!imagePath.startsWith(storageDirectory)) {
            throw new IllegalArgumentException(
                    "La ruta de la imagen no es válida."
            );
        }

        try {
            Files.deleteIfExists(imagePath);
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "No se pudo eliminar la imagen.",
                    exception
            );
        }
    }

    private String saveImage(
            MultipartFile file,
            Long specialistProfileId,
            String imageType
    ) {
        String extension = getExtension(file.getOriginalFilename());

        String fileName = imageType + "-"
                + UUID.randomUUID()
                + "."
                + extension;

        Path profileDirectory = storageDirectory
                .resolve(String.valueOf(specialistProfileId))
                .resolve(imageType)
                .normalize();

        try {
            Files.createDirectories(profileDirectory);

            Path destination = profileDirectory
                    .resolve(fileName)
                    .normalize();

            if (!destination.startsWith(profileDirectory)) {
                throw new IllegalArgumentException(
                        "La ruta de la imagen no es válida."
                );
            }

            Files.copy(
                    file.getInputStream(),
                    destination,
                    StandardCopyOption.REPLACE_EXISTING
            );

            return urlPrefix + "/"
                    + specialistProfileId
                    + "/"
                    + imageType
                    + "/"
                    + fileName;

        } catch (IOException exception) {
            throw new IllegalStateException(
                    "No se pudo guardar la imagen.",
                    exception
            );
        }
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

        try {
            BufferedImage image = ImageIO.read(file.getInputStream());

            if (image == null) {
                throw new IllegalArgumentException(
                        "No se pudo leer la imagen."
                );
            }

            if (
                    expectedWidth != null
                            && expectedHeight != null
                            && (image.getWidth() != expectedWidth
                            || image.getHeight() != expectedHeight)
            ) {
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
