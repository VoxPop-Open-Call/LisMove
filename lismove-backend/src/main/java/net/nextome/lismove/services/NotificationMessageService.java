package net.nextome.lismove.services;

import com.google.firebase.messaging.FirebaseMessagingException;
import net.nextome.modules.notifications.services.NotificationService;
import net.nextome.lismove.exceptions.LismoveException;
import net.nextome.lismove.models.*;
import net.nextome.lismove.repositories.NotificationMessageDeliveryRepository;
import net.nextome.lismove.repositories.NotificationMessageRepository;
import net.nextome.lismove.repositories.SmartphoneRepository;
import net.nextome.lismove.rest.dto.NotificationMessageDto;
import net.nextome.lismove.rest.mappers.NotificationMessageMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Transactional
public class NotificationMessageService {

	@Autowired
	private NotificationMessageRepository notificationMessageRepository;
	@Autowired
	private NotificationMessageDeliveryRepository notificationMessageDeliveryRepository;
	@Autowired
	private SmartphoneRepository smartphoneRepository;
	@Autowired
	private NotificationService notificationService;
	@Autowired
	private NotificationMessageMapper mapper;

	public List<NotificationMessage> getByOrganization(Organization organization) {
		return notificationMessageRepository.findByOrganization(organization);
	}

	public NotificationMessageDto getMessageDetails(Long id) {
		NotificationMessage message = notificationMessageRepository.findById(id).orElseThrow(() -> new LismoveException("Message not found"));
		NotificationMessageDto dto = mapper.notificationMessageToDto(message);
		dto.setReceivers(mapper.notificationMessageDeliveryToReceiverDto(notificationMessageDeliveryRepository.findByNotificationMessage(message)));
		return dto;
	}

	public NotificationMessageDelivery markRead(Long message, User u) {
		NotificationMessage m = notificationMessageRepository.findById(message).orElseThrow(() -> new LismoveException("Message not found", HttpStatus.NOT_FOUND));
		NotificationMessageDelivery delivery = notificationMessageDeliveryRepository.findByNotificationMessageAndUser(m, u);
		delivery.setRead(true);
		return notificationMessageDeliveryRepository.save(delivery);

	}

	public List<NotificationMessageDelivery> getByUser(User user) {
		return notificationMessageDeliveryRepository.findByUser(user);
	}

	public void send(NotificationMessage message) {
		List<Smartphone> phones;
		if(message.getOrganization() == null) {
			phones = smartphoneRepository.findByEndAssociationIsNull();
		} else {
			phones = smartphoneRepository.findActiveByOrganizationId(message.getOrganization().getId());
		}
		notificationMessageRepository.save(message);
		List<String> tokens = phones.stream().map(Smartphone::getFcmToken).filter(Objects::nonNull).collect(Collectors.toCollection(LinkedList::new));
		try {
			phones.forEach(p -> {
				NotificationMessageDelivery delivery = new NotificationMessageDelivery();
				delivery.setNotificationMessage(message);
				delivery.setRead(false);
				delivery.setUser(p.getUser());
				notificationMessageDeliveryRepository.save(delivery);
			});
			notificationService.sendMobileNotification(tokens, message.getTitle(), message.getBody(), message.getImageUrl(), new HashMap<String, String>() {{
				put("messageId", message.getId().toString());
			}});
		} catch(FirebaseMessagingException e) {
			e.printStackTrace();
		}
	}

	public List<NotificationMessage> getAll() {
		List<NotificationMessage> messages = new LinkedList<>();
		notificationMessageRepository.findAll().forEach(messages::add);
		return messages;
	}
}
