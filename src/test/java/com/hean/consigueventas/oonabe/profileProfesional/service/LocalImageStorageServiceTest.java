package com.hean.consigueventas.oonabe.profileProfesional.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LocalImageStorageServiceTest {

    @TempDir
    Path storageDirectory;

    @Test
    void savesProfilePhotoProducedByFrontendCropper() throws Exception {
        LocalImageStorageService service = new LocalImageStorageService(
                storageDirectory.toString(),
                "/images/profile-images"
        );

        String imageUrl = service.saveProfilePhoto(imageFile(512, 512), 9L);

        assertThat(imageUrl).startsWith("/images/profile-images/9/photo/");
        assertThat(Files.exists(resolveStoredFile(imageUrl))).isTrue();
    }

    @Test
    void rejectsLegacyProfilePhotoDimensions() throws Exception {
        LocalImageStorageService service = new LocalImageStorageService(
                storageDirectory.toString(),
                "/images/profile-images"
        );

        assertThatThrownBy(() -> service.saveProfilePhoto(imageFile(126, 126), 9L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("512x512");
    }

    @Test
    void savesCampaignImageWithoutDimensionRestriction() throws Exception {
        LocalImageStorageService service = new LocalImageStorageService(
                storageDirectory.toString(),
                "/images/profile-images"
        );

        String imageUrl = service.saveCampaignImage(imageFile(800, 600), 9L);

        assertThat(imageUrl).startsWith("/images/profile-images/9/campaign-hero/");
        assertThat(Files.exists(resolveStoredFile(imageUrl))).isTrue();
    }

    @Test
    void acceptsWebpOnlyForCampaignImagesAndChecksItsHeader() {
        LocalImageStorageService service = new LocalImageStorageService(
                storageDirectory.toString(),
                "/images/profile-images"
        );
        MockMultipartFile webp = new MockMultipartFile(
                "file", "hero.webp", "image/webp", "RIFF\0\0\0\0WEBPVP8 ".getBytes(StandardCharsets.US_ASCII));
        MockMultipartFile fake = new MockMultipartFile(
                "file", "hero.webp", "image/webp", "<html>no soy imagen</html>".getBytes(StandardCharsets.US_ASCII));

        String imageUrl = service.saveCampaignImage(webp, 9L);

        assertThat(imageUrl).startsWith("/images/profile-images/9/campaign-hero/").endsWith(".webp");
        assertThat(Files.exists(resolveStoredFile(imageUrl))).isTrue();
        assertThatThrownBy(() -> service.saveCampaignImage(fake, 9L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("No se pudo leer");
        assertThatThrownBy(() -> service.saveGalleryImage(webp, 9L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("JPG, JPEG o PNG");
    }

    @Test
    void savesGalleryImageWithoutDimensionRestriction() throws Exception {
        LocalImageStorageService service = new LocalImageStorageService(
                storageDirectory.toString(),
                "/images/profile-images"
        );

        String imageUrl = service.saveGalleryImage(imageFile(800, 600), 9L);

        assertThat(imageUrl).startsWith("/images/profile-images/9/gallery/");
        assertThat(Files.exists(resolveStoredFile(imageUrl))).isTrue();
    }

    private MockMultipartFile imageFile(int width, int height) throws Exception {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", output);
        return new MockMultipartFile("file", "profile-photo.jpg", "image/jpeg", output.toByteArray());
    }

    private Path resolveStoredFile(String imageUrl) {
        String relativePath = imageUrl.substring("/images/profile-images/".length());
        return storageDirectory.resolve(relativePath);
    }
}
