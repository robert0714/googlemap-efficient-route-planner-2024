package com.example.route.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Schema(description = "距離回應資料")
public class RouteResult {
    private long distanceInMeters;
    
    @JsonIgnore
    private String mapUrl;
    
    /**
     * Image data in Base64 format / Base64格式的影像資料
     */ 
    @Schema(description = "Image data in Base64 format / Base64格式的影像資料" , example = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAA4QAAAEnCAIAAAB+......(ommitted)" )
    @JsonProperty("map_screenshot_base64")
    private String mapScreenshotBase64;
    private List<String> navigationSteps;
    private long durationInSeconds;
    private String routeSummary;

    public RouteResult(long distanceInMeters, String mapUrl,String base64MapPic , List<String> navigationSteps, 
                      long durationInSeconds, String routeSummary) {
        this.distanceInMeters = distanceInMeters;
        this.mapUrl = mapUrl;
        this.mapScreenshotBase64 =  base64MapPic ;
        this.navigationSteps = navigationSteps;
        this.durationInSeconds = durationInSeconds;
        this.routeSummary = routeSummary;
    }

    // 增加一個便利的方法來獲取格式化的距離
    /**
     * distance / 距離數值
     */ 
    @Schema(description = "distance / 距離", example = "51.0")
    public String getDistance() {
    	 if (distanceInMeters < 1000) {
             return distanceInMeters + "";
         } else {
             return String.format("%.1f", distanceInMeters / 1000.0);
         }
    }
    /**
     * unit / 單位
     */ 
    @Schema(description = "unit / 單位", example = "公里")
    public String getUnit() {
    	if (distanceInMeters < 1000) {
            return "公尺";
        } else {
            return "公里";
        }
    }

    // 增加一個便利的方法來獲取格式化的距離
    public String getFormattedDistance() {
        if (distanceInMeters < 1000) {
            return distanceInMeters + "公尺";
        } else {
            return String.format("%.1f公里", distanceInMeters / 1000.0);
        }
    }

    // 增加一個便利的方法來獲取格式化的時間
    public String getFormattedDuration() {
        long hours = durationInSeconds / 3600;
        long minutes = (durationInSeconds % 3600) / 60;
        
        if (hours > 0) {
            return String.format("%d小時%d分鐘", hours, minutes);
        } else {
            return String.format("%d分鐘", minutes);
        }
    }
}