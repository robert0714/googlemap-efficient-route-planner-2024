package com.example.route.service.impl.routing_v2;

import com.google.type.LatLng; 
import com.google.api.gax.rpc.FixedHeaderProvider;
import com.google.api.gax.rpc.ServerStream;
import com.google.auto.value.AutoValue.Builder;
import com.google.maps.routing.v2.*;
import com.google.maps.routing.v2.RouteMatrixElement.LocalizedValues;
import com.google.rpc.Status;
import com.google.api.gax.core.NoCredentialsProvider;
import com.google.api.gax.grpc.InstantiatingGrpcChannelProvider;
import java.io.IOException;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils; 

import java.util.ArrayList;
import java.util.HashMap;
 
 
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ComputeRouteMatrixClient implements AutoCloseable{ 
	private final String apikey ;
	private static final String ENDPOINT = "routes.googleapis.com:443";
    private static final RouteTravelMode TRAVEL_MODE= RouteTravelMode.TRANSIT;
    private RoutesClient routesClient;
    
    public ComputeRouteMatrixClient(String apikey) {
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
            headers.put("X-Goog-Api-Key", apikey);
            
            // 關鍵：FieldMask 決定了 Response 哪些欄位會有資料，沒設的話會報錯
            String [] fields = {
//            		"originIndex",
//            		"destinationIndex",
//            		"duration",
//            		"distanceMeters",
//            		"status",
//            		"condition",
            		
            		"*"
//            		"routes.legs",
//            		"routes.legs.distanceMeters",
//            		"routes.legs.steps.duration",
            		
            		
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
     * 計算路線（含途經點）
     */
    public List<RouteResult >  computeRoute(
            double originLat, double originLng,
            double destLat, double destLng,
            List<double[]> waypoints) throws IOException {       
        
        // 建立起點和終點
        Waypoint origin = createWaypoint(originLat, originLng);
        Waypoint destination = createWaypoint(destLat, destLng);
             
		return computeRoute(origin, destination);
    }
    /**
     * 計算路線（含途經點）
     */
    public List<RouteResult > computeRoute(
    		Waypoint origin, 
    		Waypoint destination) throws IOException {
        
        RoutesClient client = getRoutesClient();
       
        // 建立請求
        ComputeRouteMatrixRequest.Builder requestBuilder =  ComputeRouteMatrixRequest.newBuilder()
                .addAllOrigins(List.of( RouteMatrixOrigin.newBuilder()
                		.setWaypoint(origin)
                		.setRouteModifiers(RouteModifiers.newBuilder()
                				.setVehicleInfo(VehicleInfo.newBuilder()
                						.setEmissionType(VehicleEmissionType.GASOLINE))
                				.addTollPasses(TollPass.US_MA_EZPASSMA)
                				.addTollPasses(TollPass.US_WA_GOOD_TO_GO)
                				)
                		.build()))
                .addAllDestinations(List.of( RouteMatrixDestination.newBuilder().setWaypoint(destination).build()))
                .setTravelMode(TRAVEL_MODE)                
//                .setRoutingPreference(RoutingPreference.TRAFFIC_AWARE_OPTIMAL)//Routing preference cannot be set for TRANSIT travel mode
                .setTransitPreferences(TransitPreferences.getDefaultInstance())                
                .setLanguageCode("zh-TW")
                .setRegionCode("zh-TW")                
                .setUnits(Units.METRIC);
        
        
        
        ServerStream<RouteMatrixElement> stream =
        		client.computeRouteMatrixCallable().call(requestBuilder.build());
        
        List<RouteResult > list = new ArrayList<RouteResult > ();
        for (RouteMatrixElement response : stream) {
        	//  https://developers.google.com/maps/documentation/routes?hl=zh-tw
        	list.add( parseResponse(response));
          }
       
        return list;
    }
    /**
     * 解析回應
     */
    private RouteResult parseResponse(RouteMatrixElement response) {
    	final RouteMatrixElementCondition condition = response.getCondition();
    	Status status = response.getStatus();
    	 
    	LocalizedValues localizedValues = response.getLocalizedValues();    	
        return new RouteResult(status , condition ,localizedValues );
    }
    
    /**
     * 建立 Waypoint
     */
    protected Waypoint createWaypoint(double lat, double lng) {
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
    protected Waypoint createWaypoint(String addr) {
        return Waypoint.newBuilder()
        		.setAddress(addr)   
        		
                .build();
    }
    
    
    /**
     * 路線結果類別
     */
    @Builder
    public static class RouteResult {
    	private final Status status ;
    	private final RouteMatrixElementCondition condition;
        private String distance ;
        private String duration ;
        private String staticDuration ;
        private String transitFare;
        
        
        public RouteResult(Status status ,RouteMatrixElementCondition condition ,LocalizedValues localizedValues) {
            this.status = status;
        	this.condition = condition ;
            this.distance = localizedValues.getDistance().getText();
            this.duration = localizedValues.getDuration().getText();
            this.staticDuration = localizedValues.getStaticDuration().getText();
            this.transitFare = localizedValues.getTransitFare().getText();
        }
        
        @Override
        public String toString() {
        	if(StringUtils.isNoneEmpty( this.transitFare)) {
        		 return String.format(" %s , 大眾交通運輸費用: %s , 距離: %s , 時間: %s ", 
							this.status.getMessage(), this.transitFare, this.distance, this.duration);
        	}
            return String.format(" %s , 距離: %s , 時間: %s", 
            		this.status.getMessage(), distance , duration);
        }
    }
}
