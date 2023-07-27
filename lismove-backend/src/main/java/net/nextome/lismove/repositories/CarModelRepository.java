package net.nextome.lismove.repositories;

import net.nextome.lismove.models.CarBrand;
import net.nextome.lismove.models.CarModel;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface CarModelRepository extends CrudRepository<CarModel, Long> {

	List<CarModel> findByBrand(CarBrand brand);

}
