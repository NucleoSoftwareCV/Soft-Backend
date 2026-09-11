package com.hean.consigueventas.oonabe.professionalApplication.service;

import com.hean.consigueventas.oonabe.common.exception.BusinessLogicException;
import com.hean.consigueventas.oonabe.common.exception.ResourceNotFoundException;
import com.hean.consigueventas.oonabe.professionalApplication.dto.request.ProfessionalApplicationDecisionRequest;
import com.hean.consigueventas.oonabe.professionalApplication.dto.request.ProfessionalApplicationRequest;
import com.hean.consigueventas.oonabe.professionalApplication.dto.response.ProfessionalApplicationResponse;
import com.hean.consigueventas.oonabe.professionalApplication.entity.ProfessionalApplication;
import com.hean.consigueventas.oonabe.professionalApplication.enums.ProfessionalApplicationStatus;
import com.hean.consigueventas.oonabe.professionalApplication.enums.ProfessionalType;
import com.hean.consigueventas.oonabe.professionalApplication.mapper.ProfessionalApplicationMapper;
import com.hean.consigueventas.oonabe.professionalApplication.repository.ProfessionalApplicationRepository;
import com.hean.consigueventas.oonabe.profileProfesional.service.SpecialistProfileService;
import com.hean.consigueventas.oonabe.user.entity.Role;
import com.hean.consigueventas.oonabe.user.entity.User;
import com.hean.consigueventas.oonabe.user.repository.RoleRepository;
import com.hean.consigueventas.oonabe.user.repository.UserRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashSet;

@Service
@Transactional(readOnly = true)
public class ProfessionalApplicationService {

    private final ProfessionalApplicationRepository applicationRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ProfessionalApplicationMapper mapper;
    private final SpecialistProfileService specialistProfileService;

    public ProfessionalApplicationService(
            ProfessionalApplicationRepository applicationRepository,
            UserRepository userRepository,
            RoleRepository roleRepository,
            ProfessionalApplicationMapper mapper,
            SpecialistProfileService specialistProfileService) {

        this.applicationRepository = applicationRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.mapper = mapper;
        this.specialistProfileService = specialistProfileService;
    }

    @Transactional
    public ProfessionalApplicationResponse createApplication(
            ProfessionalApplicationRequest request,
            Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Usuario no encontrado."
                        ));

        boolean isProfessional = user.getRoles() != null
                && user.getRoles().stream()
                .anyMatch(role ->
                        Role.ROLE_PROFESSIONAL.equals(role.getName()));

        if (isProfessional) {
            throw new BusinessLogicException(
                    "Tu cuenta ya tiene el rol de profesional."
            );
        }

        if (applicationRepository.findByUserId(userId).isPresent()) {
            throw new BusinessLogicException(
                    "Ya existe una solicitud profesional asociada a tu cuenta."
            );
        }

        String email = request.email().trim().toLowerCase();
        String fullName = request.fullName().trim();
        String city = request.city().trim();

        ProfessionalApplication application =
                new ProfessionalApplication();

        application.setUser(user);
        application.setFullName(fullName);
        application.setEmail(email);
        application.setCity(city);

        application.setProfessionalType(
                ProfessionalType.valueOf(
                        request.professionalType()
                                .trim()
                                .toUpperCase()
                )
        );

        application.setWhatsappPhone(
                request.whatsappPhone().trim()
        );

        application.setMotivation(
                request.motivation().trim()
        );

        application.setPrivacyAcceptedAt(Instant.now());

        application.setStatus(
                ProfessionalApplicationStatus.PENDIENTE
        );

        application.setEvaluatedBy(null);
        application.setEvaluatedAt(null);
        application.setRejectionReason(null);

        ProfessionalApplication saved =
                applicationRepository.save(application);

        return mapper.toResponse(saved);
    }

    public Page<ProfessionalApplicationResponse> getApplicationsForAdmin(
            ProfessionalApplicationStatus status,
            Pageable pageable) {

        return applicationRepository
                .findForAdmin(status, pageable)
                .map(mapper::toResponse);
    }

    @Transactional
    public ProfessionalApplicationResponse decide(
            Long applicationId,
            Long adminId,
            ProfessionalApplicationDecisionRequest request) {

        ProfessionalApplication application =
                applicationRepository.findById(applicationId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Solicitud profesional no encontrada."
                                ));

        if (application.getStatus() == request.status()) {
            return mapper.toResponse(application);
        }

        if (application.getStatus() !=
                ProfessionalApplicationStatus.PENDIENTE) {

            throw new BusinessLogicException(
                    "Solo se pueden evaluar solicitudes pendientes."
            );
        }

        User admin = userRepository.findById(adminId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Administrador no encontrado."
                        ));

        application.setEvaluatedBy(admin);
        application.setEvaluatedAt(Instant.now());
        application.setStatus(request.status());

        if (request.status() ==
                ProfessionalApplicationStatus.APROBADO) {

            application.setRejectionReason(null);

            User professional =
                    createOrPromoteProfessional(application);

            application.setUser(professional);

        } else if (request.status() ==
                ProfessionalApplicationStatus.RECHAZADO) {

            String reason = request.rejectionReason();

            if (reason == null || reason.trim().isEmpty()) {
                throw new BusinessLogicException(
                        "El motivo del rechazo es obligatorio."
                );
            }

            application.setRejectionReason(
                    reason.trim()
            );
        }

        ProfessionalApplication saved =
                applicationRepository.save(application);

        return mapper.toResponse(saved);
    }

    private User createOrPromoteProfessional(
            ProfessionalApplication application) {

        User user = application.getUser();

        if (user == null) {
            throw new BusinessLogicException(
                    "La solicitud no tiene un usuario asociado."
            );
        }

        Role professionalRole =
                roleRepository.findByName(
                        Role.ROLE_PROFESSIONAL
                ).orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Rol profesional no encontrado."
                        ));

        if (user.getRoles() == null) {
            user.setRoles(new HashSet<>());
        }

        user.getRoles().add(professionalRole);

        User savedUser = userRepository.save(user);
        specialistProfileService.createMinimalProfileIfMissing(
                savedUser,
                application.getFullName(),
                application.getWhatsappPhone()
        );

        return savedUser;
    }
}