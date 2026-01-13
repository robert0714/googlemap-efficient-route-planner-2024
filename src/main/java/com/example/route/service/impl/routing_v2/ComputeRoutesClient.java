package com.example.route.service.impl.routing_v2;
import com.google.api.gax.rpc.FixedHeaderProvider;
import com.google.auto.value.AutoValue.Builder; 
import com.google.maps.routing.v2.ComputeRoutesRequest;
import com.google.maps.routing.v2.ComputeRoutesResponse;
import com.google.maps.routing.v2.Location;
import com.google.maps.routing.v2.Route;
import com.google.maps.routing.v2.RouteLeg;
import com.google.maps.routing.v2.RouteLegStep;
import com.google.maps.routing.v2.RouteLegStepTransitDetails;
import com.google.maps.routing.v2.RouteTravelMode;
import com.google.maps.routing.v2.RoutesClient;
import com.google.maps.routing.v2.RoutesSettings;
import com.google.maps.routing.v2.TransitLine;
import com.google.maps.routing.v2.TransitPreferences;
import com.google.maps.routing.v2.TransitPreferences.TransitRoutingPreference;
import com.google.maps.routing.v2.TransitPreferences.TransitTravelMode;
import com.google.maps.routing.v2.Units;
import com.google.maps.routing.v2.Waypoint;
import com.google.type.LatLng;
import com.google.type.Money;
import com.google.api.gax.core.NoCredentialsProvider;
import com.google.api.gax.grpc.InstantiatingGrpcChannelProvider;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Comparator;
import java.util.HashMap;

import lombok.Data;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ComputeRoutesClient implements AutoCloseable{ 
	private final String apikey ;
    private static final String STATIC_MAP_BASE_URL = "https://maps.googleapis.com/maps/api/staticmap";
    private static final String ENDPOINT = "routes.googleapis.com:443";
    private static final RouteTravelMode TRAVEL_MODE= RouteTravelMode.TRANSIT;
    private RoutesClient routesClient;
    
    public ComputeRoutesClient(String apikey) {
    	this.apikey = apikey;
    }
	public void close() throws Exception {
		if (routesClient != null) {
            routesClient.close();
        }		
	}
	
	/**
     * 初始化 RoutesClient
     * 關鍵：將所有 Header 放入同一個 Map，避免被覆蓋
     */
    private RoutesClient getRoutesClient() throws IOException {
        if (routesClient == null) {
            // 1. 準備所有的 Headers
            Map<String, String> headers = new HashMap<>();
            headers.put("X-Goog-Api-Key", this.apikey);
            
            // 關鍵：FieldMask 決定了 Response 哪些欄位會有資料，沒設的話會報錯
            String [] fields = {
//            		"routes.duration",
//            		"routes.description",
//            		"routes.distance_meters",
//            		"routes.polyline.encoded_polyline",
//            		"routes.optimized_intermediate_waypoint_index",
//            		"routes.legs",
//            		"routes.legs",
//            		"routes.legs.distance_meters",
//            		"routes.legs.steps.start_location.lat_lng",
//            		"routes.legs.steps.navigation_instruction",
//            		"routes.legs.steps.navigation_instruction.instructions",
//            		"routes.legs.steps.static_duration",
//            		"routes.legs.steps.distance_meters",
//            		"routes.travel_advisory",
//            		"routes.travel_advisory.transit_fare.currencyCode" ,
//            		"routes.travel_advisory.transit_fare.units" ,
//            		"routes.travel_advisory.transit_fare.nanos" ,
            		"*",
            		
            };
            headers.put("X-Goog-FieldMask", StringUtils.join(fields, ","));
            

            FixedHeaderProvider headerProvider = FixedHeaderProvider.create(headers);

            // 2. 設定 Channel
            InstantiatingGrpcChannelProvider channelProvider = 
                RoutesSettings.defaultGrpcTransportProviderBuilder()
                    .setEndpoint(ENDPOINT)
                    .setHeaderProvider(headerProvider)
                    .build();

            // 3. 建立 Settings
            RoutesSettings settings = RoutesSettings.newBuilder()
                    .setTransportChannelProvider(channelProvider)
                    .setCredentialsProvider(NoCredentialsProvider.create())
                    .build();

            routesClient = RoutesClient.create(settings);
        }
        return routesClient;
    } 
    /**
     * 建立 Waypoint
     */
    private Waypoint createWaypoint(double lat, double lng) {
        return Waypoint.newBuilder()
                .setLocation(Location.newBuilder()
                        .setLatLng(LatLng.newBuilder()
                                .setLatitude(lat)
                                .setLongitude(lng)
                                .build())
                        .build())
                .build();
    }
    /**
     * 建立 Waypoint
     */
    public Waypoint createWaypoint(String addr) {
        return Waypoint.newBuilder()
        		.setAddress(addr)                
                .build();
    }
    
    public com.google.maps.routing.v2.Route chooseShortestRoute(final java.util.List<com.google.maps.routing.v2.Route> routes) {
		return routes.stream()
			.min(Comparator.comparingLong(this::computeTotalDistance))
			.orElse(routes.get(0));
	}
    protected long computeTotalDistance(com.google.maps.routing.v2.Route route) {
		long sum = 0L;
		if (route == null || route.getLegsList() == null) return sum;

		// 其實只會有一個
		for (RouteLeg leg : route.getLegsList()) {
			if (leg != null && leg.getDistanceMeters() >0 ) {
				sum += leg.getDistanceMeters();
			}
		}
		log.info("this route total distance: {} meters", sum);
		return sum;
	}
    protected void parseLog(final java.util.List<com.google.maps.routing.v2.Route> routes) {
    	log.info("=== 路線詳細資訊 (使用交通工具: %s) ===".formatted(TRAVEL_MODE));
        
        for (int i = 0; i < routes.size(); i++) {
            Route route = routes.get(i);
            log.info("第 %d 個 Route ".formatted( i + 1));
            
            List<RouteLeg> legs = route.getLegsList();
            log.info("路線描述: %s".formatted(route.getDescription()));
            Money transitFare = route.getTravelAdvisory().getTransitFare();
			if (transitFare != null) {
				
				log.info("transitFare units: %s ,currencyCode:  %s".formatted(transitFare.getUnits() ,transitFare.getCurrencyCode()));
			}
            for (RouteLeg leg : legs) {
                log.info("--- 路段 (Leg) 資訊 ---");
                
                log.info("預估距離: %.2f 公里".formatted(leg.getDistanceMeters() / 1000.0));
                log.info("預估時間: %d 秒".formatted(leg.getDuration().getSeconds()));
                log.info("起點Latitude: %f ".formatted(leg.getStartLocation().getLatLng().getLatitude()));
                log.info("起點Longitude: %f ".formatted(leg.getStartLocation().getLatLng().getLongitude()));
                
                log.info("迄點Latitude: %f ".formatted(leg.getEndLocation().getLatLng().getLatitude()));
                log.info("迄點Longitude: %f ".formatted(leg.getEndLocation().getLatLng().getLongitude()));
                // 解析每一個步驟 (Step)
                List<RouteLegStep> steps = leg.getStepsList();
                log.info("本路段共有 %d 個步驟:".formatted(steps.size()));

                for (int j = 0; j < steps.size(); j++) {
                    RouteLegStep step = steps.get(j);
                    
                    // 提取步驟距離
                    int stepDistance = step.getDistanceMeters();
                    
                    // 提取步驟時間 (注意：這裡使用 staticDuration)
                    long stepSeconds = step.hasStaticDuration() ? step.getStaticDuration().getSeconds() : 0;
                    
                    // 提取導航指令 (例如：右轉進入忠孝東路)
                    String instruction = "";
                    if (step.hasNavigationInstruction()) {
                        instruction = step.getNavigationInstruction().getInstructions();
                    }
                    
//                    String TripShortText = step.getTransitDetails().getTripShortText();
//                    log.info("  [Step %d] TripShortText: %s".formatted(j + 1,TripShortText));
                    log.info("  [Step %d] %s (距離: %d m, 時間: %d 秒)"
                        .formatted(j + 1, instruction.replace(System.lineSeparator(), "").replace("\n", "").replace("\r", ""), stepDistance, stepSeconds));
                    if (step.hasTransitDetails()) {
                    	RouteLegStepTransitDetails td = step.getTransitDetails();
//                    	System.out.println(td.getLocalizedValues().toString());
                    	parseTransitDetails(td);
                    	
                    }
                }
            }
            
            log.info("總距離: %.2f 公里".formatted(route.getDistanceMeters() / 1000.0));
            log.info("總時間: %d 分鐘".formatted(route.getDuration().getSeconds() / 60));
            log.info("==================================");
        }
    }
    protected void parseTransitDetails(RouteLegStepTransitDetails details) {
        // 1. 取得路線資訊
        TransitLine line = details.getTransitLine();
        String displayName = line.getNameShort(); // 例如 "307" 或 "BL"
        String type = line.getVehicle().getType().toString(); //如 BUS, SUBWAY
        // 2. 取得發車細節
        String depTime = details.getLocalizedValues().getDepartureTime().toString();
        String depStop = details.getStopDetails().getDepartureStop().getName();
        
        // 3. 取得行進方向 (Headsign)
        String headsign = details.getHeadsign(); // 例如 "往 台北車站"
        
        // 4. 取得總共經過幾站
        int stopCount = details.getStopCount();
        
        // 將以上資訊組合推播至 UI
        log.info("   [運具 %s %s 往%s] (發車時間: %s , 停%d站 , 沿途停靠: %s )"
                .formatted(type, displayName , headsign ,depTime , stopCount, depStop)  );
    }
    
    /**
     * 解析回應
     */
    public RouteResult parseResponse(ComputeRoutesResponse response) {
        if (response.getRoutesCount() == 0) {
            return null;
        }       
        final  List<Route> routesList = response.getRoutesList();
        parseLog(routesList);
        Route route = routesList.get(0);        
        return covertFromRoute(route);
    }
    
    private RouteResult covertFromRoute( Route route) {
    	String encodedPolyline = null;
        if (route.hasPolyline()) {
            encodedPolyline = route.getPolyline().getEncodedPolyline();
        }        
        int distanceMeters = route.getDistanceMeters();
        int durationSeconds = (int) route.getDuration().getSeconds();
        
        RouteResult result =  new RouteResult(encodedPolyline, distanceMeters, durationSeconds);
        LatLng startLatLng = route.getLegs(0).getStartLocation().getLatLng();
        LatLng endLatLng = route.getLegs(0).getEndLocation().getLatLng();
        result.setOriginLat(startLatLng.getLatitude());
        result.setOriginLng(startLatLng.getLongitude());
        result.setDestLat(endLatLng.getLatitude());
        result.setDestLng(endLatLng.getLongitude());
        return result;
    }
    /**
     * 計算路線（含途經點）
     */
    public ComputeRoutesResponse computeRoute(
    		Waypoint origin, 
    		Waypoint destination,
            List<double[]> waypoints) throws IOException {
        
        RoutesClient client = getRoutesClient();
        
        // 建立請求
        ComputeRoutesRequest.Builder requestBuilder = ComputeRoutesRequest.newBuilder()
                .setOrigin(origin)
                .setDestination(destination)
                .setOptimizeWaypointOrder(true) //INVALID_ARGUMENT: Requests with optimize_waypoint_order set to True also need to request for routes.optimized_intermediate_waypoint_index in the fieldmask.
                .setComputeAlternativeRoutes(true)//要求要有替代路線
//                .addRequestedReferenceRoutes(ReferenceRoute.SHORTER_DISTANCE) //要求範例：距離較短的路線   INVALID_ARGUMENT: A SHORTER_DISTANCE ReferenceRoute may not be requested when optimize_waypoint_order is set.      
                .setTravelMode(TRAVEL_MODE)
                .setTransitPreferences(TransitPreferences.newBuilder()
                		.addAllowedTravelModes(TransitTravelMode.TRAIN)
                		.setRoutingPreference(TransitRoutingPreference.LESS_WALKING )
                		.build()) 
//                .addRequestedReferenceRoutes(ReferenceRoute.SHORTER_DISTANCE)
                .setLanguageCode("zh-TW")
                .setUnits(Units.METRIC);
        
        // 添加途經點
        if (waypoints != null && !waypoints.isEmpty()) {
            for (double[] wp : waypoints) {
                requestBuilder.addIntermediates(createWaypoint(wp[0], wp[1]));
            }
        }
        
        ComputeRoutesRequest request = requestBuilder.build();
        ComputeRoutesResponse response = client.computeRoutes(request);
        
        return  response ;
    }
    /**
     * 生成 Static Map URL(Get Method/有資安疑慮，key顯示在get URL，僅能最為測試使用)
     */
    public String generateStaticMapUrl(
            String encodedPolyline,
            double originLat, double originLng,
            double destLat, double destLng,
            int width, int height,
            String language) {
        
        StringBuilder url = new StringBuilder(STATIC_MAP_BASE_URL);
        url.append("?size=").append(width).append("x").append(height);
        url.append("&path=color:0x0000ff|weight:5|enc:").append(encodedPolyline);
        
        // 起點標記
        url.append("&markers=color:red|label:A|")
           .append(originLat).append(",").append(originLng);
        
        // 終點標記
        url.append("&markers=color:red|label:B|")
           .append(destLat).append(",").append(destLng);
        
        // language
        url.append("&language=").append(language);
        
        // API KEY
        url.append("&key=").append(this.apikey);
        
        return url.toString();
    }
    /**
     * 生成 Static Map URL(Get Method/有資安疑慮，key顯示在get URL，僅能最為測試使用)
     */
    public URI generateStaticMapUrlV2(
            String encodedPolyline,
            double originLat, double originLng,
            double destLat, double destLng,
            int width, int height,
            String language) {
        
        StringBuilder url = new StringBuilder(STATIC_MAP_BASE_URL);
        url.append("?size=").append(width).append("x").append(height);
        url.append("&path=color:0x0000ff|weight:5|enc:").append(encodedPolyline);
        
        // 起點標記
        url.append("&markers=color:red|label:A|")
           .append(originLat).append(",").append(originLng);
        
        // 終點標記
        url.append("&markers=color:red|label:B|")
           .append(destLat).append(",").append(destLng);
        
        // language
        url.append("&language=").append(language);
        
        // API KEY
        url.append("&key=").append(this.apikey);
        
        URI targetUri = UriComponentsBuilder.fromHttpUrl(STATIC_MAP_BASE_URL)
                .queryParam("size", "%dx%d".formatted(width ,height ))
                .queryParam("path", "color:0x0000ff|weight:5|enc:"+encodedPolyline) // 自動處理特殊字元
                .queryParam("markers", "color:red|label:A|%f,%f".formatted(originLat ,originLng))
                .queryParam("markers", "color:red|label:B|%f,%f".formatted(destLat ,destLng))
                .queryParam("language", language)
                .queryParam("key", this.apikey)
                .build()
                .toUri();
        return targetUri;
    }
    /**
     * 路線結果類別
     */
    @Data
    @Builder
    public static class RouteResult {
        private final String encodedPolyline;
        private final int distanceMeters;
        private final int durationSeconds;
        private double originLat ;
        private double originLng ;
        private double destLat ;
        private double destLng ;
        
        public RouteResult(String encodedPolyline, int distanceMeters, int durationSeconds) {
            this.encodedPolyline = encodedPolyline;
            this.distanceMeters = distanceMeters;
            this.durationSeconds = durationSeconds;
        }
        
        public double getDistanceKm() {
            return distanceMeters / 1000.0;
        }
        
        public int getDurationMinutes() {
            return durationSeconds / 60;
        }
        
        @Override
        public String toString() {
            return String.format("距離: %.2f 公里, 時間: %d 分鐘", 
                getDistanceKm(), getDurationMinutes());
        }
    }
}
