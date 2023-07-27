package net.nextome.lismove.repositories;

import net.nextome.lismove.models.Organization;
import net.nextome.lismove.models.User;
import net.nextome.lismove.models.enums.UserType;
import net.nextome.lismove.models.query.UserDashboardSession;
import net.nextome.lismove.models.query.UserDistanceStats;
import net.nextome.lismove.models.query.UserRankingPosition;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface UserRepository extends CrudRepository<User, String> {

	Optional<User> findByEmail(String email);

	Optional<User> findByUsername(String username);

	Optional<User> findByUid(String uid);

	Optional<User> findByCoinWallet(String address);

	Set<User> findAllByUserType(UserType userType);

	/**
	 * seleziona il numero di utenti che ha l'username simile a quello fornito
	 *
	 * @param username username
	 * @return numero utenti
	 */
	@Query(value = "select count(u) from User u where u.username like ':username%'")
	Integer countByUsernameLike(String username);

	@Modifying
	@Query(value = "update users set last_logged_in=now() where uid=?1", nativeQuery = true)
	Integer updateLastLoggedIn(String uid);

	@Query(value = "select u.username, coalesce(u.earned_national_points, 0) as points, u.avatar_url from users u where u.username is not null order by points desc, u.username", nativeQuery = true)
	List<UserRankingPosition> findAllOrderByEarnedNationalPointsDesc();

	@Query(value = "select count(*) as sessions, sum(co2) as co2, avg(coalesce(gps_only_distance, 0) + gyro_distance) as avgDistance, sum(coalesce(gps_only_distance, 0) + gyro_distance) as distance from sessions where user_uid=:uid and valid is true", nativeQuery = true)
	UserDashboardSession findCountSessionByUser(String uid);

	@Query(value = "select date_trunc('day',start_time) as day, sum(coalesce(gps_only_distance, 0) + gyro_distance) as distance from sessions where user_uid=:uid and start_time > current_date - interval '1' month group by date_trunc('day',start_time)", nativeQuery = true)
	List<UserDistanceStats> findDistanceByUserInDays(String uid);

	@Query(value = "select date_trunc('month',start_time) as day, sum(coalesce(gps_only_distance, 0) + gyro_distance) as distance from sessions where valid is true and user_uid=:uid and start_time > current_date - interval '1' year group by date_trunc('month',start_time)", nativeQuery = true)
	List<UserDistanceStats> findDistanceByUserInMonths(String uid);

	List<User> findAllByHomeAddressIsNull();

	@Query(value = "SELECT u FROM Enrollment e JOIN User u ON e.user=u WHERE e.organization = :org AND current_date BETWEEN e.startDate AND e.endDate")
	List<User> findAllByOrganization(@Param("org") Organization org);
}
