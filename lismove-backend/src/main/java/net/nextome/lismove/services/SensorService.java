package net.nextome.lismove.services;

import net.nextome.lismove.exceptions.LismoveException;
import net.nextome.lismove.models.Sensor;
import net.nextome.lismove.models.User;
import net.nextome.lismove.repositories.SensorRepository;
import net.nextome.lismove.services.utils.UtilitiesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class SensorService extends UtilitiesService {

	@Autowired
	private SensorRepository sensorRepository;

	public List<Sensor> findByUser(String uid, boolean onlyActive) {
		if(onlyActive) {
			return sensorRepository.findByUserUidAndEndAssociationIsNull(uid);
		} else {
			return sensorRepository.findByUserUid(uid);
		}
	}

	public Sensor save(Sensor s) {
		return sensorRepository.save(s);
	}

	public Sensor saveSensor(Sensor update) {
		checkStolenSensor(update);

		Sensor old = getActiveSensor(update.getUser()).orElse(null);
		Sensor sensor = new Sensor();
		if(old != null) {    //closing association with old sensor
			notNullBeanCopy(old, sensor, "id");
			old.setEndAssociation(LocalDateTime.now());
			sensorRepository.save(old);
		}
		if(old == null || !sensor.getUuid().equals(old.getUuid())) {    //brand new sensor
			sensor.setStartAssociation(LocalDateTime.now());
		}
		notNullBeanCopy(update, sensor);
		Optional<Sensor> lastWithSameWheelDiameterAndBikeType = sensorRepository.findTopByUuidAndWheelDiameterAndBikeTypeOrderByIdDesc(sensor.getUuid(), sensor.getWheelDiameter(),sensor.getBikeType());
		if(lastWithSameWheelDiameterAndBikeType.isPresent()){
			sensor.setHubCoefficient(lastWithSameWheelDiameterAndBikeType.get().getHubCoefficient());
		}else{
			if(update.getHubCoefficient() == null){
				sensor.setHubCoefficient(BigDecimal.ONE);
			}
		}
		sensorRepository.save(sensor);
		return sensor;
	}

	public void endSensorAssociation(String uuid) {
		Sensor sensor = sensorRepository.findTopByUuidOrderByIdDesc(uuid).orElseThrow(() -> new LismoveException("Sensore non trovato", HttpStatus.NOT_FOUND));
		sensor.setEndAssociation(LocalDateTime.now());
		sensorRepository.save(sensor);
	}

	public Sensor setStolen(String uuid) {
		Sensor sensor = sensorRepository.findTopByUuidOrderByIdDesc(uuid).orElseThrow(() -> new LismoveException("Sensore non trovato", HttpStatus.NOT_FOUND));
		sensor.setStolen(true);
		return sensorRepository.save(sensor);
	}

	public boolean isActiveSensor(Sensor sensor) {
		List<Sensor> list = sensorRepository.findByUuidAndEndAssociationIsNull(sensor.getUuid());
		return list.stream().anyMatch(s -> !s.getUser().getUid().equals(sensor.getUser().getUid()));
	}

	public Optional<Sensor> getActiveSensor(User u) {
		return sensorRepository.findByUser(u).stream().filter(s -> s.getEndAssociation() == null).findFirst();
	}

	public void checkStolenSensor(Sensor sensor) {
		sensorRepository.findTopByUuidOrderByIdDesc(sensor.getUuid()).ifPresent(s -> {
			if(s.getStolen() != null && s.getStolen() && !sensor.getUser().getUid().equals(s.getUser().getUid())) {
				throw new LismoveException("Il dispositivo risulta rubato", HttpStatus.BAD_REQUEST);
			}
		});
	}

	public Optional<Sensor> findActiveByUserAt(User user, LocalDateTime timestamp) {
		Optional<Sensor> sensor = sensorRepository.findActiveByUserAt(user, timestamp, timestamp);
		if (!sensor.isPresent()) {
			Optional<Sensor> temp = getActiveSensor(user);
			if (temp.isPresent() && temp.get().getStartAssociation().isBefore(timestamp)) {
				sensor = temp;
			}
		}
		return sensor;
	}
}
