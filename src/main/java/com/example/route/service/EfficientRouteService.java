package com.example.route.service;

import com.example.route.config.GoogleApiProperties;
import com.example.route.model.RouteResult;
import com.google.maps.DirectionsApi; 
import com.google.maps.GeoApiContext; 
import com.google.maps.internal.PolylineEncoding;
import com.google.maps.model.DirectionsLeg;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.DirectionsStep; 
import com.google.maps.model.LatLng;
import com.google.maps.model.TravelMode;
 
 
import org.apache.commons.rng.UniformRandomProvider;
import org.apache.commons.rng.simple.RandomSource;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*; 
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor; 

@Service
@RequiredArgsConstructor
public class EfficientRouteService {	   
	
    private final int STATIC_MAP_WIDTH = 800;
    private final int STATIC_MAP_HEIGHT = 600; 
    
    private final GoogleApiProperties googleApiProperties;
    private final GetPic getPic ;
     
    
    
    protected GeoApiContext getGeoApiContext(final String apikey) {    	
    	 GeoApiContext context = new GeoApiContext.Builder()
                .apiKey(apikey)
                .build();
    	return context ;
    }
    protected String getRandomKeys() {
    	List<String> keys = googleApiProperties.getKeys() ;
    	UniformRandomProvider rng = RandomSource.XO_RO_SHI_RO_128_PP.create();
    	int m = rng.nextInt(keys.size());
    	return keys.get(m) ;
    }
    
    @Cacheable(value = "routeResults", key = "#origin + '-' + #destination", sync = true)
    public RouteResult calculateRoute(String origin, String destination) {
    	final String apikey = getRandomKeys();
        try {
            // 使用 Directions API 獲取導航路線
            DirectionsResult directions = DirectionsApi.newRequest(getGeoApiContext(apikey))
                .origin(origin)                
                .destination(destination)
                .mode(TravelMode.DRIVING)  // 可以改為 WALKING, BICYCLING, TRANSIT
                .alternatives(true)        // 獲取多條可能路線
                .language("zh-TW")         // 設定語言為繁體中文
                .await();

            if (directions.routes.length == 0) {
                throw new RuntimeException("找不到路線");
            }

            // 選擇最佳路線（預設是第一條路線）
            DirectionsRoute bestRoute = directions.routes[0];
            
            // 計算總距離
            long totalDistance = 0;
            for (DirectionsLeg leg : bestRoute.legs) {
                totalDistance += leg.distance.inMeters;
            }

            // 提取路線上所有的座標點
            List<LatLng> pathPoints = extractPathPointsV2(bestRoute);
            
            // 生成包含實際路線的靜態地圖URL
            String mapUrl = generateNavigationMapUrl(pathPoints, origin, destination);
            String base64MapPic = getPic.getBase64FromPicUrl(mapUrl);

            // 構建詳細的路線說明
            List<String> navigationSteps = new ArrayList<>();
            for (DirectionsLeg leg : bestRoute.legs) {
                for (DirectionsStep step : leg.steps) {
                    navigationSteps.add(step.htmlInstructions);
                }
            }

            // 獲取預估時間
            long durationInSeconds = bestRoute.legs[0].duration.inSeconds;

            return new RouteResult(
                totalDistance,
                mapUrl,
                base64MapPic,
                navigationSteps,
                durationInSeconds,
                bestRoute.summary
            );
        } catch (Exception e) {
			throw new RuntimeException("use apikey: %s , failure to calculate route: %s".formatted(apikey, e.getMessage()), e);
        }
    }

//    @Deprecated
    private List<LatLng> extractPathPoints(DirectionsRoute route) {
        List<LatLng> points = new ArrayList<>();
        for (DirectionsLeg leg : route.legs) {
            for (DirectionsStep step : leg.steps) {
                // 解碼 Google 的編碼路徑點
                points.addAll(decodePath(step.polyline.getEncodedPath()));
            }
        }
        return points;
    }
    @Deprecated
    private List<LatLng> extractPathPointsV2(DirectionsRoute route) {
    	 // 從路線結果中獲取起點和終點座標
    	DirectionsLeg[] legs = route.legs ; 
    	DirectionsLeg leg = route.legs[0] ; 
    	LatLng start  = leg.startLocation;
    	LatLng end  = leg.endLocation;
    	
    	// 從路徑中獲取中間點（最多取8個點以符合URL長度限制）
    	List<LatLng> points = new ArrayList<>();
    	int pathSize = route.overviewPolyline.decodePath().size();
//    	int steps = Math.max(1, pathSize / 8); 
    	
    	for (int i = 0; i < pathSize; i ++) {
    		 // 解碼 Google 的編碼路徑點
    	     LatLng point = route.overviewPolyline.decodePath().get(i);
    	     points.add(point);
    	}
        return points;
    }

    private List<LatLng> decodePath(String encodedPath) {
        return PolylineEncoding.decode(encodedPath);
    }

    private String generateNavigationMapUrl(List<LatLng> pathPoints, String origin, String destination) {
    	final String apikey = getRandomKeys() ;
        try {
            // 將路線點轉換為路徑字串
            String pathStr = pathPoints.stream()
                .map(point -> point.lat + "," + point.lng)
                .collect(Collectors.joining("|"));

            // 編碼路徑參數
            String encodedPath = "color:0x0000ff|weight:5|" + pathStr;
            String encodedMarkers = String.format("color:red|label:A|%s&markers=color:red|label:B|%s",
                URLEncoder.encode(origin, StandardCharsets.UTF_8.toString()),
                URLEncoder.encode(destination, StandardCharsets.UTF_8.toString()));

            // 生成完整的地圖URL 
            return String.format(
                    "https://maps.googleapis.com/maps/api/staticmap?" +
                    "size=%dx%d&path=%s&markers=%s&language=zh-TW&key=%s",
                    STATIC_MAP_WIDTH, STATIC_MAP_HEIGHT,
                    URLEncoder.encode(encodedPath, StandardCharsets.UTF_8.toString()),
                    encodedMarkers,
                    apikey
                );
        } catch (Exception e) {
            throw new RuntimeException("use apikey: %s , failure to generate Map ".formatted(apikey), e);            
        }
    }
}