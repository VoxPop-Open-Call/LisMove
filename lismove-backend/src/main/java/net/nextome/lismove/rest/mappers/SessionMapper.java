package net.nextome.lismove.rest.mappers;

import net.nextome.lismove.models.OfflineSession;
import net.nextome.lismove.models.Session;
import net.nextome.lismove.models.SessionPoint;
import net.nextome.lismove.rest.dto.OfflineSessionDto;
import net.nextome.lismove.rest.dto.SessionDto;
import net.nextome.lismove.rest.dto.SessionOverviewDto;
import net.nextome.lismove.rest.dto.SessionPointDto;
import net.nextome.lismove.services.SessionPointService;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
@Service
public abstract class SessionMapper extends UtilMapper {

	@Autowired
	private SessionPointService sessionPointService;
	@Autowired
	private SessionPointMapper sessionPointMapper;

	@Mapping(source = "uid", target = "user")
	@Mapping(target = "homeAddress", ignore = true)
	@Mapping(target = "workAddress", ignore = true)
	public abstract Session dtoToSession(SessionDto dto);

	public abstract List<Session> dtoToSession(List<SessionDto> list);

	@Mapping(target = "uid", source = "user.uid")
	@Mapping(target = "homeAddress", source = "homeAddress.id")
	@Mapping(target = "workAddress", source = "workAddress.seat.id")
	public abstract SessionDto sessionToDto(Session s);

	public abstract List<SessionDto> sessionToDto(List<Session> list);

	@AfterMapping
	public void sessionToDto(Session session, @MappingTarget SessionDto dto) {
		List<SessionPoint> sessionPoints = sessionPointService.findBySession(session);
		dto.setSessionPoints(sessionPointMapper.sessionPointToDto(sessionPoints));
	}

	@Named("sessionToDtoWithoutPartials")
	@Mapping(target = "uid", source = "user.uid")
	@Mapping(target = "homeAddress", source = "homeAddress.id")
	@Mapping(target = "workAddress", source = "workAddress.seat.id")
	@Mapping(target = "partials", ignore = true)
	public abstract SessionDto sessionToDtoWithoutPartials(Session session);

	@Mapping(target = ".", qualifiedByName = {"sessionToDtoWithoutPartials"})
	public abstract List<SessionDto> sessionToDtoWithoutPartials(List<Session> sessions);

	public abstract OfflineSession dtoToOfflineSession(OfflineSessionDto dto);

	public abstract List<OfflineSession> dtoToOfflineSession(List<OfflineSessionDto> dto);

	@Mapping(source = "user.uid", target = "user")
	@Mapping(source = "user.email", target = "email")
	@Mapping(source = "user.username", target = "username")
	public abstract OfflineSessionDto offlineSessionToDto(OfflineSession dto);

	public abstract List<OfflineSessionDto> offlineSessionToDto(List<OfflineSession> dto);

	@Mapping(target = "homeAddress", source = "homeAddress.address")
	@Mapping(target = "homeNumber", source = "homeAddress.number")
	@Mapping(target = "homeCity", source = "homeAddress.city")
	@Mapping(target = "homeAddressLng", source = "homeAddress.longitude")
	@Mapping(target = "homeAddressLat", source = "homeAddress.latitude")
	@Mapping(target = "workAddress", source = "workAddress.seat.address")
	@Mapping(target = "workNumber", source = "workAddress.seat.number")
	@Mapping(target = "workCity", source = "workAddress.seat.city")
	@Mapping(target = "workAddressLng", source = "workAddress.seat.longitude")
	@Mapping(target = "workAddressLat", source = "workAddress.seat.latitude")
	@Mapping(target = "uid", source = "user.uid")
	@Mapping(target = "email", source = "user.email")
	@Mapping(target = "phoneNumber", source = "user.phoneNumber")
	@Mapping(target = "username", source = "user.username")
	@Mapping(target = "firstName", source = "user.firstName")
	@Mapping(target = "lastName", source = "user.lastName")
	@Mapping(target = "totalRank", source = "user.totalRank")
	@Mapping(target = "currentRank", source = "user.currentRank")
	@Mapping(target = "lastLoggedIn", source = "user.lastLoggedIn")
	public abstract SessionOverviewDto sessionToOverviewDto(Session session);

	public abstract List<SessionOverviewDto> sessionToOverviewDto(List<Session> sessions);

	@Mapping(target = "user", ignore = true)
	@Mapping(target = "homeAddress", ignore = true)
	@Mapping(target = "workAddress", ignore = true)
	public abstract Session sessionOverviewDtoToSession(SessionOverviewDto dto);

	SessionPoint mapSessionPoints(SessionPointDto dto) {
		return sessionPointMapper.dtoToSessionPoint(dto);
	}

	SessionPointDto mapSessionPoints(SessionPoint sessionPoint) {
		return sessionPointMapper.sessionPointToDto(sessionPoint);
	}

	List<String> mapPolyline(String value) {
		if(value == null) {
			return null;
		}
		return Arrays.stream(value.split("€")).collect(Collectors.toList());
	}

	String mapPolyline(List<String> value) {
		if(value == null) {
			return null;
		}
		StringBuilder res = new StringBuilder();
		Iterator<String> iterator = value.iterator();
		while(iterator.hasNext()) {
			res.append(iterator.next()).append(iterator.hasNext() ? "€" : "");
		}
		return res.toString();
	}
}
