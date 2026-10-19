package com.hean.consigueventas.oonabe.recommendation;

import com.hean.consigueventas.oonabe.common.exception.ResourceNotFoundException;
import com.hean.consigueventas.oonabe.event.entity.Event;
import com.hean.consigueventas.oonabe.profileCliente.entity.ClientProfile;
import com.hean.consigueventas.oonabe.profileCliente.repository.ClientProfileRepository;
import com.hean.consigueventas.oonabe.user.entity.User;
import com.hean.consigueventas.oonabe.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class RecommendationService {

    private final UserRepository userRepository;
    private final ClientProfileRepository clientProfileRepository;
    private final RecommendationCandidateProvider candidateProvider;

    public RecommendationService(
            UserRepository userRepository,
            ClientProfileRepository clientProfileRepository,
            RecommendationCandidateProvider candidateProvider) {
        this.userRepository = userRepository;
        this.clientProfileRepository = clientProfileRepository;
        this.candidateProvider = candidateProvider;
    }

    public List<Event> getRecommendations(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario autenticado no encontrado."));

        String cityName = clientProfileRepository.findByUserId(user.getId())
                .map(ClientProfile::getCity)
                .map(city -> city.getName())
                .orElse(null);

        return candidateProvider.findCandidates(cityName);
    }
}
