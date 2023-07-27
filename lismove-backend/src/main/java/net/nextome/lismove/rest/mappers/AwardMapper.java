package net.nextome.lismove.rest.mappers;

import net.nextome.lismove.models.*;
import net.nextome.lismove.rest.dto.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Mapper(componentModel = "spring")
@Service
public abstract class AwardMapper extends UtilMapper {
	@Autowired
	private CouponMapper couponMapper;

	// AwardAchievement mappers
	@Mapping(target = "achievement.id", source = "achievement")
	public abstract AwardAchievement dtoToAwardAchievement(AwardAchievementDto dto);

	@Mapping(target = "achievement", source = "achievement.id")
	public abstract AwardAchievementDto awardAchievementToDto(AwardAchievement awardAchievement);

	public abstract List<AwardAchievementDto> awardAchievementToDto(List<AwardAchievement> awardRanking);

	@Mapping(target = "name", source = "awardAchievement.name")
	@Mapping(target = "description", source = "awardAchievement.description")
	@Mapping(target = "value", source = "awardAchievement.value")
	@Mapping(target = "type", source = "awardAchievement.type")
	@Mapping(target = "imageUrl", source = "awardAchievement.imageUrl")
	@Mapping(target = "achievementId", source = "awardAchievement.id")
	@Mapping(target = "organizationId", source = "awardAchievement.achievement.organization.id")
	public abstract AwardDto awardAchievementToAwardDto(AwardAchievementUser awardAchievements);

	public abstract List<AwardDto> awardAchievementToAwardDto(List<AwardAchievementUser> awardAchievements);

	// AwardPosition mappers
	public abstract AwardPosition dtoToAwardPosition(AwardPositionDto dto);

	@Mapping(target = "organization", source = "organization.id")
	@Mapping(target = "uid", ignore = true)
	@Mapping(target = "username", ignore = true)
	public abstract AwardPositionDto awardPositionToDto(AwardPosition awardPosition);

	public abstract List<AwardPositionDto> awardPositionToDto(List<AwardPosition> awardPosition);

	@Mapping(target = "name", source = "awardPosition.name")
	@Mapping(target = "description", source = "awardPosition.description")
	@Mapping(target = "value", source = "awardPosition.value")
	@Mapping(target = "type", source = "awardPosition.type")
	@Mapping(target = "imageUrl", source = "awardPosition.imageUrl")
	@Mapping(target = "latitude", source = "awardPosition.latitude")
	@Mapping(target = "longitude", source = "awardPosition.longitude")
	@Mapping(target = "address", source = "awardPosition.address")
	@Mapping(target = "number", source = "awardPosition.number")
	@Mapping(target = "city", source = "awardPosition.city")
	@Mapping(target = "radius", source = "awardPosition.radius")
	@Mapping(target = "startDate", source = "awardPosition.startDate")
	@Mapping(target = "endDate", source = "awardPosition.endDate")
	@Mapping(target = "organizationId", source = "awardPosition.organization.id")
	public abstract AwardDto awardPositionToAwardDto(AwardPositionUser awardPositions);

	public abstract List<AwardDto> awardPositionToAwardDto(List<AwardPositionUser> awardPositions);

	// AwardRanking mappers
	@Mapping(target = "ranking.id", source = "ranking")
	public abstract AwardRanking dtoToAwardRanking(AwardRankingDto dto);

	@Mapping(target = "ranking", source = "ranking.id")
	@Mapping(target = "uid", source = "user.uid")
	public abstract AwardRankingDto awardRankingToDto(AwardRanking awardRanking);

	public abstract List<AwardRankingDto> awardRankingToDto(List<AwardRanking> awardRanking);

	@Mapping(target = "rankingId", source = "ranking.id")
	@Mapping(target = "organizationId", source = "ranking.organization.id")
	public abstract AwardDto awardRankingToAwardDto(AwardRanking awardRankings);

	public abstract List<AwardDto> awardRankingToAwardDto(List<AwardRanking> awardRankings);

	// AwardCustom mappers
	public abstract AwardCustom dtoToAwardCustom(AwardCustomDto dto);

	@Mapping(target = "uid", ignore = true)
	public abstract AwardCustomDto awardCustomToDto(AwardCustom awardCustom);

	public abstract List<AwardCustomDto> awardCustomToDto(List<AwardCustom> awardCustom);

	@Mapping(target = "name", source = "awardCustom.name")
	@Mapping(target = "description", source = "awardCustom.description")
	@Mapping(target = "value", source = "awardCustom.value")
	@Mapping(target = "type", source = "awardCustom.type")
	@Mapping(target = "imageUrl", source = "awardCustom.imageUrl")
	@Mapping(target = "winningsAllowed", source = "awardCustom.winningsAllowed")
	@Mapping(target = "organization", source = "awardCustom.organization.id")
	@Mapping(target = "issuer", source = "awardCustom.issuer")
	@Mapping(target = "uid", source = "user.uid")
	public abstract AwardCustomDto awardCustomUserToDto(AwardCustomUser awardCustomUser);

	@Mapping(target = "name", source = "awardCustom.name")
	@Mapping(target = "description", source = "awardCustom.description")
	@Mapping(target = "value", source = "awardCustom.value")
	@Mapping(target = "type", source = "awardCustom.type")
	@Mapping(target = "imageUrl", source = "awardCustom.imageUrl")
	@Mapping(target = "organizationId", source = "awardCustom.organization.id")
	@Mapping(target = "issuer", source = "awardCustom.issuer")
	public abstract AwardDto awardCustomToAwardDto(AwardCustomUser awardCustom);

	public abstract List<AwardDto> awardCustomToAwardDto(List<AwardCustomUser> awardCustoms);

	CouponDto mapCoupon(Coupon value) {
		if(value == null) {
			return null;
		}
		return couponMapper.couponToDto(value);
	}
}