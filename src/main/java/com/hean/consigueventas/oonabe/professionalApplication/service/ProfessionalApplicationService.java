package com.hean.consigueventas.oonabe.professionalApplication.service;

import com.hean.consigueventas.oonabe.common.exception.BusinessLogicException;
import com.hean.consigueventas.oonabe.common.exception.ResourceNotFoundException;
import com.hean.consigueventas.oonabe.masterdata.entity.City;
import com.hean.consigueventas.oonabe.masterdata.repository.CityRepository;
import com.hean.consigueventas.oonabe.professionalApplication.dto.request.ProfessionalApplicationDecisionRequest;
import com.hean.consigueventas.oonabe.professionalApplication.dto.request.ProfessionalApplicationRequest;
import com.hean.consigueventas.oonabe.professionalApplication.dto.response.ProfessionalApplicationResponse;
import com.hean.consigueventas.oonabe.professionalApplication.entity.ProfessionalApplication;
import com.hean.consigueventas.oonabe.professionalApplication.enums.ProfessionalApplicationStatus;
import com.hean.consigueventas.oonabe.professionalApplication.mapper.ProfessionalApplicationMapper;
import com.hean.consigueventas.oonabe.professionalApplication.repository.ProfessionalApplicationRepository;
import com.hean.consigueventas.oonabe.user.entity.Role;
import com.hean.consigueventas.oonabe.user.entity.User;
import com.hean.consigueventas.oonabe.user.repository.RoleRepository;
import com.hean.consigueventas.oonabe.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@Transactional(readOnly = true)
public class ProfessionalApplicationService {

    private final ProfessionalApplicationRepository applicationRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final CityRepository cityRepository;
    private final ProfessionalApplicationMapper mapper;

    public ProfessionalApplicationService(
            ProfessionalApplicationRepository applicationRepository,
            UserRepository userRepository,
            RoleRepository roleRepository,
            CityRepository cityRepository,
            ProfessionalApplicationMapper mapper) {
        this.applicationRepository = applicationRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.cityRepository = cityRepository;
        this.mapper = mapper;
    }

    @Transactional
    public ProfessionalApplicationResponse saveMyApplication(
            Long userId,
            ProfessionalApplicationRequest request) {
        User user = getUser(userId);
        if (hasRole(user, Role.ROLE_PROFESSIONAL)) {
            throw new BusinessLogicException("El usuario ya tiene acceso profesional.");
        }

        City city = cityRepository.findById(request.cityId())
                .filter(candidate -> Boolean.TRUE.equals(candidate.getIsActive()))
                .orElseThrow(() -> new ResourceNotFoundException("Ciudad activa no encontrada."));

        ProfessionalApplication application = applicationRepository.findByUserId(userId)
                .orElseGet(() -> {
                    ProfessionalApplication created = new ProfessionalApplication();
                    created.setUser(user);
                    return created;
                });

        if (application.getStatus() == ProfessionalApplicationStatus.APROBADO) {
            throw new BusinessLogicException("La solicitud ya fue aprobada y no puede modificarse.");
        }

        application.setFullName(request.fullName().trim());
        application.setCity(city);
        application.setProfessionalType(request.professionalType());
        application.setWhatsappPhone(request.whatsappPhone().trim());
        application.setMotivation(request.motivation().trim());
        application.setPrivacyAcceptedAt(Instant.now());
        application.setStatus(ProfessionalApplicationStatus.PENDIENTE);
        application.setEvaluatedBy(null);
        application.setEvaluatedAt(null);
        application.setRejectionReason(null);

        return mapper.toResponse(applicationRepository.save(application));
    }

    public ProfessionalApplicationResponse getMyApplication(Long userId) {
        return applicationRepository.findByUserId(userId)
                .map(mapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitud profesional no encontrada."));
    }

    public Page<ProfessionalApplicationResponse> getApplicationsForAdmin(
            ProfessionalApplicationStatus status,
            Pageable pageable) {
        return applicationRepository.findForAdmin(status, pageable).map(mapper::toResponse);
    }

    @Transactional
    public ProfessionalApplicationResponse decide(
            Long applicationId,
            Long adminId,
            ProfessionalApplicationDecisionRequest request) {
        ProfessionalApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitud profesional no encontrada."));

        if (application.getStatus() == request.status()) {
            return mapper.toResponse(application);
        }
        if (application.getStatus() != ProfessionalApplicationStatus.PENDIENTE) {
            throw new BusinessLogicException("Solo se pueden evaluar solicitudes pendientes.");
        }

        User admin = getUser(adminId);
        application.setStatus(request.status());
        application.setEvaluatedBy(admin);
        application.setEvaluatedAt(Instant.now());

        if (request.status() == ProfessionalApplicationStatus.APROBADO) {
            application.setRejectionReason(null);
            grantProfessionalRole(application.getUser());
        } else {
            application.setRejectionReason(request.rejectionReason().trim());
        }

        return mapper.toResponse(applicationRepository.save(application));
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado."));
    }

    private void grantProfessionalRole(User user) {
        if (hasRole(user, Role.ROLE_PROFESSIONAL)) {
            return;
        }
        Role professionalRole = roleRepository.findByName(Role.ROLE_PROFESSIONAL)
                .orElseThrow(() -> new ResourceNotFoundException("Rol profesional no encontrado."));
        user.getRoles().add(professionalRole);
        userRepository.save(user);
    }

    private boolean hasRole(User user, String roleName) {
        return user.getRoles().stream().anyMatch(role -> roleName.equals(role.getName()));
    }
}
