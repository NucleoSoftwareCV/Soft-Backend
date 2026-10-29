package com.hean.consigueventas.oonabe.travel;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Opt-in developer smoke check; never uses stored user locations.
 * Run explicitly with -Doona.routing.smoke=true -Dtest=DrivingRoutesSmokeTest.
 */
@EnabledIfSystemProperty(named = "oona.routing.smoke", matches = "true")
class DrivingRoutesSmokeTest {
    @Test
    void publicDocumentationSampleReturnsRoadDistanceAndDrivingMinutes() {
        var provider = new DrivingRoutes("https://router.project-osrm.org");
        // Public sample coordinates from OSRM documentation (Berlin).
        var origin = new DrivingRoutes.Point(52.517037, 13.388860);
        var destination = new DrivingRoutes.Point(52.529407, 13.397634);
        var result = provider.routes(origin, List.of(destination)).get(destination);
        assertThat(result).as("OSRM demo response through the application's Java HTTP client").isNotNull();
        assertThat(result.distanceKm()).isPositive();
        assertThat(result.durationMinutes()).isPositive();
    }
}
