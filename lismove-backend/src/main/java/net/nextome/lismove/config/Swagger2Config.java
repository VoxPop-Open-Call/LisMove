package net.nextome.lismove.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import springfox.documentation.annotations.ApiIgnore;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.*;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.List;

@Configuration
@EnableSwagger2
public class Swagger2Config implements WebMvcConfigurer {

	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/**").allowedMethods("GET", "PUT", "POST", "OPTIONS", "DELETE");
	}
	

	@Bean
	public Docket api() {

		return new Docket(DocumentationType.SWAGGER_2)
				.ignoredParameterTypes(ApiIgnore.class)
				.securityContexts(Collections.singletonList(securityContext()))
				.securitySchemes(Collections.singletonList(apiKey()))
				.select()
				.apis(RequestHandlerSelectors.basePackage("net.nextome.lismove.rest"))
				.paths(PathSelectors.any())
				.build()
				.pathMapping("/")
				.directModelSubstitute(Timestamp.class, Long.class)
				.apiInfo(this.metaData());
	}

	private ApiInfo metaData() {
		return new ApiInfoBuilder()
				.title("Lis Move Server API")
				.description("\"Lis Move WEB Server API\"")
				.version("1.0.0")
				.license("Apache License Version 2.0")
				.licenseUrl("https://www.apache.org/licenses/LICENSE-2.0\"")
				.contact(new Contact("Lis Move", "https://www.lismove.it/", ""))
				.build();
	}

	private ApiKey apiKey() {
		return new ApiKey("JWT", "Authorization", "header");
	}

	private SecurityContext securityContext() {
		return SecurityContext.builder().securityReferences(defaultAuth()).build();
	}

	private List<SecurityReference> defaultAuth() {
		AuthorizationScope authorizationScope = new AuthorizationScope("global", "accessEverything");
		AuthorizationScope[] authorizationScopes = new AuthorizationScope[1];
		authorizationScopes[0] = authorizationScope;
		return Collections.singletonList(new SecurityReference("JWT", authorizationScopes));
	}
}