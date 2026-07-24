package com.hean.consigueventas.oonabe.interaction.controller;

import com.hean.consigueventas.oonabe.common.security.SecurityUtils;
import com.hean.consigueventas.oonabe.interaction.dto.FavoriteIdsResponse;
import com.hean.consigueventas.oonabe.interaction.dto.FavoriteResponse;
import com.hean.consigueventas.oonabe.interaction.dto.FavoriteStatusResponse;
import com.hean.consigueventas.oonabe.interaction.dto.FavoriteToggleRequest;
import com.hean.consigueventas.oonabe.interaction.service.FavoriteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/favorites")
@RequiredArgsConstructor
@Tag(name = "Favorites", description = "Endpoints para la gestión de favoritos/guardados del cliente")
public class FavoriteController {

    private final FavoriteService favoriteService;

    @PostMapping("/toggle")
    @Operation(summary = "Alternar favorito", description = "Agrega o quita un elemento (evento, servicio o profesional) de la lista de favoritos de un usuario")
    public ResponseEntity<FavoriteStatusResponse> toggleFavorite(@Valid @RequestBody FavoriteToggleRequest request) {
        Long userId = SecurityUtils.getAuthenticatedUserId();
        return ResponseEntity.ok(favoriteService.toggleFavorite(userId, request));
    }

    @GetMapping("/ids")
    @Operation(summary = "Obtener los IDs de favoritos", description = "Retorna un set con los IDs de eventos, servicios y profesionales favoritos del usuario")
    public ResponseEntity<FavoriteIdsResponse> getFavoriteIds() {
        Long userId = SecurityUtils.getAuthenticatedUserId();
        return ResponseEntity.ok(favoriteService.getFavoriteIds(userId));
    }

    @GetMapping
    @Operation(summary = "Obtener detalles de todos los favoritos", description = "Retorna la lista completa de favoritos del usuario con sus datos detallados")
    public ResponseEntity<List<FavoriteResponse>> getFavoritesDetails() {
        Long userId = SecurityUtils.getAuthenticatedUserId();
        return ResponseEntity.ok(favoriteService.getFavoritesDetails(userId));
    }
}