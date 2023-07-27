package net.nextome.lismove.repositories;

import net.nextome.lismove.models.OfflineSession;
import org.springframework.data.repository.CrudRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OfflineSessionRepository extends CrudRepository<OfflineSession, UUID> {
	List<OfflineSession> findAllByOrderByStartTimeDesc();

	List<OfflineSession> findByUserUid(String uid);

	Optional<OfflineSession> findByStartTime(LocalDateTime start);
}
