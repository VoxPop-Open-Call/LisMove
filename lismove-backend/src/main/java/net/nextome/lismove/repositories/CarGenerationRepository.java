package net.nextome.lismove.repositories;

import net.nextome.lismove.models.CarGeneration;
import net.nextome.lismove.models.CarModel;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface CarGenerationRepository extends CrudRepository<CarGeneration, Long> {

	List<CarGeneration> findByModel(CarModel model);
}
