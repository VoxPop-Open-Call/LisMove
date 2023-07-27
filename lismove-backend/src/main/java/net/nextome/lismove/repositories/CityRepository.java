package net.nextome.lismove.repositories;

import net.nextome.lismove.models.City;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface CityRepository extends CrudRepository<City, Long> {

	Optional<City> findByCity(String name);
}
