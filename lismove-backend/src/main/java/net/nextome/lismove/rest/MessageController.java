package net.nextome.lismove.rest;

import net.nextome.lismove.models.NotificationMessage;
import net.nextome.lismove.rest.dto.NotificationMessageDto;
import net.nextome.lismove.rest.mappers.NotificationMessageMapper;
import net.nextome.lismove.services.NotificationMessageService;
import net.nextome.lismove.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("messages")
public class MessageController {

	@Autowired
	private NotificationMessageService notificationService;
	@Autowired
	private NotificationMessageMapper mapper;
	@Autowired
	private UserService userService;

	@PostMapping("send")
	public String sendNotification(@RequestBody NotificationMessageDto messageDto) {
		NotificationMessage message = mapper.dtoToNotificationMessage(messageDto);
		notificationService.send(message);
		return "ok";
	}

	@GetMapping
	public List<NotificationMessageDto> list() {
		return mapper.notificationMessageToDto(notificationService.getAll());
	}

	@GetMapping("{mid}")
	public NotificationMessageDto get(@PathVariable("mid") Long mid) {
		return notificationService.getMessageDetails(mid);
	}
}
