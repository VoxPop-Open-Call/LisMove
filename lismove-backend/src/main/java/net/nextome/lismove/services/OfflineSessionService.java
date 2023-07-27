package net.nextome.lismove.services;

import net.nextome.lismove.exceptions.LismoveException;
import net.nextome.lismove.models.OfflineSession;
import net.nextome.lismove.models.Sensor;
import net.nextome.lismove.models.Session;
import net.nextome.lismove.models.enums.SessionStatus;
import net.nextome.lismove.repositories.OfflineSessionRepository;
import net.nextome.lismove.services.utils.UtilitiesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
public class OfflineSessionService extends UtilitiesService {
    private static final int DEFAULT_START_SESSION_THRESHOLD = 10 * 60; //sec
    private static final int DEFUALT_END_SESSION_THRESHOLD = 10 * 60; //sec
    private static final double DEFAULT_DISTANCE_TOLERANCE = 0.5; //%

    private final Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    private OfflineSessionRepository offlineSessionRepository;
    @Autowired
    private SessionService sessionService;
    @Autowired
    private SensorService sensorService;

    public OfflineSession save(OfflineSession session) {
        return offlineSessionRepository.save(session);
    }

    public void receive(List<OfflineSession> sessions) {
        for (OfflineSession offline : sessions) {
            if (!offlineSessionRepository.findByStartTime(offline.getStartTime()).isPresent()) {
                save(offline);
                //Trova e associa il sensore dell'utente in questione attivo nel periodo di svolgimento della sessione offline
                Sensor sensor = sensorService.findActiveByUserAt(offline.getUser(), offline.getStartTime()).orElseThrow(() -> new LismoveException("Active sensor expected", HttpStatus.NOT_FOUND));
                List<Session> list = sessionService.findNotCertificatedByUserAndBetween(offline.getUser(), offline.getStartTime().minusSeconds(DEFAULT_START_SESSION_THRESHOLD), offline.getEndTime().plusSeconds(DEFUALT_END_SESSION_THRESHOLD));
                BigDecimal totalDistance = list.stream().map(Session::getNationalKm).reduce(BigDecimal.ZERO, BigDecimal::add);
                if (totalDistance.compareTo(BigDecimal.ZERO) > 0 && isInPercent(BigDecimal.valueOf(offline.getDistance()), totalDistance, DEFAULT_DISTANCE_TOLERANCE)) {
                    list.stream().filter(s -> !s.getCertificated()).forEach(session -> {
                        session.setCertificated(true);
                        session.setStatus(SessionStatus.VALID_OFFLINE);
                        sessionService.assignSensor(session, sensor);
                        sessionService.save(session);
                    });
                }
            }
        }
    }

    public List<OfflineSession> list() {
        return offlineSessionRepository.findAllByOrderByStartTimeDesc();
    }
}
