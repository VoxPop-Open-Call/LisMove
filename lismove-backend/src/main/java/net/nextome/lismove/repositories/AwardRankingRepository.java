package net.nextome.lismove.repositories;

import net.nextome.lismove.models.AwardRanking;
import net.nextome.lismove.models.Ranking;
import net.nextome.lismove.models.User;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface AwardRankingRepository extends CrudRepository<AwardRanking, Long> {

    List<AwardRanking> findAllByRanking(Ranking ranking);

    List<AwardRanking> findAllByRankingOrderByValueDesc(Ranking ranking);

    List<AwardRanking> findAllByUser(User user);

}
