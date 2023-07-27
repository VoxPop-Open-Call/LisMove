package net.nextome.lismove;

import net.nextome.lismove.exceptions.LismoveException;
import net.nextome.lismove.models.Sensor;
import net.nextome.lismove.models.User;
import net.nextome.lismove.repositories.SensorRepository;
import net.nextome.lismove.repositories.UserRepository;
import net.nextome.lismove.services.SensorService;
import net.nextome.lismove.services.UserService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Sensor saving tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application-integrationtest.properties")
class SensorTests {

	@Autowired
	private SensorRepository sensorRepository;
	@Autowired
	private SensorService sensorService;

	private static User user1, user2;

	@BeforeAll
	public static void setup(
			@Autowired UserRepository userRepository
	) {
		user1 = userRepository.save(Generator.generateUser());
		user2 = userRepository.save(Generator.generateUser());
	}

	@Test
	@Order(1)
	@Rollback(false)
	public void firstSensorTest() {
		Sensor sensor = new Sensor();

		sensor.setUuid("100");
		sensor.setFirmware("v1");
		sensor.setBikeType("bici");
		sensor.setWheelDiameter(BigDecimal.valueOf(20));
		sensor.setUser(user1);

		sensorService.saveSensor(sensor);
		Optional<Sensor> result = sensorService.getActiveSensor(user1);
		assertAll(
				() -> assertTrue(result.isPresent()),
				() -> assertNull(result.get().getEndAssociation()),
				() -> assertEquals(sensor.getUuid(), result.get().getUuid())
		);
	}

	@Test
	@Order(2)
	@Rollback(false)
	public void updateSensorTest() {
		Sensor sensor = new Sensor();

		sensor.setUuid("100");
		sensor.setFirmware("v2");
		sensor.setUser(user1);

		sensorService.saveSensor(sensor);
		Optional<Sensor> result = sensorService.getActiveSensor(user1);
		assertAll(
				() -> assertTrue(result.isPresent()),
				() -> assertEquals(2, sensorRepository.findByUserUid(user1.getUid()).stream().filter((s -> s.getUuid().equals(sensor.getUuid()))).count()),
				() -> assertEquals(BigDecimal.valueOf(20).setScale(5), result.get().getWheelDiameter()),
				() -> assertEquals("bici", result.get().getBikeType())
		);
	}

	@Test
	@Order(4)
	@Rollback(false)
	public void newSensorTest() {
		Sensor sensor = new Sensor();

		sensor.setUuid("101");
		sensor.setFirmware("v1");
		sensor.setBikeType("altra bici");
		sensor.setWheelDiameter(BigDecimal.valueOf(25));
		sensor.setUser(user1);
		sensor.setStolen(true);

		sensorService.saveSensor(sensor);
		Optional<Sensor> result = sensorService.getActiveSensor(user1);
		assertAll(
				() -> assertTrue(result.isPresent()),
				() -> assertEquals("101", sensorService.getActiveSensor(user1).get().getUuid()),
				() -> assertEquals(3, sensorRepository.findByUserUid(user1.getUid()).stream().count())
		);
	}

	@Test
	@Order(5)
	@Rollback()
	public void stolenSensorTest() {
		Sensor sensor = new Sensor();

		sensor.setUuid("101");
		sensor.setFirmware("v1");
		sensor.setBikeType("altra bici");
		sensor.setWheelDiameter(BigDecimal.valueOf(25));
		sensor.setUser(user2);

		assertThrows(LismoveException.class, () -> sensorService.saveSensor(sensor));
	}

	//TODO: test per parziali di debug
}
