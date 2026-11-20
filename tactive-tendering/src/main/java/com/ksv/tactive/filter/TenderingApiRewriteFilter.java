package com.ksv.tactive.filter;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TenderingApiRewriteFilter extends OncePerRequestFilter {
	private final String publicPrefix;
	private final String apiPrefix;

	public TenderingApiRewriteFilter(@Value("${app.public-path:/tendering}") String publicPath) {
		this.publicPrefix = publicPath.endsWith("/") ? publicPath.substring(0, publicPath.length() - 1) : publicPath;
		this.apiPrefix = this.publicPrefix + "/tactive";
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		String contextPath = request.getContextPath() == null ? "" : request.getContextPath();
		String path = request.getRequestURI();
		if (path.startsWith(contextPath)) {
			path = path.substring(contextPath.length());
		}
		if (path.equals(apiPrefix) || path.startsWith(apiPrefix + "/")) {
			String rewritten = path.substring(publicPrefix.length());
			filterChain.doFilter(new RewrittenRequest(request, contextPath, path, rewritten), response);
			return;
		}
		filterChain.doFilter(request, response);
	}

	private static final class RewrittenRequest extends HttpServletRequestWrapper {
		private final String contextPath;
		private final String originalPath;
		private final String rewrittenPath;

		private RewrittenRequest(HttpServletRequest request, String contextPath, String originalPath, String rewrittenPath) {
			super(request);
			this.contextPath = contextPath;
			this.originalPath = originalPath;
			this.rewrittenPath = rewrittenPath;
		}

		@Override
		public String getRequestURI() {
			return contextPath + rewrittenPath;
		}

		@Override
		public String getServletPath() {
			return rewrittenPath;
		}

		@Override
		public String getPathInfo() {
			return null;
		}

		@Override
		public StringBuffer getRequestURL() {
			StringBuffer url = super.getRequestURL();
			int index = url.indexOf(originalPath);
			if (index >= 0) {
				return new StringBuffer(url.substring(0, index)).append(rewrittenPath);
			}
			return url;
		}
	}
}
