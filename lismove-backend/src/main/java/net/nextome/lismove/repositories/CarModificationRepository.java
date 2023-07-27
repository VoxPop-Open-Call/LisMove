package net.nextome.lismove.repositories;

import net.nextome.lismove.models.CarGeneration;
import net.nextome.lismove.models.CarModification;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface CarModificationRepository extends CrudRepository<CarModification, Long> {

	List<CarModification> findByGeneration(CarGeneration generation);
}
