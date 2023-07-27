package net.nextome.lismove.repositories;

import net.nextome.lismove.models.Achievement;
import net.nextome.lismove.models.AwardAchievement;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface AwardAchievementRepository extends CrudRepository<AwardAchievement, Long> {

    List<AwardAchievement> findByAchievement(Achievement achievement);

}
