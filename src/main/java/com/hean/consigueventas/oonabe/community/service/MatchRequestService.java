package com.hean.consigueventas.oonabe.community.service;

import com.hean.consigueventas.oonabe.category.entity.Category;
import com.hean.consigueventas.oonabe.category.repository.CategoryRepository;
import com.hean.consigueventas.oonabe.common.exception.BusinessLogicException;
import com.hean.consigueventas.oonabe.common.exception.ResourceNotFoundException;
import com.hean.consigueventas.oonabe.community.dto.request.MatchRequestUpsertRequest;
import com.hean.consigueventas.oonabe.community.dto.response.MatchSubmissionResponse;
import com.hean.consigueventas.oonabe.community.entity.MatchRequest;
import com.hean.consigueventas.oonabe.community.repository.MatchRequestRepository;
import com.hean.consigueventas.oonabe.user.entity.User;
import com.hean.consigueventas.oonabe.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class MatchRequestService {

    private static final String CONFIRMATION_MESSAGE =
            "Gracias por participar. M\u00e1s adelante te enviaremos por WhatsApp la informaci\u00f3n de tu match.";

    private final MatchRequestRepository matchRequestRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    public MatchRequestService(
            MatchRequestRepository matchRequestRepository,
            UserRepository userRepository,
            CategoryRepository categoryRepository) {
        this.matchRequestRepository = matchRequestRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
    }

    @Transactional
    public MatchSubmissionResponse upsert(Long userId, MatchRequestUpsertRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + userId));
        Set<Category> categories = resolveActiveCategories(request.categoryIds());

        MatchRequest matchRequest = matchRequestRepository.findByUserId(userId)
                .orElseGet(() -> newMatchRequest(user));

        matchRequest.setName(request.name().trim());
        matchRequest.setAge(request.age());
        matchRequest.setEmail(request.email().trim().toLowerCase());
        matchRequest.setWhatsapp(request.whatsapp().trim());
        matchRequest.setExactZone(request.exactZone().trim());
        matchRequest.setGender(request.gender());
        matchRequest.setLanguages(new HashSet<>(request.languages()));
        matchRequest.setCategories(categories);
        matchRequest.setAvailableDays(new HashSet<>(request.availableDays()));
        matchRequest.setExpectations(request.expectations().trim());
        matchRequest.setDescriptors(request.descriptors() == null
                ? new HashSet<>()
                : new HashSet<>(request.descriptors()));

        matchRequestRepository.save(matchRequest);
        return new MatchSubmissionResponse(CONFIRMATION_MESSAGE);
    }

    private MatchRequest newMatchRequest(User user) {
        MatchRequest matchRequest = new MatchRequest();
        matchRequest.setUser(user);
        return matchRequest;
    }

    private Set<Category> resolveActiveCategories(Set<Long> categoryIds) {
        List<Category> categories = categoryRepository.findAllById(categoryIds);
        if (categories.size() != categoryIds.size()) {
            throw new ResourceNotFoundException("Una o mas categorias seleccionadas no existen.");
        }
        if (categories.stream().anyMatch(category -> !category.isActive())) {
            throw new BusinessLogicException("Todas las categorias seleccionadas deben estar activas.");
        }
        return new HashSet<>(categories);
    }
}
