package com.example.route.service;

import java.net.URI; 
import java.time.Duration;
import java.util.Base64;

import org.springframework.http.HttpHeaders; 
import org.springframework.stereotype.Component; 
import org.springframework.web.reactive.function.client.WebClient;
 

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Component
public class GetPic {
	private final static String BASE64_IMG_PREFIX = "data:image/png;base64,";

	private WebClient webClient;

	@PostConstruct
	public void postConstruct() {
		this.webClient = WebClient.builder().baseUrl("https://maps.googleapis.com/maps/api/staticmap").build();
	}

	public String getBase64FromPicUrl(String url) {
		Mono<String> mono = fetchStaticMap(url);
		String screenshotBase64 = mono.block(Duration.ofMinutes(2L));
		return new StringBuffer(BASE64_IMG_PREFIX).append(screenshotBase64).toString();
	}

	protected Mono<String> fetchStaticMap(String url) {
		url = url.replace("https://maps.googleapis.com/maps/api/staticmap?", "?").replace("|", "%7C") // 修正管道符号
				.replace(":", "%3A"); // 修正冒号
		url = "https://maps.googleapis.com/maps/api/staticmap" + url;

		return webClient.get().uri(URI.create(url)) // 使用完整 URL
				.header(HttpHeaders.CONTENT_TYPE, "application/json").retrieve().bodyToMono(byte[].class)
				.map(bytes -> Base64.getEncoder().encodeToString(bytes));
	}
}
