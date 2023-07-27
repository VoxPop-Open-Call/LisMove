package net.nextome.lismove.rest.mappers;

import net.nextome.lismove.exceptions.LismoveException;
import net.nextome.lismove.models.NotificationMessage;
import net.nextome.lismove.models.NotificationMessageDelivery;
import net.nextome.lismove.rest.dto.NotificationMessageDeliveryDto;
import net.nextome.lismove.rest.dto.NotificationMessageDto;
import net.nextome.lismove.rest.dto.NotificationMessageReceiverDto;
import net.nextome.lismove.services.OrganizationService;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Mapper(componentModel = "spring")
@Service
public abstract class NotificationMessageMapper extends UtilMapper {

	@Autowired
	private OrganizationService organizationService;

	public abstract NotificationMessageDto notificationMessageToDto(NotificationMessage message);

	public abstract List<NotificationMessageDto> notificationMessageToDto(List<NotificationMessage> messages);

	@Mapping(source = "notificationMessage.id", target = "message")
	@Mapping(source = "notificationMessage.title", target = "title")
	@Mapping(source = "notificationMessage.body", target = "body")
	@Mapping(source = "notificationMessage.imageUrl", target = "imageUrl")
	@Mapping(source = "notificationMessage.createdDate", target = "createdDate")
	@Mapping(source = "notificationMessage.organization.id", target = "organization")
	@Mapping(source = "user.uid", target = "uid")
	@Mapping(source = "user.username", target = "username")
	public abstract NotificationMessageDeliveryDto notificationMessageDeliveryToDto(NotificationMessageDelivery message);

	public abstract List<NotificationMessageDeliveryDto> notificationMessageDeliveryToDto(List<NotificationMessageDelivery> messages);

	@Mapping(source = "user.uid", target = "uid")
	@Mapping(source = "user.username", target = "username")
	public abstract NotificationMessageReceiverDto notificationMessageDeliveryToReceiverDto(NotificationMessageDelivery message);

	public abstract List<NotificationMessageReceiverDto> notificationMessageDeliveryToReceiverDto(List<NotificationMessageDelivery> messages);

	public abstract NotificationMessage dtoToNotificationMessage(NotificationMessageDto message);

	public abstract List<NotificationMessage> dtoToNotificationMessage(List<NotificationMessageDto> messages);

	@AfterMapping
	public void dtoToNotificationMessage(@MappingTarget NotificationMessage message, NotificationMessageDto dto) {
		if(dto.getOrganization() != null) {
			message.setOrganization(organizationService.findById(dto.getOrganization()).orElseThrow(() -> new LismoveException("Organization not found")));
		}
	}

}
