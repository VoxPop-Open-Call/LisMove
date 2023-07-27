package net.nextome.lismove.services;

import net.nextome.lismove.models.City;
import net.nextome.lismove.repositories.CityRepository;
import net.nextome.lismove.services.utils.UtilitiesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CityService extends UtilitiesService {

    @Autowired
    private CityRepository cityRepository;

    public Optional<City> findById(Long istatId) {
        return cityRepository.findById(istatId);
    }

    public City findByName(String name) { return cityRepository.findByCity(name).orElse(null); }

    public List<City> getAll() {
        List<City> cities = (List<City>) cityRepository.findAll();
        return cities;
    }
}
