package com.hean.consigueventas.oonabe.profileProfesional.service;

import org.springframework.web.multipart.MultipartFile;

public interface ImageStorageService {

    int PROFILE_PHOTO_SIZE = 512;
    int BANNER_WIDTH = 1248;
    int BANNER_HEIGHT = 256;
    int COVER_WIDTH = 1200;
    int COVER_HEIGHT = 900;

    String saveProfilePhoto(MultipartFile file, Long specialistProfileId);

    String saveBanner(MultipartFile file, Long specialistProfileId);

    String saveGalleryImage(MultipartFile file, Long specialistProfileId);

    String saveEventImage(MultipartFile file, Long eventId);

    String saveSessionCoverImage(MultipartFile file, Long sessionId);

    void deleteImage(String imageUrl);
}