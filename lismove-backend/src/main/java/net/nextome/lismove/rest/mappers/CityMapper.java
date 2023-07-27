package net.nextome.lismove.rest.mappers;

import net.nextome.lismove.models.City;
import net.nextome.lismove.rest.dto.CityOverviewDto;
import net.nextome.lismove.services.CityService;
import org.mapstruct.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Mapper(componentModel = "spring")
@Service
public abstract class CityMapper extends UtilMapper {

    @Autowired
    protected CityService cityService;

    public abstract CityOverviewDto cityToOverviewDto(City city);
    public abstract List<CityOverviewDto> cityToOverviewDto(List<City> cities);

}
