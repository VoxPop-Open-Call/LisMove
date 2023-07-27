package net.nextome.lismove.repositories;

import net.nextome.lismove.models.AwardCustom;
import net.nextome.lismove.models.User;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Set;

public interface AwardCustomRepository extends CrudRepository<AwardCustom, Long> {
}
