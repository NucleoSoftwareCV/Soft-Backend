package com.hean.consigueventas.oonabe.travel;
import com.hean.consigueventas.oonabe.event.repository.EventMapRepository;
import com.hean.consigueventas.oonabe.profileCliente.service.ClientProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.*;
@Service @RequiredArgsConstructor
public class EventTravelService {
    public record Travel(Long occurrenceId,String status,Double distanceKm,Double durationMinutes,
            String method,boolean approximate,String address,String cityName,String directionsUrl) {}
    private final ClientProfileService profiles;
    private final EventMapRepository destinations;
    private final DrivingRoutes routes;
    public List<Travel> calculate(String username,List<Long> ids) {
        var origin=profiles.getReferenceLocation(username);
        if(origin==null||!valid(origin.latitude(),origin.longitude()))
            return ids.stream().distinct().map(id->new Travel(id,"NO_REFERENCE",null,null,null,false,null,null,null)).toList();
        var points=destinations.findTravelDestinations(Instant.now(),ids);
        var source=new DrivingRoutes.Point(origin.latitude(),origin.longitude());
        var found=routes.routes(source,points.stream().map(p->new DrivingRoutes.Point(p.latitude().doubleValue(),p.longitude().doubleValue())).toList());
        Map<Long,Travel> result=new HashMap<>();
        for(var p:points) {
            var destination=new DrivingRoutes.Point(p.latitude().doubleValue(),p.longitude().doubleValue());
            var route=found.get(destination);
            boolean approximate=Boolean.TRUE.equals(origin.approximate())||(p.address()!=null&&p.address().startsWith("Ubicación aproximada"));
            String url="https://www.google.com/maps/dir/?api=1&origin="+source.latitude()+","+source.longitude()
                    +"&destination="+destination.latitude()+","+destination.longitude()+"&travelmode=driving";
            result.put(p.occurrenceId(),new Travel(p.occurrenceId(),"AVAILABLE",
                    route==null?distanceKm(source,destination):route.distanceKm(),route==null?null:route.durationMinutes(),
                    route==null?"STRAIGHT_LINE":"ROUTE",approximate,p.address(),p.cityName(),url));
        }
        return ids.stream().distinct().map(id->result.getOrDefault(id,new Travel(id,"UNAVAILABLE",null,null,null,false,null,null,null))).toList();
    }
    static boolean valid(Double lat,Double lon) {
        return lat!=null&&lon!=null&&Double.isFinite(lat)&&Double.isFinite(lon)&&Math.abs(lat)<=90&&Math.abs(lon)<=180;
    }
    static double distanceKm(DrivingRoutes.Point a,DrivingRoutes.Point b) {
        double lat=Math.toRadians(b.latitude()-a.latitude()),lon=Math.toRadians(b.longitude()-a.longitude());
        double h=Math.pow(Math.sin(lat/2),2)+Math.cos(Math.toRadians(a.latitude()))*Math.cos(Math.toRadians(b.latitude()))*Math.pow(Math.sin(lon/2),2);
        return 6371.0088*2*Math.asin(Math.sqrt(Math.min(1,Math.max(0,h))));
    }
}
