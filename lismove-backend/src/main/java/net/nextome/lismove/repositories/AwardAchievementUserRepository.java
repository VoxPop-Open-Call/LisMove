package net.nextome.lismove.repositories;

import net.nextome.lismove.models.AwardAchievement;
import net.nextome.lismove.models.AwardAchievementUser;
import net.nextome.lismove.models.User;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface AwardAchievementUserRepository extends CrudRepository<AwardAchievementUser, Long> {

    Optional<AwardAchievementUser> findByAwardAchievement(AwardAchievement awardAchievement);

    List<AwardAchievementUser> findAllByAchievementUser_User(User achievementUser_user);

    Set<AwardAchievementUser> findByAwardAchievementAndAchievementUser_User(AwardAchievement awardAchievement, User achievementUser_user);
}
