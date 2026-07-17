package com.hean.consigueventas.oonabe.interaction.service;

import com.hean.consigueventas.oonabe.common.enums.ApprovalStatus;
import com.hean.consigueventas.oonabe.common.enums.PublicationStatus;
import com.hean.consigueventas.oonabe.common.exception.BusinessLogicException;
import com.hean.consigueventas.oonabe.common.exception.ResourceNotFoundException;
import com.hean.consigueventas.oonabe.interaction.dto.FollowedProfessionalResponse;
import com.hean.consigueventas.oonabe.interaction.dto.ProfessionalFollowStatusResponse;
import com.hean.consigueventas.oonabe.interaction.entity.ProfessionalFollow;
import com.hean.consigueventas.oonabe.interaction.mapper.ProfessionalFollowMapper;
import com.hean.consigueventas.oonabe.interaction.repository.ProfessionalFollowRepository;
import com.hean.consigueventas.oonabe.profileProfesional.entity.SpecialistProfile;
import com.hean.consigueventas.oonabe.profileProfesional.repository.SpecialistProfileRepository;
import com.hean.consigueventas.oonabe.user.entity.User;
import com.hean.consigueventas.oonabe.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ProfessionalFollowService {

    private final ProfessionalFollowRepository followRepository;
    private final SpecialistProfileRepository specialistProfileRepository;
    private final UserRepository userRepository;
    private final ProfessionalFollowMapper followMapper;

    public ProfessionalFollowService(
            ProfessionalFollowRepository followRepository,
            SpecialistProfileRepository specialistProfileRepository,
            UserRepository userRepository,
            ProfessionalFollowMapper followMapper
    ) {
        this.followRepository = followRepository;
        this.specialistProfileRepository = specialistProfileRepository;
        this.userRepository = userRepository;
        this.followMapper = followMapper;
    }

    @Transactional
    public ProfessionalFollowStatusResponse follow(String username, Long professionalId) {
        User user = getAuthenticatedUser(username);
        SpecialistProfile specialist = getPublicProfessional(professionalId);

        if (specialist.getUser().getId().equals(user.getId())) {
            throw new BusinessLogicException("No puedes seguir tu propio perfil profesional.");
        }

        if (!followRepository.existsByUserIdAndSpecialistProfileId(user.getId(), specialist.getId())) {
            ProfessionalFollow follow = new ProfessionalFollow();
            follow.setUser(user);
            follow.setSpecialistProfile(specialist);
            followRepository.save(follow);
        }

        return new ProfessionalFollowStatusResponse(professionalId, true);
    }

    @Transactional
    public ProfessionalFollowStatusResponse unfollow(String username, Long professionalId) {
        User user = getAuthenticatedUser(username);
        followRepository.findByUserIdAndSpecialistProfileId(user.getId(), professionalId)
                .ifPresent(followRepository::delete);
        return new ProfessionalFollowStatusResponse(professionalId, false);
    }

    public ProfessionalFollowStatusResponse getStatus(String username, Long professionalId) {
        User user = getAuthenticatedUser(username);
        SpecialistProfile specialist = getPublicProfessional(professionalId);
        boolean following = followRepository.existsByUserIdAndSpecialistProfileId(user.getId(), specialist.getId());
        return new ProfessionalFollowStatusResponse(professionalId, following);
    }

    public Page<FollowedProfessionalResponse> getFollowedProfessionals(String username, Pageable pageable) {
        User user = getAuthenticatedUser(username);
        return followRepository.findVisibleByUserId(
                        user.getId(),
                        ApprovalStatus.APROBADO,
                        PublicationStatus.PUBLICADO,
                        pageable
                )
                .map(followMapper::toResponse);
    }

    private User getAuthenticatedUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario autenticado no encontrado."));
    }

    private SpecialistProfile getPublicProfessional(Long professionalId) {
        SpecialistProfile specialist = specialistProfileRepository.findById(professionalId)
                .orElseThrow(() -> new ResourceNotFoundException("Perfil profesional no encontrado."));

        if (specialist.getApprovalStatus() != ApprovalStatus.APROBADO
                || specialist.getPublicationStatus() != PublicationStatus.PUBLICADO) {
            throw new ResourceNotFoundException("Perfil profesional no encontrado o no disponible.");
        }
        return specialist;
    }
}
