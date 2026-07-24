package com.hean.consigueventas.oonabe.interaction.service;

import com.hean.consigueventas.oonabe.common.enums.EventOccurrenceStatus;
import com.hean.consigueventas.oonabe.common.enums.FavoriteEntityType;
import com.hean.consigueventas.oonabe.common.exception.ResourceNotFoundException;
import com.hean.consigueventas.oonabe.event.entity.Event;
import com.hean.consigueventas.oonabe.event.entity.EventOccurrence;
import com.hean.consigueventas.oonabe.event.repository.EventRepository;
import com.hean.consigueventas.oonabe.interaction.dto.FavoriteIdsResponse;
import com.hean.consigueventas.oonabe.interaction.dto.FavoriteResponse;
import com.hean.consigueventas.oonabe.interaction.dto.FavoriteStatusResponse;
import com.hean.consigueventas.oonabe.interaction.dto.FavoriteToggleRequest;
import com.hean.consigueventas.oonabe.interaction.entity.Favorite;
import com.hean.consigueventas.oonabe.interaction.entity.FavoriteId;
import com.hean.consigueventas.oonabe.interaction.repository.FavoriteRepository;
import com.hean.consigueventas.oonabe.oneToOneSession.entity.OneToOneService;
import com.hean.consigueventas.oonabe.oneToOneSession.repository.OneToOneServiceRepository;
import com.hean.consigueventas.oonabe.profileCliente.entity.CustomerProfile;
import com.hean.consigueventas.oonabe.profileProfesional.entity.SpecialistProfile;
import com.hean.consigueventas.oonabe.profileProfesional.repository.SpecialistProfileRepository;
import com.hean.consigueventas.oonabe.user.repository.TemporaryCustomerProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final TemporaryCustomerProfileRepository customerProfileRepository;
    private final EventRepository eventRepository;
    private final OneToOneServiceRepository oneToOneServiceRepository;
    private final SpecialistProfileRepository specialistProfileRepository;

    private CustomerProfile getCustomerByUserId(Long userId) {
        return customerProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Perfil de cliente no encontrado para el usuario: " + userId));
    }

    @Transactional
    public FavoriteStatusResponse toggleFavorite(Long userId, FavoriteToggleRequest request) {
        CustomerProfile customer = getCustomerByUserId(userId);
        
        // Validar que la entidad existe antes de marcarla como favorito
        validateEntityExists(request.entityType(), request.entityId());

        FavoriteId favId = new FavoriteId();
        favId.setCustomerId(customer.getId());
        favId.setEntityType(request.entityType());
        favId.setEntityId(request.entityId());

        Optional<Favorite> existing = favoriteRepository.findById(favId);
        if (existing.isPresent()) {
            favoriteRepository.delete(existing.get());
            log.info("Usuario {} eliminó de favoritos {} ID {}", userId, request.entityType(), request.entityId());
            return new FavoriteStatusResponse(false);
        } else {
            Favorite favorite = new Favorite();
            favorite.setId(favId);
            favorite.setCustomer(customer);
            favoriteRepository.save(favorite);
            log.info("Usuario {} añadió a favoritos {} ID {}", userId, request.entityType(), request.entityId());
            return new FavoriteStatusResponse(true);
        }
    }

    @Transactional(readOnly = true)
    public FavoriteIdsResponse getFavoriteIds(Long userId) {
        CustomerProfile customer = getCustomerByUserId(userId);
        List<Favorite> list = favoriteRepository.findByIdCustomerId(customer.getId());

        Set<Long> eventIds = new HashSet<>();
        Set<Long> serviceIds = new HashSet<>();
        Set<Long> professionalIds = new HashSet<>();

        for (Favorite fav : list) {
            FavoriteId id = fav.getId();
            if (id.getEntityType() == FavoriteEntityType.EVENTO) {
                eventIds.add(id.getEntityId());
            } else if (id.getEntityType() == FavoriteEntityType.SERVICIO) {
                serviceIds.add(id.getEntityId());
            } else if (id.getEntityType() == FavoriteEntityType.PROFESIONAL) {
                professionalIds.add(id.getEntityId());
            }
        }

        return new FavoriteIdsResponse(eventIds, serviceIds, professionalIds);
    }

    @Transactional(readOnly = true)
    public List<FavoriteResponse> getFavoritesDetails(Long userId) {
        CustomerProfile customer = getCustomerByUserId(userId);
        List<Favorite> list = favoriteRepository.findByIdCustomerId(customer.getId());

        List<FavoriteResponse> details = new ArrayList<>();

        for (Favorite fav : list) {
            FavoriteId id = fav.getId();
            try {
                if (id.getEntityType() == FavoriteEntityType.EVENTO) {
                    eventRepository.findById(id.getEntityId()).ifPresent(event -> {
                        String locationName = getEventCityName(event);
                        details.add(new FavoriteResponse(
                                FavoriteEntityType.EVENTO,
                                event.getId(),
                                event.getTitle(),
                                null, // El frontend gestionará el fallbackImage
                                event.getCategory() != null ? event.getCategory().getName() : "EVENTO",
                                locationName != null ? locationName : "Presencial",
                                event.getPriceFrom(),
                                event.getCurrency(),
                                null,
                                event.getSpecialist() != null ? event.getSpecialist().getPublicName() : null,
                                getEventNextStartsAt(event),
                                event.isRecurring() ? "RECURRENTE" : "ÚNICO"
                        ));
                    });
                } else if (id.getEntityType() == FavoriteEntityType.SERVICIO) {
                    oneToOneServiceRepository.findById(id.getEntityId()).ifPresent(service -> {
                        String locationName = service.getModality().name().equals("ONLINE") ? "Online" :
                                (service.getLocation() != null && service.getLocation().getCity() != null ? 
                                 service.getLocation().getCity().getName() : "Presencial");
                        details.add(new FavoriteResponse(
                                FavoriteEntityType.SERVICIO,
                                service.getId(),
                                service.getTitle(),
                                service.getImageUrl(),
                                "SESIONES 1:1",
                                locationName,
                                service.getPrice(),
                                service.getCurrency(),
                                service.getSlug(),
                                service.getSpecialist() != null ? service.getSpecialist().getPublicName() : null,
                                null,
                                "SESIÓN 1:1"
                        ));
                    });
                } else if (id.getEntityType() == FavoriteEntityType.PROFESIONAL) {
                    specialistProfileRepository.findById(id.getEntityId()).ifPresent(specialist -> {
                        details.add(new FavoriteResponse(
                                FavoriteEntityType.PROFESIONAL,
                                specialist.getId(),
                                specialist.getPublicName(),
                                specialist.getPhotoUrl(),
                                specialist.getProfileCategory(),
                                "Presencial / Online",
                                null,
                                null,
                                specialist.getSlug(),
                                null,
                                null,
                                "PROFESIONAL"
                        ));
                    });
                }
            } catch (Exception e) {
                log.error("Error al cargar detalles de favorito tipo {} con ID {}: {}", id.getEntityType(), id.getEntityId(), e.getMessage());
            }
        }

        return details;
    }

    private void validateEntityExists(FavoriteEntityType entityType, Long entityId) {
        boolean exists = false;
        if (entityType == FavoriteEntityType.EVENTO) {
            exists = eventRepository.existsById(entityId);
        } else if (entityType == FavoriteEntityType.SERVICIO) {
            exists = oneToOneServiceRepository.existsById(entityId);
        } else if (entityType == FavoriteEntityType.PROFESIONAL) {
            exists = specialistProfileRepository.existsById(entityId);
        }
        
        if (!exists) {
            throw new ResourceNotFoundException("No se encontró la entidad de tipo " + entityType + " con ID " + entityId);
        }
    }

    private String getEventCityName(Event event) {
        if (event.getOccurrences() == null || event.getOccurrences().isEmpty()) {
            return null;
        }
        EventOccurrence occurrence = event.getOccurrences().stream()
                .filter(o -> o.getStatus() == EventOccurrenceStatus.PROGRAMADA)
                .min(Comparator.comparing(EventOccurrence::getStartsAt))
                .orElse(event.getOccurrences().getFirst());

        if (occurrence == null || occurrence.getLocation() == null || occurrence.getLocation().getCity() == null) {
            return null;
        }
        return occurrence.getLocation().getCity().getName();
    }

    private Instant getEventNextStartsAt(Event event) {
        if (event.getOccurrences() == null || event.getOccurrences().isEmpty()) {
            return null;
        }
        EventOccurrence occurrence = event.getOccurrences().stream()
                .filter(o -> o.getStatus() == EventOccurrenceStatus.PROGRAMADA)
                .min(Comparator.comparing(EventOccurrence::getStartsAt))
                .orElse(event.getOccurrences().getFirst());
        return occurrence == null ? null : occurrence.getStartsAt();
    }
}