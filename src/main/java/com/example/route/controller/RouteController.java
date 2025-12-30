package com.example.route.controller;

import com.example.route.model.DistanceRequest;
import com.example.route.model.ErrorResponse;
import com.example.route.model.RouteResult;
import com.example.route.service.EfficientRouteService;
 
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Tag(name = "DistanceController", description = "最佳距離計算與截圖")
@Validated
@RestController
public class RouteController {
    @Autowired
    private EfficientRouteService routeService;

    @PostMapping("/distance")
    @ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "successful operation", content = @Content(array = @ArraySchema(schema = @Schema(implementation = RouteResult.class)))),
			@ApiResponse(responseCode = "500", description = "Invalid status value", content = @Content(array = @ArraySchema(schema = @Schema(implementation = ErrorResponse.class)))) })
    public ResponseEntity<?> calculateRoute(
    		@RequestBody DistanceRequest data) {
		String origin = data.getStartAddress();
		String destination = data.getEndAddress();
		try {
			return ResponseEntity.ok(routeService.calculateRoute(origin, destination));
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return ResponseEntity.status(500).body(new ErrorResponse("%s ->  %s  , failure to calculate route".formatted(origin , destination)));
		}
    }
    
    @GetMapping("/calculate-route")
    public  ResponseEntity<?> calculateRoute(
        @RequestParam String origin,
        @RequestParam String destination
    ) {
		try {
			return ResponseEntity.ok(routeService.calculateRoute(origin, destination));
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return ResponseEntity.status(500).body(new ErrorResponse("%s ->  %s  , failure to calculate route".formatted(origin , destination)));
		}
    }
}