package net.nextome.lismove.repositories;

import net.nextome.lismove.models.AwardAchievementUser;
import net.nextome.lismove.models.AwardPosition;
import net.nextome.lismove.models.AwardPositionUser;
import net.nextome.lismove.models.User;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface AwardPositionUserRepository extends CrudRepository<AwardPositionUser, Long> {

    Optional<AwardPositionUser> findByAwardPosition(AwardPosition awardPosition);

    List<AwardPositionUser> findAllByAwardPosition(AwardPosition awardPosition);

    Set<AwardPositionUser> findByAwardPositionAndUser(AwardPosition awardPosition, User user);

    List<AwardPositionUser> findAllByUser(User user);

}
