package com.ksv.tactive.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class PublicPathResourceConfig implements WebMvcConfigurer {
	private final String publicPath;

	public PublicPathResourceConfig(@Value("${app.public-path:/tendering}") String publicPath) {
		this.publicPath = publicPath.endsWith("/") ? publicPath.substring(0, publicPath.length() - 1) : publicPath;
	}

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		if ("/tendering".equals(publicPath)) {
			return;
		}
		registry.addResourceHandler(publicPath + "/**")
				.addResourceLocations("classpath:/static/tendering/");
	}
}
