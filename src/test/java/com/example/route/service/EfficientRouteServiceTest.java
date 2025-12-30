package com.example.route.service;

import static org.junit.jupiter.api.Assertions.*;
 
import java.util.concurrent.ExecutionException; 
import java.util.concurrent.TimeoutException;
 
import lombok.extern.slf4j.Slf4j;

import org.junit.jupiter.api.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest; 

import com.example.route.model.RouteResult;

@Slf4j
@SpringBootTest
class EfficientRouteServiceTest {

	@Autowired
	private EfficientRouteService service;

	@Test
	public void testCalculateRoute() throws InterruptedException, ExecutionException, TimeoutException {
		  RouteResult data = service.calculateRoute("台北火車站", "樹林火車站"); 
		 long distanceInMeters = data.getDistanceInMeters();
		 String mapUrl = data.getMapUrl();
		 String formattedDuration = data.getFormattedDuration();
		 String formattedDistance = data. getFormattedDistance();
		 String base64MapPic = data. getMapScreenshotBase64();
		 assertNotNull(distanceInMeters); 
		 assertNotNull(mapUrl); 
		 assertNotNull(formattedDuration); 
		 assertNotNull(base64MapPic); 
		 log.info(mapUrl);
		 log.info("distanceInMeters: {}" , distanceInMeters);
		 log.info("formattedDistance: {}" , formattedDistance);
		 log.info("formattedDuration: {}" , formattedDuration);
		 log.info("formattedDuration: {}" , formattedDuration);
		 log.info("base64MapPic: {}" , base64MapPic);
	}

}
