package com.example.route.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;
 
/**
 * -Dfile.encoding=UTF-8
 * */
@Component
@ConfigurationProperties(prefix = "google.api"  , ignoreInvalidFields = true)
@Data
public class GoogleApiProperties {
	private List<String> keys; 
}
