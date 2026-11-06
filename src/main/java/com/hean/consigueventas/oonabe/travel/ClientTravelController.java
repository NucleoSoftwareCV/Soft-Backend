package com.hean.consigueventas.oonabe.travel;
import com.hean.consigueventas.oonabe.profileCliente.service.ClientProfileService;
import com.hean.consigueventas.oonabe.profileCliente.dto.request.ReferenceLocationRequest;
import com.hean.consigueventas.oonabe.profileCliente.dto.response.ReferenceLocationResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.hean.consigueventas.oonabe.common.config.OpenApiConfig;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.List;
@Tag(name = "Ubicación y desplazamientos del cliente")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
@RestController @RequiredArgsConstructor @PreAuthorize("hasRole('USER')")
@RequestMapping("/api/v1/client-profiles/me")
public class ClientTravelController {
    public record Request(@NotEmpty @Size(max=50) List<@NotNull @Positive Long> occurrenceIds) {}
    private final ClientProfileService profiles;
    private final EventTravelService travel;
    private <T> ResponseEntity<T> response(T body) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore().cachePrivate()).body(body);
    }
    @GetMapping("/reference-location")
    public ResponseEntity<ReferenceLocationResponse> get(Principal user) { return response(profiles.getReferenceLocation(user.getName())); }
    @PutMapping("/reference-location")
    public ResponseEntity<ReferenceLocationResponse> save(Principal user,@Valid @RequestBody ReferenceLocationRequest request) {
        return response(profiles.saveReferenceLocation(user.getName(),request));
    }
    @DeleteMapping("/reference-location")
    public ResponseEntity<Void> clear(Principal user) {
        profiles.clearReferenceLocation(user.getName());
        return ResponseEntity.noContent().cacheControl(CacheControl.noStore().cachePrivate()).build();
    }
    @PostMapping("/event-travel")
    public ResponseEntity<List<EventTravelService.Travel>> calculate(Principal user,@Valid @RequestBody Request request) {
        return response(travel.calculate(user.getName(),request.occurrenceIds()));
    }
}
