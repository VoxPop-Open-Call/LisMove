package net.nextome.lismove.repositories;

import net.nextome.lismove.models.NotificationMessage;
import net.nextome.lismove.models.NotificationMessageDelivery;
import net.nextome.lismove.models.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface NotificationMessageDeliveryRepository extends CrudRepository<NotificationMessageDelivery, Long> {

	List<NotificationMessageDelivery> findByNotificationMessage(NotificationMessage notificationMessage);

	List<NotificationMessageDelivery> findByUser(User user);

	NotificationMessageDelivery findByNotificationMessageAndUser(NotificationMessage m, User u);

	@Query(nativeQuery = true, value = "select count(*) from notification_messages_delivery where read is false and user_uid=:uid")
	Integer countUnreadMessages(String uid);

}
