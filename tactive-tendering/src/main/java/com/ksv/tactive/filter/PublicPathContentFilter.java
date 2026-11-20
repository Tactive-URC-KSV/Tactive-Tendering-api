package com.ksv.tactive.filter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class PublicPathContentFilter extends OncePerRequestFilter {
	private static final String PACKAGED_PREFIX = "/tendering/";
	private final String publicPath;

	public PublicPathContentFilter(@Value("${app.public-path:/tendering}") String publicPath) {
		this.publicPath = publicPath.endsWith("/") ? publicPath.substring(0, publicPath.length() - 1) : publicPath;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, jakarta.servlet.FilterChain filterChain)
			throws jakarta.servlet.ServletException, IOException {
		if ("/tendering".equals(publicPath) || !isTextAsset(requestPath(request))) {
			filterChain.doFilter(request, response);
			return;
		}
		CapturingResponse capture = new CapturingResponse(response);
		filterChain.doFilter(request, capture);
		byte[] body = capture.body();
		String rewritten = new String(body, StandardCharsets.UTF_8).replace(PACKAGED_PREFIX, publicPath + "/");
		byte[] output = rewritten.getBytes(StandardCharsets.UTF_8);
		response.setContentLength(output.length);
		response.getOutputStream().write(output);
	}

	private static String requestPath(HttpServletRequest request) {
		String contextPath = request.getContextPath() == null ? "" : request.getContextPath();
		String path = request.getRequestURI();
		if (path.startsWith(contextPath)) {
			path = path.substring(contextPath.length());
		}
		return path;
	}

	private static boolean isTextAsset(String path) {
		String lower = path.toLowerCase();
		return lower.endsWith(".html") || lower.endsWith(".js") || lower.endsWith(".css");
	}

	private static final class CapturingResponse extends HttpServletResponseWrapper {
		private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();

		private CapturingResponse(HttpServletResponse response) {
			super(response);
		}

		@Override
		public ServletOutputStream getOutputStream() {
			return new ServletOutputStream() {
				@Override
				public boolean isReady() {
					return true;
				}

				@Override
				public void setWriteListener(WriteListener writeListener) {
				}

				@Override
				public void write(int b) {
					buffer.write(b);
				}
			};
		}

		@Override
		public void setContentLength(int len) {
		}

		@Override
		public void setContentLengthLong(long len) {
		}

		private byte[] body() {
			return buffer.toByteArray();
		}
	}
}
