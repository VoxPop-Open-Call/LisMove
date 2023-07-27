package net.nextome.lismove.rest.mappers;

import net.nextome.lismove.models.Smartphone;
import net.nextome.lismove.models.User;
import net.nextome.lismove.models.WorkAddress;
import net.nextome.lismove.rest.dto.*;
import net.nextome.lismove.services.AddressService;
import net.nextome.lismove.services.SensorService;
import net.nextome.lismove.services.UserService;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
@Service
public abstract class UserMapper extends UtilMapper {

	@Autowired
	protected UserService userService;
	@Autowired
	protected SensorService sensorService;
	@Autowired
	protected AddressService addressService;
	@Autowired
	protected CityMapper cityMapper;
	@Autowired
	protected SeatMapper seatMapper;
	@Autowired
	protected AddressMapper addressMapper;

	@Mapping(target = "homeAddress", ignore = true)
	public abstract User dtoToUser(LismoverUserDto dto);

	public abstract List<User> dtoToUser(List<LismoverUserDto> dto);

	@Mapping(target = "homeAddress", source = "homeAddress.address")
	@Mapping(target = "homeNumber", source = "homeAddress.number")
	@Mapping(target = "homeCity", source = "homeAddress.city")
	@Mapping(target = "homeLatitude", source = "homeAddress.latitude")
	@Mapping(target = "homeLongitude", source = "homeAddress.longitude")
	public abstract LismoverUserDto userToDto(User u);

	public abstract List<LismoverUserDto> userToDto(List<User> u);

	@AfterMapping
	public void userToDto(User user, @MappingTarget LismoverUserDto dto) {
		userService.getActivePhone(user).ifPresent(phone -> dto.setActivePhone(phone.getImei()));
		dto.setWorkAddresses(seatMapper.seatToDto(addressService.getActiveWorkAddresses(user).stream().map(WorkAddress::getSeat).collect(Collectors.toSet())));
	}

	//	Overview
	public abstract UserOverviewDto userToOverviewDto(User user);

	public abstract List<UserOverviewDto> userToOverviewDto(List<User> users);

	@AfterMapping
	public void userToOverviewDto(User user, @MappingTarget UserOverviewDto dto) {
		dto.setHomeAddresses(addressMapper.homeAddressToDto(addressService.getHomeAddressesHistory(user)));
		dto.setWorkAddresses(addressMapper.workAddressToDto(addressService.getWorkAddressesHistory(user)));
	}

	public abstract User managerDtoToUser(ManagerDto dto);

	public abstract ManagerDto userToManagerDto(User user);

	public abstract Set<ManagerDto> userToManagerDto(Set<User> users);

	@Mapping(target = "points", ignore = true)
	public abstract RankingPositionDto userToRankDto(User user);

	public abstract List<RankingPositionDto> userToRankDto(List<User> user);

	@Named("global")
	@Mapping(target = "points", source = "earnedNationalPoints")
	public abstract RankingPositionDto userToRankDtoGlobal(User user);

	public abstract List<RankingPositionDto> userToRankDtoGlobal(List<User> user);

	public abstract SmartphoneDto smartphoneToDto(Smartphone smartphone);

	public abstract List<SmartphoneDto> smartphoneToDto(List<Smartphone> smartphone);
}
