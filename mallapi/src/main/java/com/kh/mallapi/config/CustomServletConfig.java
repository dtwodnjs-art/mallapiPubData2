package com.kh.mallapi.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.kh.mallapi.controller.formatter.LocalDateFormatter;

@Configuration
public class CustomServletConfig implements WebMvcConfigurer {
	@Override
	public void addFormatters(FormatterRegistry registry) {
		registry.addFormatter(new LocalDateFormatter());
	}
  
	/*@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/**")
		.allowedOrigins("*").allowedMethods("HEAD", "GET", "POST", "PUT","PATCH","DELETE", "OPTIONS")
		.maxAge(300)
		.allowedHeaders("Authorization", "Cache-Control", "Content-Type");
	}*/

}

//1. addMapping("/**") : 모든 URL 경로에 대해 CORS를 허용(/api/**, /user/**, /todo 등 전부 허용함) 
//2. allowedOrigins("*") : 모든 출처(origin)에서 오는 요청허용 (http://localhost:3000, https://mydomain.com, 등) 
//3. allowedMethods("HEAD", "GET", "POST", "PUT", "DELETE", "OPTIONS") HTTP 메서드를 허용할지 지정 (OPTIONS: 브라우저의 사전 요청, HEAD:응답헤더요청등) 
//4. maxAge(300):  브라우저가 OPTIONS 요청을 보낸 후, 300초 동안은 재요청 없이 캐시된 정보 사용 
//5. allowedHeaders("Authorization",  "Cache-Control",  "Content-Type") : 
//클라이언트에서 보낼 수 있는 요청 헤더(Authorization: JWT 등 토큰 인증용, Cache-Control : 캐시 설정 , Content-Type : 요청 
//본문의 데이터 형식 지정 (예: application/json)
