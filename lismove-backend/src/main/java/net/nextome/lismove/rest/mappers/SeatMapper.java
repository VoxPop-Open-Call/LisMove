package net.nextome.lismove.rest.mappers;

import net.nextome.lismove.models.Seat;
import net.nextome.lismove.rest.dto.SeatDto;
import net.nextome.lismove.services.CityService;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;

@Mapper(componentModel = "spring")
@Service
public abstract class SeatMapper extends UtilMapper {

	@Autowired
	private CityService cityService;

	@Mapping(target = "cityName", source = "city")
	@Mapping(target = "organization", source = "organization.id")
	public abstract SeatDto seatToDto(Seat seat);
	public abstract Set<SeatDto> seatToDto(Set<Seat> seat);

	@AfterMapping
	void dtoToSeat(@MappingTarget Seat seat, SeatDto dto) {
		if(dto.getCity() == null && dto.getCityName() != null) {
			seat.setCity(cityService.findByName(dto.getCityName()));
		}
	}
	@Mapping(target = "organization", ignore = true)
	public abstract Seat dtoToSeat(SeatDto dto);
}
