package net.nextome.lismove.rest;

import net.nextome.lismove.exceptions.LismoveException;
import net.nextome.lismove.models.City;
import net.nextome.lismove.repositories.CityRepository;
import net.nextome.lismove.rest.dto.CityOverviewDto;
import net.nextome.lismove.rest.mappers.CityMapper;
import net.nextome.lismove.services.CityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedList;
import java.util.List;

@RestController
@RequestMapping("cities")
public class CityController {

    @Autowired
    private CityService cityService;
    @Autowired
    private CityRepository cityRepository;
    @Autowired
    private CityMapper cityMapper;

    @GetMapping
    public List<CityOverviewDto> list(){
        LinkedList<City> list = new LinkedList<>();
        cityRepository.findAll().forEach(list::add);
        return cityMapper.cityToOverviewDto(list);
    }

    @GetMapping("{istatId}")
    public City get(@PathVariable Long istatId) {
        return cityRepository.findById(istatId).orElseThrow(() -> new LismoveException("City not found", HttpStatus.NOT_FOUND));
    }

}
