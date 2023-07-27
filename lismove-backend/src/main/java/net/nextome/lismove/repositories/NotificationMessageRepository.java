package net.nextome.lismove.repositories;

import net.nextome.lismove.models.NotificationMessage;
import net.nextome.lismove.models.Organization;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface NotificationMessageRepository extends CrudRepository<NotificationMessage, Long> {

	List<NotificationMessage> findByOrganization(Organization organization);

}
