package net.nextome.lismove.repositories;

import net.nextome.lismove.models.Sensor;
import net.nextome.lismove.models.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SensorRepository extends CrudRepository<Sensor, Long> {

    List<Sensor> findByUser(User user);

	List<Sensor> findByUserUid(String uid);

	List<Sensor> findByUserUidAndEndAssociationIsNull(String uid);

	List<Sensor> findByUuidAndEndAssociationIsNull(String uuid);

	Optional<Sensor> findTopByUuidOrderByIdDesc(String uuid);

	@Query("select s from Sensor s where s.user = :user and s.startAssociation < :startAssociation and s.endAssociation > :endAssociation")
	Optional<Sensor> findActiveByUserAt(User user, LocalDateTime startAssociation, LocalDateTime endAssociation);

	Optional<Sensor> findTopByUuidAndWheelDiameterAndBikeTypeOrderByIdDesc(String uuid, BigDecimal wheelDiameter, String bikeType);
}