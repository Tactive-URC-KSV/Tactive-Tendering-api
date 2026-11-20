package com.ksv.tactive.controller;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpaController {
	private static final String PACKAGED_PREFIX = "/tendering/";
	private final String publicPath;

	public SpaController(@Value("${app.public-path:/tendering}") String publicPath) {
		this.publicPath = publicPath.endsWith("/") ? publicPath.substring(0, publicPath.length() - 1) : publicPath;
	}

	@GetMapping({
			"${app.public-path:/tendering}/contractor",
			"${app.public-path:/tendering}/contractor/",
			"${app.public-path:/tendering}/contractor/{path:^(?!assets$)[^.]+}",
			"${app.public-path:/tendering}/contractor/{path:^(?!assets$)[^.]+}/**"
	})
	public void contractor(HttpServletResponse response) throws IOException {
		writePage(response, "static/tendering/contractor/index.html");
	}

	@GetMapping({
			"${app.public-path:/tendering}",
			"${app.public-path:/tendering}/",
			"${app.public-path:/tendering}/{path:^(?!tactive$)(?!contractor$)(?!assets$)[^.]+}",
			"${app.public-path:/tendering}/{path:^(?!tactive$)(?!contractor$)(?!assets$)[^.]+}/**"
	})
	public void tender(HttpServletResponse response) throws IOException {
		writePage(response, "static/tendering/index.html");
	}

	private void writePage(HttpServletResponse response, String classpathLocation) throws IOException {
		ClassPathResource page = new ClassPathResource(classpathLocation);
		String html = StreamUtils.copyToString(page.getInputStream(), StandardCharsets.UTF_8);
		if (!"/tendering".equals(publicPath)) {
			html = html.replace(PACKAGED_PREFIX, publicPath + "/");
		}
		response.setContentType("text/html;charset=UTF-8");
		response.getWriter().write(html);
	}
}
