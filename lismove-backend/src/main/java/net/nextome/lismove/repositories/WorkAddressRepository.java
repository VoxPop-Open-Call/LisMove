package net.nextome.lismove.repositories;

import net.nextome.lismove.models.Seat;
import net.nextome.lismove.models.User;
import net.nextome.lismove.models.WorkAddress;
import org.springframework.data.repository.CrudRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface WorkAddressRepository extends CrudRepository<WorkAddress, Long> {

    Set<WorkAddress> findByUserAndEndAssociationNull(User user);

    List<WorkAddress> findByUserOrderByIdAsc(User user);

    Set<WorkAddress> findBySeat(Seat seat);

    Set<WorkAddress> findAllByEndAssociationIsNullAndSeat(Seat seat);

    Set<WorkAddress> findByUserAndStartAssociationLessThanEqualAndEndAssociationGreaterThanEqual(User user, LocalDateTime startAssociation, LocalDateTime endAssociation);
}
