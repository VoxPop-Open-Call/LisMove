package net.nextome.lismove.repositories;

import net.nextome.lismove.models.Session;
import net.nextome.lismove.models.User;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SessionRepository extends CrudRepository<Session, UUID> {
	List<Session> findAllByOrderByStartTimeDesc();

	@Query(nativeQuery = true, value = "SELECT * from sessions ORDER BY start_time DESC LIMIT :limit")
	List<Session> findAllByOrderByStartTimeDesc(Integer limit);

	List<Session> findByUserUid(String uid);

	Optional<Session> findByUserUidAndStartTime(String uid, LocalDateTime start);

	List<Session> findByUserUidAndStartTimeAfter(String uid, LocalDateTime start);

	Optional<List<Session>> findByValidIsTrueAndUserAndStartTimeAfter(User user, LocalDateTime start);

	Optional<List<Session>> findByValidIsTrueAndStartTimeAfter(LocalDateTime start);

	List<Session> findByOldSessionIdIsNullAndStartTimeAfter(LocalDateTime start);

	@Query(value = "select s.* from sessions s where s.old_session_id is not null and s.polyline is null", nativeQuery = true)
	List<Session> findBrokenSessions();

	@Query(value = "select s.* from sessions s where s.old_session_id is not null and s.polyline is null and s.user_uid=:uid", nativeQuery = true)
	List<Session> findBrokenSessionsByUser(String uid);

	@Query(value = "select count(s.*) from sessions s where s.old_session_id is not null", nativeQuery = true)
	Integer countAllBrokenSessions();

	List<Session> findByStartTimeAfter(LocalDateTime start);

	@Query(value = "SELECT s.* FROM sessions s JOIN session_points sp ON sp.session_id = s.id WHERE sp.organization_id = :organization ORDER BY s.start_time DESC", nativeQuery = true)
	List<Session> findByOrganization(Long organization);

	@Query(value = "SELECT s.* FROM sessions s JOIN session_points sp ON sp.session_id = s.id WHERE sp.organization_id = :organization ORDER BY s.start_time DESC LIMIT :limit", nativeQuery = true)
	List<Session> findByOrganization(Long organization, int limit);

	List<Session> findByUserAndStartTimeIsGreaterThanEqualAndEndTimeIsLessThanEqual(User user, LocalDateTime startTime, LocalDateTime endTime);

	@Modifying
	@Query(nativeQuery = true, value = "REFRESH MATERIALIZED VIEW heatmap_mat_view")
	void refreshHeatmapView();
}
