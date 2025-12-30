package com.example.route.service.impl.routing_v2;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.example.route.config.GoogleApiProperties;
import com.example.route.service.impl.routing_v2.ComputeRouteMatrixClient.RouteResult; 
import com.google.maps.routing.v2.Waypoint;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest
public class ComputeRouteMatrixClientTest {
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
	public void computeRoute() throws Exception {
		// Enable it by  https://console.developers.google.com/apis/api/routes.googleapis.com/overview?project=376614899184
		try (ComputeRouteMatrixClient client = new ComputeRouteMatrixClient(apikey)) {            
	         // 建立起點和終點
	            Waypoint origin = client.createWaypoint("樹林火車站");
	            Waypoint destination = client.createWaypoint("台北火車站");            
	            System.out.println("計算路線中...");
	            
	           List<RouteResult> list = client.computeRoute(origin, destination );
	           System.out.println(list.size());
	           list.stream().forEach(unit-> System.out.println(unit.toString()));
	        }
	}

}
