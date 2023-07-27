package net.nextome.lismove.config;

import net.nextome.lismove.security.ExceptionFilter;
import net.nextome.lismove.security.FirebaseTokenFilter;
import net.nextome.lismove.security.NextomeUserDetailsService;
import net.nextome.lismove.security.NoRedirectStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import static org.springframework.http.HttpStatus.FORBIDDEN;

//FilterChain
//https://docs.spring.io/spring-security/site/docs/current/reference/html5/#ns-custom-filters@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

	@Autowired
	private SecurityServiceProperties serviceProperties;
	@Autowired
	private NextomeUserDetailsService userDetailsService;

	private static final RequestMatcher PUBLIC_URLS = new OrRequestMatcher(
			new AntPathRequestMatcher("/public/**"),
			new AntPathRequestMatcher("/keepalive", HttpMethod.GET.toString(), true),
			new AntPathRequestMatcher("/", HttpMethod.GET.toString()),
			new AntPathRequestMatcher("/webjars/**"),
			new AntPathRequestMatcher("/swagger-ui.html"),
			new AntPathRequestMatcher("/swagger-resources/**"),
			new AntPathRequestMatcher("/v2/api-docs"),
			new AntPathRequestMatcher("/error"),
			new AntPathRequestMatcher("/resources/**"),
			new AntPathRequestMatcher("/users/**/exists"),
			new AntPathRequestMatcher("/users", HttpMethod.POST.name()),
			new AntPathRequestMatcher("/vendors", HttpMethod.POST.name()),
			new AntPathRequestMatcher("/revolut/token"),
			new AntPathRequestMatcher("/migrations/**")
	);

	@Bean
	AuthenticationEntryPoint forbiddenEntryPoint() {
		return new HttpStatusEntryPoint(FORBIDDEN);
	}

	@Bean
	SimpleUrlAuthenticationSuccessHandler successHandler() {
		SimpleUrlAuthenticationSuccessHandler successHandler = new SimpleUrlAuthenticationSuccessHandler();
		successHandler.setRedirectStrategy(new NoRedirectStrategy());
		return successHandler;
	}

	@Bean
	SimpleUrlAuthenticationFailureHandler failureHandler() {
		SimpleUrlAuthenticationFailureHandler handler = new SimpleUrlAuthenticationFailureHandler();
		handler.setRedirectStrategy(new NoRedirectStrategy());
		return handler;
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		// Disable CSRF (cross site request forgery)
		http.csrf().disable();
		// No session will be created or used by spring security
		http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
		// Entry points
		http.authorizeRequests()
				.requestMatchers(PUBLIC_URLS).permitAll()
				.anyRequest().authenticated();

		http.addFilterBefore(new ExceptionFilter(), UsernamePasswordAuthenticationFilter.class);
		http.addFilterBefore(new FirebaseTokenFilter(serviceProperties, userDetailsService), UsernamePasswordAuthenticationFilter.class);
	}

	@Override
	public void configure(WebSecurity web) {
		web.ignoring().requestMatchers(PUBLIC_URLS);
	}

	@Override
	@Bean
	public AuthenticationManager authenticationManagerBean() throws Exception {
		return super.authenticationManagerBean();
	}

}
