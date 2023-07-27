package net.nextome.lismove.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

public class ExceptionFilter extends OncePerRequestFilter {

	private final Logger log = LoggerFactory.getLogger(getClass());

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		try {
			filterChain.doFilter(request, response);
		} catch(NextomeSecurityException e) {
			log.error("Spring Security Filter Chain Exception: {}", e.getMessage());
			PrintWriter out = response.getWriter();
			response.setContentType("application/json");
			response.setCharacterEncoding("UTF-8");
			response.setStatus(e.getHttpStatusCode());
			out.print(e.toJson());
			out.flush();
		}
	}
}
