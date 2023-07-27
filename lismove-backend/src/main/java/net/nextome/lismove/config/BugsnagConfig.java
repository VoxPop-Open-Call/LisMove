package net.nextome.lismove.config;

import com.bugsnag.Bugsnag;
import com.bugsnag.BugsnagSpringConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(BugsnagSpringConfiguration.class)
public class BugsnagConfig {

	@Value("${spring.profiles.active}")
	private String env;
	@Autowired
	BuildProperties buildProperties;

	@Bean
	public Bugsnag bugsnag() {
		Bugsnag app = new Bugsnag("");
		app.setIgnoreClasses("org.springframework.web.HttpRequestMethodNotSupportedException", "org.springframework.web.bind.MissingServletRequestParameterException");
		app.setAppVersion(buildProperties.getVersion());
		app.setReleaseStage(env.equals("prod") ? "production" : "development");
		return app;
	}
}
