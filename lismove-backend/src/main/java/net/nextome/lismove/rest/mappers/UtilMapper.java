package net.nextome.lismove.rest.mappers;

import net.nextome.lismove.models.*;
import net.nextome.lismove.models.enums.*;
import net.nextome.lismove.services.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.*;
import java.util.Optional;
import java.util.UUID;

public class UtilMapper {
	@Autowired
	private CityService cityService;
	@Autowired
	private UserService userService;
	@Autowired
	private OrganizationService organizationService;
	@Autowired
	private RankingService rankingService;
	@Autowired
	private CustomFieldService customFieldService;
	@Autowired
	private OrganizationSettingsService organizationSettingsService;
	@Autowired
	private VendorService vendorService;

	LocalDateTime mapTime(Long value) {
		if(value == null) {
			return null;
		}
		return LocalDateTime.ofInstant(Instant.ofEpochMilli(value), ZoneId.systemDefault());
	}

	LocalDate mapDate(Long value) {
		if(value == null) {
			return null;
		}
		return Instant.ofEpochMilli(value).atZone(ZoneId.systemDefault()).toLocalDate();
	}

	Long mapTime(LocalDateTime value) {
		if(value == null) {
			return null;
		}
		return value.toInstant(ZoneOffset.UTC).toEpochMilli();
	}

	Long mapDate(LocalDate value) {
		if(value == null) {
			return null;
		}
		return value.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
	}

	User mapUser(String uid) {
		if(uid == null) {
			return null;
		}
		Optional<User> user = userService.findByUid(uid);
		return user.orElse(null);
	}

	String mapErrorCode(SessionStatus errorCode) {
		if(errorCode != null) {
			return errorCode.getMsg();
		} else {
			return null;
		}
	}

	City mapCity(Long value) {
		if(value == null) {
			return null;
		}
		return cityService.findById(value).orElse(null);
	}

	Long mapCity(City value) {
		if(value == null) {
			return null;
		}
		return value.getIstatId();
	}

	SessionType mapSessionType(Integer value) {
		if(value == null) {
			return null;
		}
		return SessionType.values()[value];
	}

	Integer mapSessionType(SessionType value) {
		if(value == null) {
			return null;
		}
		return value.ordinal();
	}

	City mapCityName(String value) {
		if(value == null) {
			return null;
		}
		return cityService.findByName(value);
	}

	String mapCityName(City value) {
		if(value == null) {
			return null;
		}
		return value.getCity();
	}

	String mapUUID(UUID value) {
		if(value == null) {
			return null;
		}
		return value.toString();
	}

	UUID mapUUID(String value) {
		if(value == null) {
			return null;
		}
		return UUID.fromString(value);
	}

	Organization mapOrganization(Long value) {
		if(value == null) {
			return null;
		}
		return organizationService.findById(value).orElse(null);
	}

	Long mapOrganization(Organization value) {
		if(value == null) {
			return null;
		}
		return value.getId();
	}

	Ranking mapRanking(Long value) {
		if(value == null) {
			return null;
		}
		return rankingService.findById(value).orElse(null);
	}

	Enrollment mapEnrollment(Long value) {
		if(value == null) {
			return null;
		}
		return organizationService.findEnrollmentById(value).orElse(null);
	}

	CustomField mapCustomField(Long value) {
		if(value == null) {
			return null;
		}
		return customFieldService.findById(value).orElse(null);
	}

	CustomFieldValue mapCustomFieldValue(Long value) {
		if(value == null) {
			return null;
		}
		return customFieldService.findValueById(value).orElse(null);
	}

	OrganizationType mapOrganizationType(Integer value) {
		if(value == null) {
			return null;
		}
		return OrganizationType.values()[value];
	}

	Integer mapOrganizationType(OrganizationType value) {
		if(value == null) {
			return null;
		}
		return value.ordinal();
	}

	AwardType mapAwardType(Integer value) {
		if(value == null) {
			return null;
		}
		return AwardType.values()[value];
	}

	Integer mapAwardType(AwardType value) {
		if(value == null) {
			return null;
		}
		return value.ordinal();
	}

	SessionStatus mapSessionStatus(Integer value) {
		if(value == null) {
			return null;
		}
		return SessionStatus.values()[value];
	}

	Integer mapSessionStatus(SessionStatus value) {
		if(value == null) {
			return null;
		}
		return value.ordinal();
	}

	PartialType mapPartialType(Integer value) {
		if(value == null) {
			return null;
		}
		return PartialType.values()[value];
	}

	Integer mapPartialType(PartialType value) {
		if(value == null) {
			return null;
		}
		return value.ordinal();
	}

	UserType mapUserType(Integer value) {
		if(value == null) {
			return null;
		}
		return UserType.values()[value];
	}

	Integer mapUserType(UserType value) {
		if(value == null) {
			return null;
		}
		return value.ordinal();
	}


	RankingFilter mapRankingFilter(Integer value) {
		if(value == null) {
			return null;
		}
		return RankingFilter.values()[value];
	}

	Integer mapRankingFilter(RankingFilter value) {
		if(value == null) {
			return null;
		}
		return value.ordinal();
	}

	RankingValue mapRankingValue(Integer value) {
		if(value == null) {
			return null;
		}
		return RankingValue.values()[value];
	}

	Integer mapRankingValue(RankingValue value) {
		if(value == null) {
			return null;
		}
		return value.ordinal();
	}

	RankingRepeat mapRankingRepeat(Integer value) {
		if(value == null) {
			return null;
		}
		return RankingRepeat.values()[value];
	}

	Integer mapRankingRepeat(RankingRepeat value) {
		if(value == null) {
			return null;
		}
		return value.ordinal();
	}

	OrganizationSetting mapOrganizationSetting(String value) {
		if(value == null) {
			return null;
		}
		return organizationSettingsService.findSettingById(value).orElse(null);
	}

	String mapOrganizationSetting(OrganizationSetting value) {
		if(value == null) {
			return null;
		}
		return value.getName();
	}

	RefundStatus mapRefundStatus(Integer value) {
		if(value == null) {
			return null;
		}
		return RefundStatus.values()[value];
	}

	Integer mapRefundStatus(RefundStatus value) {
		if(value == null) {
			return null;
		}
		return value.ordinal();
	}

	Category mapCategory(Long value) {
		if (value == null) {
			return null;
		}
		return vendorService.findCategoryById(value).orElse(null);
	}

	Long mapCategory(Category value) {
		if (value == null) {
			return null;
		}
		return value.getId();
	}

	AwardCustomIssuer mapAwardCustomAuthor(Integer value) {
		if(value == null) {
			return null;
		}
		return AwardCustomIssuer.values()[value];
	}

	Integer mapAwardCustomAuthor(AwardCustomIssuer value) {
		if(value == null) {
			return null;
		}
		return value.ordinal();
	}
}
