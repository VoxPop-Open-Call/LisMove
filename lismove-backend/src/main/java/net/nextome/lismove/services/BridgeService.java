package net.nextome.lismove.services;

import com.bugsnag.Bugsnag;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.nextome.lismove.exceptions.LismoveException;
import net.nextome.lismove.models.Session;
import net.nextome.lismove.models.User;
import net.nextome.lismove.rest.dto.SessionForwardDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.RoundingMode;
import java.time.LocalTime;
import java.time.ZoneOffset;

@Service
@Transactional
public class BridgeService {
	@Value("${bridge.base-url}")
	private String baseUrl;
	@Value("${bridge.bearer-token}")
	private String bearerToken;

	@Autowired
	private Bugsnag bugsnag;

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	public boolean forwardSession(Session session) {
		SessionForwardDto forward = new SessionForwardDto();
		forward.setSession_id("sessione_nuova_app_" + session.getId());
		forward.setUser_id(session.getUser().getOldUserId());
		forward.setKm_to_assign(session.getNationalKm().setScale(2, RoundingMode.HALF_UP).floatValue());
		forward.setCan_gain_urb_nat_points((short) 1);
		forward.setSession_type(session.getHomeWorkPath() ? 0 : 2);
		forward.setDuration(session.getDuration() <= 86399 ? LocalTime.ofSecondOfDay(session.getDuration()).toString() : "23:59:59");
		forward.setSession_timestamp_start(session.getStartTime().atOffset(ZoneOffset.UTC).toEpochSecond());
		forward.setSession_timestamp_end(session.getEndTime().atOffset(ZoneOffset.UTC).toEpochSecond());
		forward.setSend_notification((short) 0);
		try {
			String json = WebClient.create(baseUrl)
					.post()
					.uri("/create_manual_session")
					.body(Mono.just(forward), SessionForwardDto.class)
					.headers(h -> h.setBearerAuth(bearerToken))
					.retrieve()
//                    .onStatus(HttpStatus::is2xxSuccessful, clientResponse -> {
//                        logger.info("Session forwarding successful");
//                        return Mono.empty();
//                    })
//                    .onStatus(HttpStatus::is4xxClientError, clientResponse -> {
//                        logger.error("Session forwarding failed");
//                        bugsnag.notify(new LismoveException("Session forwarding failed"), report -> {
//                            User user = session.getUser();
//                            report.setUser(user.getUid(), user.getEmail(), user.getUsername());
//                            report.addToTab("Session info", "uuid", session.getId());
//                        });
//                        return Mono.empty();
//                    })
					.bodyToMono(String.class)
					.block();

			if(json != null && json.contains("\"success\":true")) {
				logger.info("Session forwarding successful");
			} else {
				ObjectMapper mapper = new ObjectMapper();
				ResponseError response = mapper.readValue(json, ResponseError.class);
				throw new LismoveException("Session forwarding failed: " + response.getData());
			}
			return true;
		} catch(Exception e) {
			logger.error(e.getMessage());
			bugsnag.notify(e, report -> {
				User user = session.getUser();
				report.setUser(user.getUid(), user.getEmail(), user.getUsername());
				report.addToTab("Session info", "uuid", session.getId());
			});
			return false;
		}
	}

	private static class ResponseError {
		private Boolean success;
		private String data;

		public Boolean getSuccess() {
			return success;
		}

		public void setSuccess(Boolean success) {
			this.success = success;
		}

		public String getData() {
			return data;
		}

		public void setData(String data) {
			this.data = data;
		}
	}

}
