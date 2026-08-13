package com.hean.consigueventas.oonabe.profileProfesional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hean.consigueventas.oonabe.common.enums.PublicationStatus;
import com.hean.consigueventas.oonabe.profileProfesional.entity.SpecialistProfile;
import com.hean.consigueventas.oonabe.profileProfesional.repository.ProfessionalGalleryImageRepository;
import com.hean.consigueventas.oonabe.profileProfesional.repository.SpecialistProfileRepository;
import com.hean.consigueventas.oonabe.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class SpecialistProfileGalleryIntegrationTest {

    private static final String GALLERY_URL = "/api/v1/specialist-profiles/me/gallery-images";
    private static final String PATCH_ME_URL = "/api/v1/specialist-profiles/me";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SpecialistProfileRepository specialistProfileRepository;

    @Autowired
    private ProfessionalGalleryImageRepository galleryImageRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void uploadingGalleryImageAddsItAndResetsPublicationToDraft() throws Exception {
        assertPublicationStatus("specialist_ana", PublicationStatus.PUBLICADO);

        mockMvc.perform(multipart(GALLERY_URL)
                        .file(imageFile())
                        .with(user("specialist_ana").roles("PROFESSIONAL")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.galleryImages.length()").value(1))
                .andExpect(jsonPath("$.galleryImages[0].imageUrl").exists())
                .andExpect(jsonPath("$.publicationStatus").value("BORRADOR"));

        assertPublicationStatus("specialist_ana", PublicationStatus.BORRADOR);
    }

    @Test
    void uploadingMoreThanTwelveImagesIsRejected() throws Exception {
        for (int i = 0; i < 12; i++) {
            mockMvc.perform(multipart(GALLERY_URL)
                            .file(imageFile())
                            .with(user("specialist_carlos").roles("PROFESSIONAL")))
                    .andExpect(status().isOk());
        }

        mockMvc.perform(multipart(GALLERY_URL)
                        .file(imageFile())
                        .with(user("specialist_carlos").roles("PROFESSIONAL")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(
                        "Solo puedes subir hasta 12 imágenes a tu galería."
                ));
    }

    @Test
    void deletingGalleryImageRemovesItAndResetsPublicationToDraft() throws Exception {
        Long imageId = uploadImageAndGetId("professional_demo");

        mockMvc.perform(delete(GALLERY_URL + "/{imageId}", imageId)
                        .with(user("professional_demo").roles("PROFESSIONAL")))
                .andExpect(status().isOk());

        assertThat(galleryImageRepository.findById(imageId)).isEmpty();
        assertPublicationStatus("professional_demo", PublicationStatus.BORRADOR);
    }

    @Test
    void deletingSomeoneElsesGalleryImageIsNotFound() throws Exception {
        Long imageId = uploadImageAndGetId("specialist_ana");

        mockMvc.perform(delete(GALLERY_URL + "/{imageId}", imageId)
                        .with(user("specialist_carlos").roles("PROFESSIONAL")))
                .andExpect(status().isNotFound());
    }

    @Test
    void uploadRequiresAuthentication() throws Exception {
        mockMvc.perform(multipart(GALLERY_URL).file(imageFile()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void uploadRequiresProfessionalRole() throws Exception {
        mockMvc.perform(multipart(GALLERY_URL)
                        .file(imageFile())
                        .with(user("user2").roles("USER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void togglingSectionVisibilityDoesNotResetPublicationStatus() throws Exception {
        assertPublicationStatus("specialist_ana", PublicationStatus.PUBLICADO);

        mockMvc.perform(patch(PATCH_ME_URL)
                        .with(user("specialist_ana").roles("PROFESSIONAL"))
                        .contentType("application/json")
                        .content("{\"showGallery\": false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.showGallery").value(false))
                .andExpect(jsonPath("$.publicationStatus").value("PUBLICADO"));

        assertPublicationStatus("specialist_ana", PublicationStatus.PUBLICADO);
    }

    private Long uploadImageAndGetId(String username) throws Exception {
        String responseBody = mockMvc.perform(multipart(GALLERY_URL)
                        .file(imageFile())
                        .with(user(username).roles("PROFESSIONAL")))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode root = objectMapper.readTree(responseBody);
        return root.get("galleryImages").get(0).get("id").asLong();
    }

    private void assertPublicationStatus(String username, PublicationStatus expected) {
        SpecialistProfile profile = specialistProfileRepository
                .findByUserId(userRepository.findByUsername(username).orElseThrow().getId())
                .orElseThrow();
        assertThat(profile.getPublicationStatus()).isEqualTo(expected);
    }

    private MockMultipartFile imageFile() throws Exception {
        BufferedImage image = new BufferedImage(200, 150, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", output);
        return new MockMultipartFile("file", "gallery.jpg", "image/jpeg", output.toByteArray());
    }
}
