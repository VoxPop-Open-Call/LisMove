package net.nextome.lismove;

import net.nextome.lismove.exceptions.LismoveException;
import net.nextome.lismove.models.*;
import net.nextome.lismove.repositories.*;
import net.nextome.lismove.rest.dto.LismoverUserDto;
import net.nextome.lismove.rest.dto.SeatDto;
import net.nextome.lismove.services.AddressService;
import net.nextome.lismove.services.UserService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.TestPropertySource;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Lismover create/update home/work addresses tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application-integrationtest.properties")
class AddressAndPathTests {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private HomeWorkPathRepository homeWorkPathRepository;
    @Autowired
    private AddressService addressService;
    @Autowired
    private UserService userService;

    private static String uid;
    private static Long sid1, sid2, sid3;

    @BeforeAll
    public static void setup(
            @Autowired CityRepository cityRepository,
            @Autowired SeatRepository seatRepository
    ) {
        cityRepository.save(Generator.generateCity(72006L,"Bari"));
        sid1 = seatRepository.save(Generator.generateSeat("Via Gorizia","13",cityRepository.findById(72006L).get(),41.11903092110878,16.884132714247794)).getId();
        sid2 = seatRepository.save(Generator.generateSeat("Via Nizza","37",cityRepository.findById(72006L).get(),41.115091098768396,16.876330421724223)).getId();
        sid3 = seatRepository.save(Generator.generateSeat("Via Pasubio","194",cityRepository.findById(72006L).get(),41.11046188026098,16.867014541487347)).getId();
    }

    @Test
    @Order(0)
    public void createUserWithHomeAddressManualCoordinatesTest() {
        LismoverUserDto u1 = Generator.generateLismoverDto("mrossi0", "test0@test.test");
        u1.setHomeLatitude(41.12209);
        u1.setHomeLongitude(16.87197);
        userService.createLismover(u1).getUid();

        Optional<User> u = userRepository.findByUsername("mrossi0");
        assertAll(
                () -> assertTrue(u.isPresent()),
                () -> assertEquals("Via Dante Alighieri, 5, 701xx Bari BA", u.get().getHomeAddress().formatAddress()),
                () -> assertEquals(41.12209, u.get().getHomeAddress().getLatitude()),
                () -> assertEquals(16.87197, u.get().getHomeAddress().getLongitude())

        );
    }

    @Test
    @Order(1)
    public void createUserWithHomeAddressTest() {
        uid = userService.createLismover(Generator.generateLismoverDto("mrossi", "test@test.test")).getUid();

        Optional<User> u = userRepository.findByUid(uid);
        assertAll(
                () -> assertTrue(u.isPresent()),
                () -> assertEquals("Via Dante Alighieri, 5, 701xx Bari BA", u.get().getHomeAddress().formatAddress())
        );
    }

    @Test
    @Order(2)
    public void saveUserWorkAddressesTest() {
        LismoverUserDto upd = new LismoverUserDto();
        SeatDto work;
        upd.setWorkAddresses(new HashSet<>());

        work = new SeatDto();
        work.setId(sid1);
        upd.getWorkAddresses().add(work);
        work = new SeatDto();
        work.setId(sid2);
        upd.getWorkAddresses().add(work);

        userService.updateLismover(userService.findByUid(uid).get(), upd);

        Optional<User> u = userRepository.findByUid(uid);
        assertTrue(u.isPresent());
        Set<WorkAddress> w = addressService.getActiveWorkAddresses(u.get());
        Set<Long> wl = w.stream().map(address -> address.getSeat().getId()).collect(Collectors.toSet());
        Set<HomeWorkPath> p = homeWorkPathRepository.findByUser(u.get());
        assertAll(
                () -> assertEquals(2, w.size()),
                () -> assertEquals(2, p.size()),
//                    Checks if paths are referencing the active home address
                () -> assertTrue(p.stream().allMatch(homeWorkPath -> u.get().getHomeAddress().formatAddress().equals(homeWorkPath.getHomeAddress().formatAddress()))),
//                    Checks if paths are referencing active work addresses
                () -> assertTrue(p.stream().allMatch(homeWorkPath -> wl.contains(homeWorkPath.getSeat().getId())))
        );
    }

    @Test
    @Order(3)
    public void updateUserHomeAddressTest() {
        LismoverUserDto upd = new LismoverUserDto();
        upd.setHomeAddress("Via Dante Alighieri");
        upd.setHomeNumber("10");
        upd.setHomeCity(72006L);

        userService.updateLismover(userService.findByUid(uid).get(), upd);

        Optional<User> u = userRepository.findByUid(uid);
        assertTrue(u.isPresent());
        List<HomeAddress> h = addressService.getHomeAddressesHistory(u.get());
        Set<WorkAddress> w = addressService.getActiveWorkAddresses(u.get());
        Set<Long> wl = w.stream().map(address -> address.getSeat().getId()).collect(Collectors.toSet());
        Set<HomeWorkPath> p = homeWorkPathRepository.findByUser(u.get());
        assertAll(
                () -> assertEquals(2, h.size()),
                () -> assertEquals(2, p.size()),
//                    Checks if active home path has been updated
                () -> assertEquals("Via Dante Alighieri, 10, 701xx Bari BA", u.get().getHomeAddress().formatAddress()),
//                    Checks if paths are referencing the active home address
                () -> assertTrue(p.stream().allMatch(homeWorkPath -> u.get().getHomeAddress().formatAddress().equals(homeWorkPath.getHomeAddress().formatAddress()))),
//                    Checks if paths are referencing active work addresses
                () -> assertTrue(p.stream().allMatch(homeWorkPath -> wl.contains(homeWorkPath.getSeat().getId())))
        );
    }

    @Test
    @Order(4)
    public void updateUserWorkAddressesTest() {
        LismoverUserDto upd = new LismoverUserDto();
        SeatDto work = new SeatDto();
        upd.setWorkAddresses(new HashSet<>());

        work.setId(sid3);
        upd.getWorkAddresses().add(work);

        userService.updateLismover(userService.findByUid(uid).get(), upd);

        Optional<User> u = userRepository.findByUid(uid);
        assertTrue(u.isPresent());
        Set<WorkAddress> w = addressService.getActiveWorkAddresses(u.get());
        Set<Long> wl = w.stream().map(address -> address.getSeat().getId()).collect(Collectors.toSet());
        Set<HomeWorkPath> p = homeWorkPathRepository.findByUser(u.get());
        assertAll(
                () -> assertEquals(1, w.size()),
                () -> assertEquals(1, p.size()),
                () -> assertEquals(3, addressService.getWorkAddressesHistory(u.get()).size()),
//                    Checks if paths are referencing the active home address
                () -> assertTrue(p.stream().allMatch(homeWorkPath -> u.get().getHomeAddress().formatAddress().equals(homeWorkPath.getHomeAddress().formatAddress()))),
//                    Checks if paths are referencing active work addresses
                () -> assertTrue(p.stream().allMatch(homeWorkPath -> wl.contains(homeWorkPath.getSeat().getId())))
        );
    }

    @Test
    @Order(5)
    public void updateUsernameExceptionTest() {
        LismoverUserDto user = new LismoverUserDto();
        user.setUsername("mrossi");

        assertThrows(LismoveException.class, () -> userService.createLismover(user));
    }

    @Test
    @Order(5)
    public void updateUidExceptionTest() {
        LismoverUserDto user = new LismoverUserDto();
        user.setUid(uid);

        assertThrows(LismoveException.class, () -> userService.createLismover(user));
    }
}
