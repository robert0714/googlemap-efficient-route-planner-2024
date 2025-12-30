package com.example.route.service.impl.routing_v2;


import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.example.route.config.GoogleApiProperties;
import com.google.maps.routing.v2.ComputeRoutesResponse;
import com.google.maps.routing.v2.Route;
import com.google.maps.routing.v2.RouteLeg;
import com.google.maps.routing.v2.RouteLegStep;
import com.google.maps.routing.v2.Waypoint;

import lombok.extern.slf4j.Slf4j; 

@Slf4j
@SpringBootTest
class ComputeRoutesClientTest {
	private  String apikey ;
	
	@Autowired
    private  GoogleApiProperties googleApiProperties;
	@BeforeEach
	protected void setUp() throws Exception {
		apikey = googleApiProperties.getKeys().get(0);
	}

	@AfterEach
	protected void tearDown() throws Exception {
	}

	@Test
	public void computeRoute() {
		// Enable it by  https://console.developers.google.com/apis/api/routes.googleapis.com/overview?project=376614899184
		try (final ComputeRoutesClient client = new ComputeRoutesClient(apikey)) {            
	         // 建立起點和終點
	            Waypoint origin = client.createWaypoint("基隆市中正區新豐街293號");
	            Waypoint destination = client.createWaypoint("新北市板橋區府中路29-2號");            
	            System.out.println("計算路線中...");
	            ComputeRoutesResponse  response = client.computeRoute(origin, destination ,null);
	            ComputeRoutesClient.RouteResult result = client.parseResponse(response);
	            
	            if (result != null) {
	            	System.out.println("----------------"); 
	                System.out.println("result: %s".formatted(result));	                
	            	System.out.println("----------------"); 
	                double originLat = result.getOriginLat();
		            double originLng = result.getOriginLng();
		            double destLat = result.getDestLat();
		            double destLng = result.getDestLng();
		             
		            // （改為最短總距離）
		            Route shortestRoute = client.chooseShortestRoute(response.getRoutesList());
		            String summary =   shortestRoute.getDescription();
		            System.out.println("\nsummary: %s".formatted(summary)); 
		            
		            // 計算總距離
					long totalDistance = 0;
					for (RouteLeg leg : shortestRoute.getLegsList()) {
						totalDistance += leg.getDistanceMeters();
					}
					// 獲取預估時間
					long durationInSeconds = shortestRoute.getDuration().getSeconds();
					
					System.out.println("\n自己算的 總距離: %d".formatted(totalDistance));
					System.out.println("\n自己算的 總時間: %d".formatted(durationInSeconds));
					// 構建詳細的路線說明
					List<String> navigationSteps = new ArrayList<>();
					for (RouteLeg leg : shortestRoute.getLegsList()) {
						List<RouteLegStep> steps = leg.getStepsList();
						for (RouteLegStep step : steps) {
							navigationSteps.add(step.getNavigationInstruction().getInstructions());
						}
					}
					
	                // 生成地圖
	                String mapUrl = client.generateStaticMapUrl(
	                    result.getEncodedPolyline(),
	                    originLat, originLng,
	                    destLat,  destLng,
	                    600, 400 ,"zh-TW"
	                );
	                
	                System.out.println("\n地圖 URL:");
	                System.out.println(mapUrl);
	                 
	                
	            } else {
	                System.out.println("無法計算路線");
	            }
	            
	        } catch (Exception e) {
	            System.err.println("錯誤: " + e.getMessage());
	            e.printStackTrace();
	        }
	}

}

