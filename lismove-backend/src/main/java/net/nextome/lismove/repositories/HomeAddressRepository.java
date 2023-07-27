package net.nextome.lismove.repositories;

import net.nextome.lismove.models.HomeAddress;
import net.nextome.lismove.models.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface HomeAddressRepository extends CrudRepository<HomeAddress, Long> {

    Optional<HomeAddress> findByUserAndEndAssociationNull(User user);

    List<HomeAddress> findByUserOrderByIdAsc(User user);

    @Query("select h from HomeAddress h where h.user = :user and h.startAssociation < :timestamp and (h.endAssociation is null or h.endAssociation > :timestamp)")
    Optional<HomeAddress> findActiveByUserAt(User user, LocalDateTime timestamp);

}
