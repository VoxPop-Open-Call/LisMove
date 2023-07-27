package net.nextome.lismove.services;

import com.bugsnag.Bugsnag;
import net.nextome.lismove.models.AwardAchievementUser;
import net.nextome.lismove.models.LogWall;
import net.nextome.lismove.models.Session;
import net.nextome.lismove.models.SessionPoint;
import net.nextome.lismove.models.enums.AwardType;
import net.nextome.lismove.models.enums.LogWallTemplate;
import net.nextome.lismove.models.enums.SessionType;
import net.nextome.lismove.repositories.LogWallRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class LogWallService {

	@Autowired
	private LogWallRepository logWallRepository;
	@Autowired
	private SessionService sessionService;
	@Autowired
	private Bugsnag bugsnag;
	DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	public List<String> generateLogWall() {
		List<String> logwall = new LinkedList<>();
		logWallRepository.findAll(Sort.by(Sort.Direction.DESC, "ts")).forEach(lw -> {
			logwall.add(String.format(lw.getTemplate().getText(), lw.getParams().split("\\|")));
		});
		return logwall;
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void writeLog(Session s) {
		try {
			if(s.getValid() && s.getNationalKm().compareTo(BigDecimal.ZERO) > 0) {
				if(s.getSessionPoints() != null && !s.getSessionPoints().isEmpty()) {
					for(int i = 0; i < s.getSessionPoints().size(); i++) {
						SessionPoint point = s.getSessionPoints().get(i);
						LogWallTemplate template;
						if(s.getHomeWorkPath() && i == 0) {
							template = LogWallTemplate.SESSION_INIZIATIVE_HOMEWORK;
						} else {
							template = LogWallTemplate.SESSION_INIZIATIVE;
						}
						logWallRepository.save(new LogWall(s.getEndTime().toInstant(ZoneOffset.UTC).toEpochMilli(), template,
								String.join("|",
										s.getEndTime().atZone(ZoneId.systemDefault()).withZoneSameInstant(ZoneId.of("Europe/Rome")).format(formatter),
										s.getUser().getUsername(),
										point.getOrganization().getTitle(),
										Optional.ofNullable(s.getType()).orElse(SessionType.BIKE).getName(),
										point.getDistance().setScale(2, RoundingMode.FLOOR).toPlainString(),
										point.getPoints().toPlainString(),
										s.getNationalPoints().toPlainString()
								)));
					}
				} else {
					logWallRepository.save(new LogWall(s.getEndTime().toInstant(ZoneOffset.UTC).toEpochMilli(), LogWallTemplate.SESSION,
							String.join("|",
									s.getEndTime().atZone(ZoneId.systemDefault()).withZoneSameInstant(ZoneId.of("Europe/Rome")).format(formatter),
									s.getUser().getUsername(),
									Optional.ofNullable(s.getType()).orElse(SessionType.BIKE).getName(),
									s.getNationalKm().setScale(2, RoundingMode.FLOOR).toPlainString(),
									s.getNationalPoints().toPlainString()
							)));
				}
			}
		} catch(RuntimeException e) {
			bugsnag.notify(e);
		}
	}

	@Transactional
	public void writeLog(AwardAchievementUser a) {
		try {
			LogWallTemplate template;
			if(a.getAchievementUser().getFullfilled() && a.getAwardAchievement().getType().equals(AwardType.POINTS)) {
				template = LogWallTemplate.ACHIEVEMENT_POINTS;
			} else if(a.getAchievementUser().getFullfilled() && a.getAwardAchievement().getType().equals(AwardType.MONEY)) {
				template = LogWallTemplate.ACHIEVEMENT_EURO;
			} else return;
			logWallRepository.save(new LogWall(a.getTimestamp().toInstant(ZoneOffset.UTC).toEpochMilli(), template,
					String.join("|",
							a.getTimestamp().atZone(ZoneId.systemDefault()).withZoneSameInstant(ZoneId.of("Europe/Rome")).format(formatter),
							a.getAchievementUser().getUser().getUsername(),
							a.getAchievementUser().getAchievement().getOrganization().getTitle(),
							a.getAchievementUser().getAchievement().getName(),
							a.getAwardAchievement().getValue().setScale(2, RoundingMode.FLOOR).toPlainString()
					)));
		} catch(RuntimeException e) {
			bugsnag.notify(e);
		}
	}

	public void regenerateLogwall() {
		logWallRepository.deleteAll();
		sessionService.findAll(null).forEach(s -> {
			if(s.getValid()) {
				try {
					writeLog(s);
				} catch(Exception e) {
					logger.error(e.getMessage());
				}
			}
		});
	}
}
