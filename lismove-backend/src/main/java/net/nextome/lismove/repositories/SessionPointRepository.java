package net.nextome.lismove.repositories;

import net.nextome.lismove.models.Session;
import net.nextome.lismove.models.SessionPoint;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface SessionPointRepository extends CrudRepository<SessionPoint, Long> {

	void deleteAllBySession(Session session);

	List<SessionPoint> findBySession(Session session);

	@Query(value = "select sum(sp.euro) from session_points sp where sp.session_id in (select s.id from sessions s where s.user_uid=:uid) and sp.organization_id=:org", nativeQuery = true)
	Optional<BigDecimal> getTotInitiativeEuros(String uid, Long org);

	@Query(value = "select sum(sp.euro) from session_points sp where sp.session_id in (select s.id from sessions s where s.user_uid=:uid and date_part('year', s.start_time)=:y and date_part('month', s.start_time)=:m) and sp.organization_id=:org", nativeQuery = true)
	Optional<BigDecimal> getTotMonthlyEuros(String uid, Long org, Integer y, Integer m);

	@Query(value = "select sum(sp.euro) from session_points sp where sp.session_id in (select s.id from sessions s where s.user_uid=:uid and date_part('year', s.start_time)=:y and date_part('month', s.start_time)=:m and date_part('day', s.start_time)=:d) and sp.organization_id=:org", nativeQuery = true)
	Optional<BigDecimal> getTotDailyEuros(String uid, Long org, Integer y, Integer m, Integer d);
}
