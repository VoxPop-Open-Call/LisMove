package net.nextome.lismove.repositories;

import net.nextome.lismove.models.Organization;
import net.nextome.lismove.models.Seat;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;
import java.util.Set;

public interface SeatRepository extends CrudRepository<Seat, Long> {

	Optional<Seat> findByIdAndDeletedFalse(Long id);

	Set<Seat> findAllByOrganizationAndDeletedFalse(Organization organization);

}
