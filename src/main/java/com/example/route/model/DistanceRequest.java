package com.example.route.model;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@Schema(description = "規劃路徑")
public class DistanceRequest {
	/**
     * Starting geographical address / 起始地理位址
     */ 
    @Schema(description = "Starting geographical address / 起始地理位址" , example = "基隆市中正區新豐街293號")
    @NotNull(message = "startAddress is required")
	private String startAddress;
    
    /**
     * Starting geographical address / 起始地理位址
     */ 
    @Schema(description = "target geographic address / 到達地理位址" , example = "新北市板橋區府中路29-2號")
    @NotNull(message = "endAddress is required")
	private String endAddress;
    
    
    /**
     * Stops / 停靠點
     */ 
    @Schema(description = "stops / 停靠點" ,type = "array", example = "[\"100台北市中正區重慶南路一段122號\", \"220新北市板橋區縣民大道二段7號\", \"新北市樹林區樹北里鎮前街112號\"]")
	private List<String> stops;
}
