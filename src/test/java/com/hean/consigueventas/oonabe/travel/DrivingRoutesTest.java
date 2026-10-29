package com.hean.consigueventas.oonabe.travel;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import static org.assertj.core.api.Assertions.*;
class DrivingRoutesTest {
    @Test void disabledProviderDoesNotInventTravelTimes() {
        assertThat(new DrivingRoutes("").routes(new DrivingRoutes.Point(0,0),List.of(new DrivingRoutes.Point(1,1)))).isEmpty();
    }
    @Test void readsTableUnitsAndCachesIdenticalRoute() throws Exception {
        HttpServer server=HttpServer.create(new InetSocketAddress("127.0.0.1",0),0);
        AtomicInteger calls=new AtomicInteger();
        server.createContext("/table/v1/driving/",exchange->{
            calls.incrementAndGet();
            byte[] bytes=("{\"code\":\"Ok\",\"distances\":[[0,12500]],\"durations\":[[0,900]],"
                +"\"sources\":[{\"distance\":0}],\"destinations\":[{\"distance\":0},{\"distance\":20}]}").getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type","application/json");
            exchange.sendResponseHeaders(200,bytes.length);exchange.getResponseBody().write(bytes);exchange.close();
        });
        server.start();
        try {
            var provider=new DrivingRoutes("http://127.0.0.1:"+server.getAddress().getPort());
            var origin=new DrivingRoutes.Point(40,-3);var destination=new DrivingRoutes.Point(41,2);
            var result=provider.routes(origin,List.of(destination)).get(destination);
            assertThat(result.distanceKm()).isEqualTo(12.5);assertThat(result.durationMinutes()).isEqualTo(15);
            assertThat(provider.routes(origin,List.of(destination)).get(destination)).isEqualTo(result);
            assertThat(calls.get()).isEqualTo(1);
        } finally { server.stop(0); }
    }
    @Test void routingFailureDoesNotInventAnEstimate() throws Exception {
        HttpServer server=HttpServer.create(new InetSocketAddress("127.0.0.1",0),0);
        server.createContext("/",exchange->{exchange.sendResponseHeaders(503,-1);exchange.close();});
        server.start();
        try {
            var provider=new DrivingRoutes("http://127.0.0.1:"+server.getAddress().getPort());
            assertThat(provider.routes(new DrivingRoutes.Point(40,-3),List.of(new DrivingRoutes.Point(41,2)))).isEmpty();
        } finally { server.stop(0); }
    }

    @Test void farSnappedPointsAreRejectedRatherThanShownAsAnExactRoute() throws Exception {
        HttpServer server=HttpServer.create(new InetSocketAddress("127.0.0.1",0),0);
        server.createContext("/",exchange->{
            byte[] bytes=("{\"code\":\"Ok\",\"distances\":[[0,12500]],\"durations\":[[0,900]],"
                +"\"sources\":[{\"distance\":0}],\"destinations\":[{\"distance\":0},{\"distance\":2000}]}").getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type","application/json");
            exchange.sendResponseHeaders(200,bytes.length);exchange.getResponseBody().write(bytes);exchange.close();
        });
        server.start();
        try {
            var provider=new DrivingRoutes("http://127.0.0.1:"+server.getAddress().getPort());
            assertThat(provider.routes(new DrivingRoutes.Point(40,-3),List.of(new DrivingRoutes.Point(41,2)))).isEmpty();
        } finally { server.stop(0); }
    }

}
