package net.nextome.lismove.rest.mappers;

import net.nextome.lismove.models.HomeAddress;
import net.nextome.lismove.models.Seat;
import net.nextome.lismove.models.WorkAddress;
import net.nextome.lismove.rest.dto.AddressOverviewDto;
import net.nextome.lismove.services.OrganizationSettingsService;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring")
@Service
public abstract class AddressMapper extends UtilMapper {
    @Autowired
    private OrganizationSettingsService organizationSettingsService;

    @Mapping(target = "cityName", source = "city.city")
    public abstract AddressOverviewDto homeAddressToDto(HomeAddress address);
    public abstract Set<AddressOverviewDto> homeAddressToDto(Set<HomeAddress> addresses);
    public abstract List<AddressOverviewDto> homeAddressToDto(List<HomeAddress> addresses);

    @Mapping(target = "tolerance", source = "destinationTolerance")
    public abstract AddressOverviewDto seatToDto(Seat address);
    public abstract Set<AddressOverviewDto> seatToDto(Set<Seat> addresses);

    @Mapping(target = "address", source = "seat.address")
    @Mapping(target = "number", source = "seat.number")
    @Mapping(target = "city", source = "seat.city")
    @Mapping(target = "cityName", source = "seat.city.city")
    @Mapping(target = "latitude", source = "seat.latitude")
    @Mapping(target = "longitude", source = "seat.longitude")
    @Mapping(target = "tolerance", source = "seat.destinationTolerance")
    public abstract AddressOverviewDto workAddressToDto(WorkAddress address);
    public abstract List<AddressOverviewDto> workAddressToDto(List<WorkAddress> addresses);
    public abstract Set<AddressOverviewDto> workAddressToDto(Set<WorkAddress> addresses);

    @AfterMapping
    protected void seatToDto(@MappingTarget AddressOverviewDto dto, Seat seat) {
        if (dto.getTolerance() == null) {
            dto.setTolerance(organizationSettingsService.get(seat.getOrganization(), "homeWorkPointsTolerance", Double.class));
        }
    }

    @AfterMapping
    protected void workAddressToDto(@MappingTarget AddressOverviewDto dto, WorkAddress address) {
        if (dto.getTolerance() == null) {
            dto.setTolerance(organizationSettingsService.get(address.getSeat().getOrganization(), "homeWorkPointsTolerance", Double.class));
        }
    }

}
