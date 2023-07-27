package net.nextome.lismove.repositories;

import net.nextome.lismove.models.HomeWorkPath;
import net.nextome.lismove.models.Seat;
import net.nextome.lismove.models.User;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;
import java.util.Set;

public interface HomeWorkPathRepository extends CrudRepository<HomeWorkPath, Long> {

    Set<HomeWorkPath> findByUser(User user);

    Set<HomeWorkPath> findBySeat(Seat seat);

    void deleteAllBySeat(Seat seat);

}
