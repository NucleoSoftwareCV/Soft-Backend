package com.hean.consigueventas.oonabe.managementAdmin.service;

import com.hean.consigueventas.oonabe.common.exception.BusinessLogicException;
import com.hean.consigueventas.oonabe.common.exception.ResourceNotFoundException;
import com.hean.consigueventas.oonabe.managementAdmin.dto.PromotionResponseDto;
import com.hean.consigueventas.oonabe.managementAdmin.dto.RolePromotionRequestDto;
import com.hean.consigueventas.oonabe.managementAdmin.entity.RolePromotionRequest;
import com.hean.consigueventas.oonabe.managementAdmin.enums.PromotionStatus;
import com.hean.consigueventas.oonabe.managementAdmin.mapper.RolePromotionMapper;
import com.hean.consigueventas.oonabe.managementAdmin.repository.RolePromotionRequestRepository;
import com.hean.consigueventas.oonabe.user.entity.Role;
import com.hean.consigueventas.oonabe.user.entity.User;
import com.hean.consigueventas.oonabe.user.repository.RoleRepository;
import com.hean.consigueventas.oonabe.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RolePromotionService {

    private final RolePromotionRequestRepository requestRepository;
    private final RolePromotionMapper mapper;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public RolePromotionService(RolePromotionRequestRepository requestRepository, RolePromotionMapper mapper, UserRepository userRepository, RoleRepository roleRepository) {
        this.requestRepository = requestRepository;
        this.mapper = mapper;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    @Transactional
    public PromotionResponseDto createPromotionRequest(Long userId, RolePromotionRequestDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (requestRepository.findByUserIdAndStatus(userId, PromotionStatus.PENDIENTE).isPresent()) {
            throw new BusinessLogicException("Ya existe una solicitud pendiente.");
        }

        RolePromotionRequest request = new RolePromotionRequest();
        request.setUser(user);
        request.setReason(dto.getReason());
        request.setStatus(PromotionStatus.PENDIENTE);
        return mapper.toDto(requestRepository.save(request));
    }

    @Transactional
    public PromotionResponseDto evaluatePromotionRequest(Long requestId, String statusStr) {
        RolePromotionRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found"));
        
        PromotionStatus newStatus = PromotionStatus.valueOf(statusStr.toUpperCase());
        request.setStatus(newStatus);
        
        if (newStatus == PromotionStatus.APROBADO) {
            Role roleProf = roleRepository.findByName(Role.ROLE_PROFESSIONAL)
                    .orElseThrow(() -> new ResourceNotFoundException("Role not found"));
            request.getUser().getRoles().add(roleProf);
            userRepository.save(request.getUser());
        }

        return mapper.toDto(requestRepository.save(request));
    }
}
