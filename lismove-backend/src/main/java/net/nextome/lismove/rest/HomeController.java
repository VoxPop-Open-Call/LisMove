package net.nextome.lismove.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.info.BuildProperties;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class HomeController {


	@Value("${spring.profiles.active}")
	private String env;
	@Autowired
	private BuildProperties buildProperties;

	@GetMapping({"", "keepalive"})
	public String keepAlive() {
		return "Server up - " + buildProperties.getVersion() + " " + env;
	}

}
