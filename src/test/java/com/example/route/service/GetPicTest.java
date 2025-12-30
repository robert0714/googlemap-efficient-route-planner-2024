package com.example.route.service;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@SpringBootTest
class GetPicTest {
	@Autowired
	private GetPic getPic ;
	String url = "https://maps.googleapis.com/maps/api/staticmap?size=800x600&language=zh-TW&path=color%3A0x0000ff%7Cweight%3A5%7C25.04765%2C121.51331%7C25.05467%2C121.49603%7C25.049950000000003%2C121.49126000000001%7C25.04707%2C121.47956%7C25.039270000000002%2C121.46356000000002%7C25.028910000000003%2C121.4475%7C25.018400000000003%2C121.43944%7C24.99988%2C121.43299%7C24.992070000000002%2C121.42553000000001&markers=color:red|label:A|%E5%8F%B0%E5%8C%97%E7%81%AB%E8%BB%8A%E7%AB%99&markers=color:red|label:B|%E6%A8%B9%E6%9E%97%E7%81%AB%E8%BB%8A%E7%AB%99&key=AIzaSyCz5mP_fkHn_A2_o7DkODD607AFqu_H0Ls";
	
	
	@Test
	@Disabled
	public void testGetBase64FromPicUrl() {
//		 String test= getPic.getBase64FromPicUrl("https://maps.googleapis.com/maps/api/staticmap?center=New+York,NY&zoom=13&size=600x300&maptype=roadmap&key=AIzaSyCz5mP_fkHn_A2_o7DkODD607AFqu_H0Ls");
		String test= getPic.getBase64FromPicUrl(url);
		 System.out.println(test);
	}
	@Test
	public void testFetchStaticMap() {
			 
		 Mono<String> test= getPic.fetchStaticMap(url);
		 System.out.println(test.block(Duration.ofMinutes(2L)));
		 
	}

}
