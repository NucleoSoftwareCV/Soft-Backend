package com.hean.consigueventas.oonabe.travel;
import com.hean.consigueventas.oonabe.profileCliente.dto.response.ReferenceLocationResponse;
import com.hean.consigueventas.oonabe.profileCliente.service.ClientProfileService;
import com.hean.consigueventas.oonabe.event.repository.EventMapRepository;
import com.hean.consigueventas.oonabe.event.dto.response.EventMapPointResponse;
import com.hean.consigueventas.oonabe.common.enums.EventModality;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.*;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;
class EventTravelServiceTest {
    ClientProfileService profiles=mock(ClientProfileService.class);
    EventMapRepository repository=mock(EventMapRepository.class);
    DrivingRoutes routes=mock(DrivingRoutes.class);
    EventTravelService service=new EventTravelService(profiles,repository,routes);
    private EventMapPointResponse point() {
        return new EventMapPointResponse(7L,1L,"Yoga",BigDecimal.TEN,"EUR",null,null,"Centro",
            "Ubicación aproximada en Valencia","Valencia",new BigDecimal("39.47"),new BigDecimal("-0.38"),
            null,1L,"Yoga",null,null,1L,"Profesional",EventModality.PRESENCIAL,false,null);
    }
    private void origin(double lat,double lon) {
        when(profiles.getReferenceLocation("ana")).thenReturn(new ReferenceLocationResponse("Mi referencia",lat,lon,false));
    }
    @Test void missingOriginNeverQueriesDestinationsOrProvider() {
        assertThat(service.calculate("ana",List.of(7L)).getFirst().status()).isEqualTo("NO_REFERENCE");
        verifyNoInteractions(repository,routes);
    }
    @Test void invalidOriginDoesNotProduceDistances() {
        origin(Double.NaN,0);
        assertThat(service.calculate("ana",List.of(7L)).getFirst().distanceKm()).isNull();
        verifyNoInteractions(repository,routes);
    }
    @Test void unavailableSessionsHaveNoInventedMetrics() {
        origin(40,-3); when(repository.findTravelDestinations(any(),any())).thenReturn(List.of());
        assertThat(service.calculate("ana",List.of(7L)).getFirst().durationMinutes()).isNull();
        assertThat(service.calculate("ana",List.of(7L)).getFirst().status()).isEqualTo("UNAVAILABLE");
    }
    @Test void providerFailureKeepsOnlyLabelledStraightLineDistance() {
        origin(40.42,-3.70); when(repository.findTravelDestinations(any(),any())).thenReturn(List.of(point()));
        when(routes.routes(any(),any())).thenReturn(Map.of());
        var result=service.calculate("ana",List.of(7L)).getFirst();
        assertThat(result.method()).isEqualTo("STRAIGHT_LINE");
        assertThat(result.distanceKm()).isBetween(290.0,310.0);
        assertThat(result.durationMinutes()).isNull();
        assertThat(result.approximate()).isTrue();
    }
    @Test void routeUsesProviderDistanceAndTimeAndChangesWithOrigin() {
        origin(40,-3); when(repository.findTravelDestinations(any(),any())).thenReturn(List.of(point()));
        when(routes.routes(eq(new DrivingRoutes.Point(40,-3)),any())).thenReturn(
            Map.of(new DrivingRoutes.Point(39.47,-0.38),new DrivingRoutes.Route(350,240)));
        var first=service.calculate("ana",List.of(7L)).getFirst();
        assertThat(first.distanceKm()).isEqualTo(350);
        assertThat(first.durationMinutes()).isEqualTo(240);
        origin(39.47,-0.38); when(routes.routes(eq(new DrivingRoutes.Point(39.47,-0.38)),any())).thenReturn(Map.of());
        assertThat(service.calculate("ana",List.of(7L)).getFirst().distanceKm()).isZero();
    }
    @Test void handlesAntipodalAndZeroCoordinates() {
        assertThat(EventTravelService.distanceKm(new DrivingRoutes.Point(0,0),new DrivingRoutes.Point(0,180))).isBetween(20000.0,20020.0);
        assertThat(EventTravelService.valid(0.0,0.0)).isTrue();
    }
}
