package net.nextome.lismove.security;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import net.nextome.lismove.config.SecurityServiceProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class FirebaseTokenFilter extends OncePerRequestFilter {

	private final SecurityServiceProperties serviceProperties;
	private final NextomeUserDetailsService userDetailsService;
	private final Logger logger = LoggerFactory.getLogger(getClass());

	public FirebaseTokenFilter(SecurityServiceProperties serviceProperties, NextomeUserDetailsService userDetailsService) {
		this.serviceProperties = serviceProperties;
		this.userDetailsService = userDetailsService;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse httpServletResponse, FilterChain filterChain) throws ServletException, IOException {
		String token = request.getHeader(serviceProperties.getJwt_header());

		if(token == null) {
			if(serviceProperties.getMockuser() == null) {
				throw new NextomeSecurityException("Missing Token", HttpStatus.UNAUTHORIZED);
			}
			UserDetails userDetails = userDetailsService.loadUserByUsername(serviceProperties.getMockuser());
			Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
			SecurityContextHolder.getContext().setAuthentication(auth);
			userDetailsService.registerLogin(serviceProperties.getMockuser(), request.getHeader("app-version"), request.getHeader("app-os"));
			logger.info("App: {}({}) - {} {}?{} - {}", request.getHeader("app-os"), request.getHeader("app-version"), request.getMethod(), request.getRequestURI(), request.getQueryString(), serviceProperties.getMockuser());
		} else {
			try {
				FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(token.replace(serviceProperties.getTokenPrefix(), ""));
				UserDetails userDetails = userDetailsService.loadUserByUsername(decodedToken.getUid());
				Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
				SecurityContextHolder.getContext().setAuthentication(auth);
				try {
					userDetailsService.registerLogin(decodedToken.getUid(), request.getHeader("app-version"), request.getHeader("app-os"));
				} catch(Exception e) {
					logger.error(e.getMessage());
				}
				logger.info("App: {}({}) - {} {}?{} - {}", request.getHeader("app-os"), request.getHeader("app-version"), request.getMethod(), request.getRequestURI(), request.getQueryString(), decodedToken.getUid());
			} catch(FirebaseAuthException e) {
				logger.error("App: {}({}) - {} {}?{} - {}", request.getHeader("app-os"), request.getHeader("app-version"), request.getMethod(), request.getRequestURI(), request.getQueryString(), token);
				logger.error("Token Invalid: {}", e.getMessage());
				throw new NextomeSecurityException(e.getMessage(), HttpStatus.UNAUTHORIZED);
			}
		}

		filterChain.doFilter(request, httpServletResponse);
	}

}
