package net.nextome.lismove.repositories;

import net.nextome.lismove.models.AwardCustom;
import net.nextome.lismove.models.AwardCustomUser;
import net.nextome.lismove.models.User;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface AwardCustomUserRepository extends CrudRepository<AwardCustomUser, Long> {

    List<AwardCustomUser> findAllByAwardCustom(AwardCustom awardCustom);

    List<AwardCustomUser> findAllByAwardCustomAndUser(AwardCustom awardCustom, User user);

    List<AwardCustomUser> findAllByUser(User user);

}
