package com.example.route.model;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Schema(description = "距離錯誤回應資料")
public class ErrorResponse {
	@Schema(description = "eroor message / 錯誤訊息" , example = "可能的錯誤訊息有很多種" )
	private String error;
}