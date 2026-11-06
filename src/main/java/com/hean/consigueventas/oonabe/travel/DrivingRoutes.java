package com.hean.consigueventas.oonabe.travel;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.*;
@Component
public class DrivingRoutes {
    public record Point(double latitude, double longitude) {}
    public record Route(double distanceKm, double durationMinutes) {}
    private record Cached(Route route, long expires) {}
    private final Map<String,Cached> cache = new LinkedHashMap<>();
    private final String baseUrl;
    private final RestClient client;
    private long nextRequest;
    public DrivingRoutes(@Value("${oona.travel.routing-url:}") String baseUrl) {
        this.baseUrl=baseUrl.replaceAll("/+$","");
        var factory=new JdkClientHttpRequestFactory(HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build());
        factory.setReadTimeout(Duration.ofSeconds(8));
        client=RestClient.builder().requestFactory(factory).build();
    }
    // Disabled unless the operator explicitly configures an authorized routing provider.
    public synchronized Map<Point,Route> routes(Point origin,List<Point> destinations) {
        var result=new HashMap<Point,Route>();
        long now=System.currentTimeMillis();
        cache.entrySet().removeIf(e -> e.getValue().expires()<=now);
        var missing=new ArrayList<Point>();
        for(var point:destinations.stream().distinct().toList()) {
            var cached=cache.get(origin+":"+point);
            if(cached!=null) result.put(point,cached.route()); else missing.add(point);
        }
        if(missing.isEmpty()||baseUrl.isBlank()||now<nextRequest) return result;
        nextRequest=now+1000;
        String coordinates=origin.longitude()+","+origin.latitude();
        for(var point:missing) coordinates+=";"+point.longitude()+","+point.latitude();
        try {
            JsonNode body=client.get().uri(baseUrl+"/table/v1/driving/"+coordinates+"?sources=0&annotations=distance,duration").retrieve().body(JsonNode.class);
            if(body==null||!"Ok".equals(body.path("code").asText())) return result;
            for(int i=0;i<missing.size();i++) {
                var d=body.path("distances").path(0).path(i+1);
                var t=body.path("durations").path(0).path(i+1);
                double meters=d.asDouble(-1),seconds=t.asDouble(-1);
                double snap=body.path("sources").path(0).path("distance").asDouble(Double.POSITIVE_INFINITY);
                double targetSnap=body.path("destinations").path(i+1).path("distance").asDouble(Double.POSITIVE_INFINITY);
                if(!d.isNumber()||!t.isNumber()||!Double.isFinite(meters)||!Double.isFinite(seconds)
                        ||meters<0||seconds<0||!Double.isFinite(snap)||!Double.isFinite(targetSnap)||snap>1000||targetSnap>1000) continue;
                var route=new Route(meters/1000,seconds/60); result.put(missing.get(i),route);
                if(cache.size()>=1000) cache.remove(cache.keySet().iterator().next());
                cache.put(origin+":"+missing.get(i),new Cached(route,now+300_000));
            }
        } catch(RuntimeException ignored) { /* Caller retains explicitly labelled straight-line distance. */ }
        return result;
    }
}
