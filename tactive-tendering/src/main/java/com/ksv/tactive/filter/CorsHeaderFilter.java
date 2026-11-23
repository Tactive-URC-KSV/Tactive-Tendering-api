package com.ksv.tactive.filter;

import java.io.IOException;
import java.util.List;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorsHeaderFilter extends OncePerRequestFilter {
	private final CorsConfigurationSource corsConfigurationSource;

	public CorsHeaderFilter(CorsConfigurationSource corsConfigurationSource) {
		this.corsConfigurationSource = corsConfigurationSource;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		CorsConfiguration configuration = corsConfigurationSource.getCorsConfiguration(request);
		String allowedOrigin = allowedOrigin(configuration, request.getHeader(HttpHeaders.ORIGIN));
		if (allowedOrigin != null) {
			response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, allowedOrigin);
			response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
			response.addHeader(HttpHeaders.VARY, HttpHeaders.ORIGIN);
			if (HttpMethod.OPTIONS.matches(request.getMethod())) {
				response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, join(configuration.getAllowedMethods()));
				String requestedHeaders = request.getHeader(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS);
				response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS,
						requestedHeaders == null || requestedHeaders.isBlank() ? "Content-Type,Authorization" : requestedHeaders);
				response.setHeader(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, "Authorization");
				response.setHeader(HttpHeaders.ACCESS_CONTROL_MAX_AGE, "3600");
				response.setStatus(HttpServletResponse.SC_OK);
				return;
			}
		}
		filterChain.doFilter(request, response);
	}

	private static String allowedOrigin(CorsConfiguration configuration, String origin) {
		if (configuration == null || origin == null || origin.isBlank()) {
			return null;
		}
		return configuration.checkOrigin(origin);
	}

	private static String join(List<String> values) {
		if (values == null || values.isEmpty()) {
			return "GET,POST,PUT,PATCH,DELETE,OPTIONS";
		}
		return String.join(",", values);
	}
}
