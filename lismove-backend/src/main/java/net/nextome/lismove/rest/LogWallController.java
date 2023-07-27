package net.nextome.lismove.rest;

import net.nextome.lismove.services.LogWallService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("logwall")
public class LogWallController {

	@Autowired
	private LogWallService logWallService;

	@GetMapping
	public List<String> getLogWall() {
		return logWallService.generateLogWall();
	}
}
