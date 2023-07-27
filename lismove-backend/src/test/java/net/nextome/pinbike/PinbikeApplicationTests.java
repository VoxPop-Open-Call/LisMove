package net.nextome.lismove;

import net.nextome.lismove.models.*;
import net.nextome.lismove.repositories.*;
import net.nextome.lismove.rest.dto.LismoverUserDto;
import net.nextome.lismove.rest.dto.SeatDto;
import net.nextome.lismove.services.AddressService;
import net.nextome.lismove.services.EmailService;
import net.nextome.lismove.services.UserService;
import net.nextome.lismove.services.utils.UtilitiesService;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.thymeleaf.context.Context;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application-integrationtest.properties")
class LismoveApplicationTests {

	@Autowired
	private TestRestTemplate template;
	@Autowired
	BuildProperties buildProperties;

	@Test
	public void networkTest() {
		ResponseEntity<String> result = template.getForEntity("/", String.class);
		assertAll(
				() -> assertEquals(HttpStatus.OK, result.getStatusCode()),
				() -> assertEquals("Server up - " + buildProperties.getVersion() + " test", result.getBody())
		);
	}

	@Test
	public void speedUtilTest() {
		assertEquals(
				BigDecimal.valueOf(15.6).setScale(3, RoundingMode.HALF_UP),
				UtilitiesService.speed(BigDecimal.valueOf(1300), 300)
		);
	}
}
