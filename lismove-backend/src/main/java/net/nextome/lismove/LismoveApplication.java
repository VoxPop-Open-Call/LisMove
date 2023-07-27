package net.nextome.lismove;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.maps.GeoApiContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.InputStream;

@SpringBootApplication(scanBasePackages = {"net.nextome.lismove", "net.nextome.modules"})
@EnableJpaAuditing
@EnableScheduling
@EnableAsync
public class LismoveApplication extends SpringBootServletInitializer {
	@Value("${firebase.config}")
	private String firebaseConfig;
	@Value("${firebase.storage}")
	private String firebaseStorage;
	@Value("${google.api-key}")
	private String googleApiKey;

	@Bean
	public FirebaseApp firebaseApp() {
		try {
			InputStream config = new ClassPathResource(firebaseConfig).getInputStream();

			FirebaseOptions options = FirebaseOptions.builder()
					.setCredentials(GoogleCredentials.fromStream(config))
					.setStorageBucket(firebaseStorage)
					.build();
			if(FirebaseApp.getApps().isEmpty()) {
				return FirebaseApp.initializeApp(options);
			}
			return null;
		} catch(java.io.IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Bean
	public GeoApiContext geoApiContext() {
		return new GeoApiContext.Builder()
				.apiKey(googleApiKey)
				.build();
	}

	public static void main(String[] args) {
		SpringApplication.run(LismoveApplication.class, args);
	}

}
