package net.nextome.lismove.repositories;

import net.nextome.lismove.models.Achievement;
import net.nextome.lismove.models.AchievementUser;
import net.nextome.lismove.models.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AchievementUserRepository extends CrudRepository<AchievementUser, Long> {

	Optional<AchievementUser> findByUserAndAchievement(User u, Achievement a);

	@Query(value = "from AchievementUser au where au.user=:u and au.achievement.startDate<=:date and au.achievement.endDate >= :date")
	List<AchievementUser> findActiveByUser(User u, LocalDate date);

	List<AchievementUser> findByAchievementOrderByScoreDesc(Achievement a);
}
