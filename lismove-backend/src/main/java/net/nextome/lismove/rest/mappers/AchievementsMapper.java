package net.nextome.lismove.rest.mappers;

import net.nextome.lismove.models.Achievement;
import net.nextome.lismove.models.AchievementUser;
import net.nextome.lismove.models.User;
import net.nextome.lismove.rest.dto.AchievementDto;
import net.nextome.lismove.rest.dto.AchievementUserListDto;
import net.nextome.lismove.rest.dto.AchievementUserPositionDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Mapper(componentModel = "spring")
public abstract class AchievementsMapper extends UtilMapper {

	@Mapping(target = "organization", source = "organization.id")
	@Mapping(target = "organizationTitle", source = "organization.title")
	public abstract AchievementDto achievementToDto(Achievement achievement);

	public abstract List<AchievementDto> achievementToDto(List<Achievement> achievement);

	@Mapping(target = "organization.id", source = "organization")
	public abstract Achievement dtoToAchievement(AchievementDto dto);

	@Mapping(target = "organization", source = "achievement.organization.id")
	@Mapping(target = "organizationTitle", source = "achievement.organization.title")
	@Mapping(target = "user", source = "user.uid")
	@Mapping(target = "username", source = "user.username")
	@Mapping(target = "id", source = "achievement.id")
	@Mapping(target = "name", source = "achievement.name")
	@Mapping(target = "duration", source = "achievement.duration")
	@Mapping(target = "startDate", source = "achievement.startDate")
	@Mapping(target = "endDate", source = "achievement.endDate")
	@Mapping(target = "value", source = "achievement.value")
	@Mapping(target = "logo", source = "achievement.logo")
	@Mapping(target = "target", source = "achievement.target")
	public abstract AchievementDto achievementUserToDto(AchievementUser achievement);

	public abstract List<AchievementDto> achievementUserToDto(List<AchievementUser> achievement);

	@Mapping(target = "users", ignore = true)
	public abstract AchievementUserListDto achievementToAchievementUserListDto(Achievement achievement);

	@Mapping(target = "user", source = "user.uid")
	@Mapping(target = "username", source = "user.username")
	public abstract AchievementUserPositionDto userToAchievementUserPositionDto(AchievementUser achievementUser);

	public abstract List<AchievementUserPositionDto> userToAchievementUserPositionDto(List<AchievementUser> achievementUser);

}
