package net.nextome.lismove.rest;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import io.swagger.annotations.ApiOperation;
import net.nextome.lismove.exceptions.LismoveException;
import net.nextome.lismove.models.*;
import net.nextome.lismove.models.enums.OrganizationType;
import net.nextome.lismove.rest.dto.*;
import net.nextome.lismove.rest.mappers.*;
import net.nextome.lismove.security.NextomeUserDetails;
import net.nextome.lismove.services.*;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("organizations")
public class OrganizationController {

    @Autowired
    private OrganizationService organizationService;
    @Autowired
    private OrganizationSettingsService organizationSettingsService;
    @Autowired
    private AddressService addressService;
    @Autowired
    private UserService userService;
    @Autowired
    private RankingService rankingService;
    @Autowired
    private CustomFieldService customFieldService;
    @Autowired
    private AchievementService achievementService;
    @Autowired
    private AwardService awardService;
    @Autowired
    private NotificationMessageService notificationMessageService;
    @Autowired
    private OrganizationMapper organizationMapper;
    @Autowired
    private SeatMapper seatMapper;
    @Autowired
    private EnrollmentMapper enrollmentMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private RankingMapper rankingMapper;
    @Autowired
    private AchievementsMapper achievementsMapper;
    @Autowired
    private AwardMapper awardMapper;
    @Autowired
    private NotificationMessageMapper notificationMessageMapper;
    @Autowired
    private SessionMapper sessionMapper;
    @Autowired
    private CouponService couponService;
    @Autowired
    private CouponMapper couponMapper;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @GetMapping("{oid}")
    public OrganizationDto get(@PathVariable Long oid) {
        Organization org = organizationService.findById(oid).orElseThrow(() -> new LismoveException("Organization not found", HttpStatus.NOT_FOUND));
        return organizationMapper.organizationToDto(org);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public List<OrganizationOverviewDto> getAllOrganizations(@AuthenticationPrincipal @ApiIgnore NextomeUserDetails userDetails) {
        User u = userDetails.getUserData();
        ArrayList<Organization> list = new ArrayList<>();
        switch (u.getUserType()) {
            case ROLE_ADMIN:
                list.addAll(organizationService.getAll());
                break;
            case ROLE_MANAGER:
                organizationService.findById(u.getOrganization().getId()).ifPresent(list::add);
                break;
        }
        return organizationMapper.organizationToOverviewDto(list);
    }

    @PostMapping
    public OrganizationDto create(@RequestBody OrganizationDto organization) {
        if (organization.getType() == null) {
            throw new LismoveException("Missing type", HttpStatus.BAD_REQUEST);
        }
        if (organization.getTitle() == null) {
            throw new LismoveException("Missing title", HttpStatus.BAD_REQUEST);
        }
        return organizationMapper.organizationToDto(organizationService.create(organization));
    }

    @PutMapping("{oid}")
    public OrganizationDto update(@PathVariable Long oid, @RequestBody OrganizationDto organizationDto) {
        Organization old = organizationService.findById(oid).orElseThrow(() -> new LismoveException("Organization not found", HttpStatus.NOT_FOUND));
        old = organizationService.update(old, organizationDto);
        return organizationMapper.organizationToDto(old);
    }

    @DeleteMapping("{oid}")
    public String delete(@PathVariable Long oid) {
        logger.info("Delete organization {}", oid);
        organizationService.findById(oid).orElseThrow(() -> new LismoveException("Organization not found", HttpStatus.NOT_FOUND));
        organizationService.delete(oid);
        return "Deleted";
    }

    @PostMapping("{oid}/codes")
    public List<EnrollmentDto> generateCodes(@PathVariable Long oid, @RequestBody EnrollmentCodeGenerator codeGenerator) {
        logger.info("Generate {} codes for organization {}", codeGenerator.getN(), oid);
        Organization o = organizationService.findById(oid).orElseThrow(() -> new LismoveException("Organization not found", HttpStatus.NOT_FOUND));
        return enrollmentMapper.enrollmentToDto(organizationService.generateCodes(codeGenerator, o));
    }

    @GetMapping("{oid}/codes")
    public List<EnrollmentDto> getCodes(@PathVariable Long oid) {
        Organization org = organizationService.findById(oid).orElseThrow(() -> new LismoveException("Organization not found", HttpStatus.NOT_FOUND));
        return enrollmentMapper.enrollmentToDto(new ArrayList<>(org.getEnrollments()));
    }

    @PutMapping("{oid}/codes")
    public List<EnrollmentDto> editCodes(@PathVariable Long oid, @RequestBody EnrollmentCodeEditor codeEditor) {
        Organization o = organizationService.findById(oid).orElseThrow(() -> new LismoveException("Organization not found", HttpStatus.NOT_FOUND));
        return enrollmentMapper.enrollmentToDto(organizationService.editCodes(codeEditor, o));
    }

    //	Seats APIs
    @GetMapping("{oid}/seats")
    public Set<SeatDto> getSeats(@PathVariable Long oid) {
        Organization org = organizationService.findById(oid).orElseThrow(() -> new LismoveException("Organization not found", HttpStatus.NOT_FOUND));
        if (org.getType().equals(OrganizationType.PA)) {
            return new HashSet<>();
        } else {
            return seatMapper.seatToDto(addressService.findAllSeatsByOrganization(org));
        }
    }

    @ApiOperation(value = "createSeat", notes = "Aggiunge sedi per qualsiasi tipo di Organization (COMPANY, PA con o senza validazione attiva). Le sedi aggiunte sono già segnate come validated")
    @PostMapping("{oid}/seats")
    public SeatDto createSeat(@PathVariable Long oid, @RequestBody SeatDto dto) {
        Organization org = organizationService.findById(oid).orElseThrow(() -> new LismoveException("Organization not found", HttpStatus.NOT_FOUND));
        return seatMapper.seatToDto(addressService.createSeat(org, dto));
    }

    @PutMapping("{oid}/seats/{sid}")
    public SeatDto updateSeat(@PathVariable Long oid, @PathVariable Long sid, @RequestBody SeatDto dto) {
        Seat old = addressService.findSeatById(sid).orElseThrow(() -> new LismoveException("Seat not found", HttpStatus.NOT_FOUND));
        if (oid.equals(old.getOrganization().getId())) {
            return seatMapper.seatToDto(addressService.updateSeat(old, dto));
        }
        return null;
    }

    @DeleteMapping("{oid}/seats/{sid}")
    public String deleteSeat(@PathVariable Long oid, @PathVariable Long sid) {
        Seat seat = addressService.findSeatById(sid).orElseThrow(() -> new LismoveException("Seat not found", HttpStatus.NOT_FOUND));
        if (oid.equals(seat.getOrganization().getId())) {
            addressService.deleteSeat(seat);
            return "Deleted";
        }
        return "";
    }

    @GetMapping("{oid}/seats/{sid}")
    public String validateSeat(@PathVariable Long oid, @PathVariable Long sid, @RequestParam String token) {
        Seat seat = addressService.findSeatById(sid).orElseThrow(() -> new LismoveException("Seat not found", HttpStatus.NOT_FOUND));
        Algorithm algorithm = Algorithm.HMAC256(AddressService.SECRET_JWT_KEY);
        JWTVerifier verifier = JWT.require(algorithm).build();
        DecodedJWT jwt = verifier.verify(token);
        if (!jwt.getClaim("seat").asLong().equals(seat.getId())) {
            throw new LismoveException("Seat doesn't match", HttpStatus.BAD_REQUEST);
        }
        if (!seat.getOrganization().getId().equals(oid)) {
            throw new LismoveException("Organization doesn't match", HttpStatus.BAD_REQUEST);
        }
        if (seat.getOrganization().getType().equals(OrganizationType.PA) && seat.getOrganization().getValidation()) {
            if (jwt.getClaim("validate").asBoolean()) {
                addressService.approveSeat(seat);
                return "Validated";
            } else {
                addressService.rejectSeat(seat);
                return "Rejected";
            }
        } else {
            throw new LismoveException("Seat validation not enabled for this organization", HttpStatus.BAD_REQUEST);
        }
    }

    //	Manager APIs
    @PostMapping("{oid}/users")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MANAGER') and @authorizationService.belongsToOrganization(#userDetails, #oid)")
    public ManagerDto createManager(@PathVariable Long oid, @RequestBody ManagerDto dto, @AuthenticationPrincipal @ApiIgnore NextomeUserDetails userDetails) {
        if (dto.getPassword() == null) {
            throw new LismoveException("Missing password", HttpStatus.BAD_REQUEST);
        }
        Organization org = organizationService.findById(oid).orElseThrow(() -> new LismoveException("Organization not found", HttpStatus.NOT_FOUND));
        return userMapper.userToManagerDto(organizationService.createManager(dto, org));
    }

    @PutMapping("{oid}/users/{uid}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MANAGER') and @authorizationService.belongsToOrganization(#userDetails, #oid)")
    public ManagerDto updateManager(@PathVariable Long oid, @PathVariable String uid, @RequestBody ManagerDto dto, @AuthenticationPrincipal @ApiIgnore NextomeUserDetails userDetails) {
        organizationService.findById(oid).orElseThrow(() -> new LismoveException("Organization not found", HttpStatus.NOT_FOUND));
        User user = userService.findByUid(uid).orElseThrow(() -> new LismoveException("User not found", HttpStatus.NOT_FOUND));
        if (!user.getOrganization().getId().equals(oid)) {
            throw new LismoveException("Accesso negato", HttpStatus.FORBIDDEN);
        }
        return userMapper.userToManagerDto(organizationService.updateManager(user, dto));
    }

    @GetMapping("{oid}/users")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MANAGER') and @authorizationService.belongsToOrganization(#userDetails, #oid)")
    public Set<ManagerDto> getManagers(@PathVariable Long oid, @AuthenticationPrincipal @ApiIgnore NextomeUserDetails userDetails) {
        Organization org = organizationService.findById(oid).orElseThrow(() -> new LismoveException("Organization not found", HttpStatus.NOT_FOUND));
        return userMapper.userToManagerDto(organizationService.findAllManagers(org));
    }

    //	Rankings
    @GetMapping("{oid}/rankings")
    public List<RankingDto> getRankings(@PathVariable Long oid, @RequestParam(required = false, defaultValue = "false") Boolean active) {
        Organization org = organizationService.findById(oid).orElseThrow(() -> new LismoveException("Organization not found", HttpStatus.NOT_FOUND));
        if (active) {
            return rankingMapper.rankingToDto(rankingService.findActiveByOrganization(org));
        } else {
            return rankingMapper.rankingToDto(rankingService.findAllByOrganization(org));
        }
    }

    //Custom Fields
    @GetMapping("{oid}/custom-fields")
    public List<CustomFieldDto> getCustomFields(@PathVariable Long oid) {
        Organization org = organizationService.findById(oid).orElseThrow(() -> new LismoveException("Organization not found", HttpStatus.NOT_FOUND));
        List<CustomField> customFields = customFieldService.findByOrganization(org);
        return organizationMapper.customFieldToDto(customFields);
    }

    @PostMapping("{oid}/custom-fields")
    public CustomFieldDto createCustomField(@PathVariable Long oid, @RequestBody CustomFieldDto dto) {
        organizationService.findById(oid).orElseThrow(() -> new LismoveException("Organization not found", HttpStatus.NOT_FOUND));
        if (dto.getOrganization() != null && !dto.getOrganization().equals(oid)) {
            throw new LismoveException("Forbidden", HttpStatus.FORBIDDEN);
        }
        dto.setOrganization(oid);
        return organizationMapper.customFieldToDto(customFieldService.save(organizationMapper.dtoToCustomField(dto)));
    }

    @PutMapping("{oid}/custom-fields/{fid}")
    public CustomFieldDto updateCustomField(@PathVariable Long oid, @PathVariable Long fid, @RequestBody CustomFieldDto dto) {
        organizationService.findById(oid).orElseThrow(() -> new LismoveException("Organization not found", HttpStatus.NOT_FOUND));
        if (dto.getOrganization() != null && !dto.getOrganization().equals(oid)) {
            throw new LismoveException("Forbidden", HttpStatus.FORBIDDEN);
        }
        CustomField old = customFieldService.findById(fid).orElseThrow(() -> new LismoveException("Custom Field not found", HttpStatus.NOT_FOUND));
        return organizationMapper.customFieldToDto(customFieldService.update(old, dto));
    }

    @DeleteMapping("{oid}/custom-fields/{fid}")
    public String deleteCustomField(@PathVariable Long oid, @PathVariable Long fid) {
        organizationService.findById(oid).orElseThrow(() -> new LismoveException("Organization not found", HttpStatus.NOT_FOUND));
        CustomField customField = customFieldService.findById(fid).orElseThrow(() -> new LismoveException(("Custom Field not found"), HttpStatus.NOT_FOUND));
        try {
            customFieldService.delete(customField);
        } catch (DataIntegrityViolationException e) {
            throw new LismoveException(e.getMessage());
        }
        return "Deleted";
    }

    @GetMapping("{oid}/custom-field-values")
    public List<CustomFieldValueDto> getCustomFieldValues(@PathVariable Long oid, @RequestParam Long eid) {
        organizationService.findById(oid).orElseThrow(() -> new LismoveException("Organization not found", HttpStatus.NOT_FOUND));
        Enrollment enrollment = organizationService.findEnrollmentById(eid).orElseThrow(() -> new LismoveException("Enrollment not found", HttpStatus.NOT_FOUND));
        if (!oid.equals(enrollment.getOrganization().getId())) {
            throw new LismoveException("Enrollment owned by another organization", HttpStatus.FORBIDDEN);
        }
        return organizationMapper.customFieldValueToDto(customFieldService.findByEnrollment(enrollment));
    }

    @PostMapping("{oid}/custom-field-values")
    @ApiOperation(value = "createCustomFieldValue", notes = "L'API può essere usata per creare e aggiornare i Custom Field Values. Non è richiesto valorizzare l'id.")
    public CustomFieldValueDto saveCustomFieldValue(@PathVariable Long oid, @RequestBody CustomFieldValueDto dto) {
        Organization org = organizationService.findById(oid).orElseThrow(() -> new LismoveException("Organization not found", HttpStatus.NOT_FOUND));
        CustomField customField = customFieldService.findById(dto.getCustomField()).orElseThrow(() -> new LismoveException("Custom Field not found", HttpStatus.NOT_FOUND));
        Enrollment enrollment = organizationService.findEnrollmentById(dto.getEnrollment()).orElseThrow(() -> new LismoveException("Enrollment not found", HttpStatus.NOT_FOUND));
        if (!enrollment.getOrganization().getId().equals(org.getId())) {
            throw new LismoveException("Enrollment owned by another organization", HttpStatus.FORBIDDEN);
        }
        if (!customField.getOrganization().getId().equals(org.getId())) {
            throw new LismoveException("Custom Field owned by another organization", HttpStatus.FORBIDDEN);
        }

        Optional<CustomFieldValue> value = customFieldService.findValueByEnrollmentAndCustomField(enrollment, customField);
        if (value.isPresent()) {
            return organizationMapper.customFieldValueToDto(customFieldService.update(value.get(), dto.getValue()));
        } else {
            return organizationMapper.customFieldValueToDto(customFieldService.save(organizationMapper.dtoToCustomFieldValue(dto)));
        }
    }

    @DeleteMapping("{oid}/custom-field-values/{vid}")
    public String deleteCustomFieldValue(@PathVariable Long oid, @PathVariable Long vid) {
        organizationService.findById(oid).orElseThrow(() -> new LismoveException("Organization not found", HttpStatus.NOT_FOUND));
        CustomFieldValue customFieldValue = customFieldService.findValueById(vid).orElseThrow(() -> new LismoveException(("Custom Field Value not found"), HttpStatus.NOT_FOUND));
        customFieldService.delete(customFieldValue);
        return "Deleted";
    }

    //Achievements
    @GetMapping("{oid}/achievements")
    public List<AchievementDto> getAchievements(@PathVariable Long oid) {
        Organization org = organizationService.findById(oid).orElseThrow(() -> new LismoveException("Organization not found", HttpStatus.NOT_FOUND));
        return achievementsMapper.achievementToDto(achievementService.findByOrganization(org));
    }

    //Awards
    @GetMapping("{oid}/award-positions")
    public List<AwardPositionDto> getAwardPositions(@PathVariable Long oid, @RequestParam(required = false, defaultValue = "false") Boolean active) {
        Organization org = organizationService.findById(oid).orElseThrow(() -> new LismoveException("Organization not found", HttpStatus.NOT_FOUND));
        return awardMapper.awardPositionToDto(awardService.findAllAwardPositionsByOrganization(org, active));
    }

    //Settings
    @GetMapping("{oid}/settings")
    public List<OrganizationSettingValueDto> getSettings(@PathVariable Long oid) {
        Organization org = organizationService.findById(oid).orElseThrow(() -> new LismoveException("Organization not found", HttpStatus.NOT_FOUND));
        return organizationSettingsService.findSettingsByOrganization(org);
    }

    @PostMapping("{oid}/settings")
    @ApiOperation(value = "saveSettings", notes = "L'API può essere usata per creare e aggiornare gli Organization Settings. Non è richiesto valorizzare id, defaultValue e organization.")
    public List<OrganizationSettingValueDto> saveSettings(@PathVariable Long oid, @RequestBody List<OrganizationSettingValueDto> settings) {
        organizationService.findById(oid).orElseThrow(() -> new LismoveException("Organization not found", HttpStatus.NOT_FOUND));
        settings.forEach(value -> {
            if (value.getOrganization() != null && !value.getOrganization().equals(oid)) {
                throw new LismoveException("Setting owned by another organization", HttpStatus.FORBIDDEN);
            }
            value.setOrganization(oid);
        });
        return organizationMapper.OrganizationSettingValueToDto(organizationService.save(settings));
    }

    @GetMapping("{oid}/messages")
    public List<NotificationMessageDto> getMessages(@PathVariable Long oid) {
        Organization org = organizationService.findById(oid).orElseThrow(() -> new LismoveException("Organization not found", HttpStatus.NOT_FOUND));
        return notificationMessageMapper.notificationMessageToDto(notificationMessageService.getByOrganization(org));
    }

    @GetMapping("{oid}/partials")
    public List<PartialsOverviewDto> getPartials(
            @PathVariable Long oid,
            @RequestParam(required = false, defaultValue = "false") Boolean isHomeWorkPath,
            @RequestParam Integer minAge, @RequestParam Integer maxAge,
            @RequestParam String minDate, @RequestParam String maxDate,
            @RequestParam String minTime, @RequestParam String maxTime
    ) {
        Organization org = organizationService.findById(oid).orElseThrow(() -> new LismoveException("Organization not found", HttpStatus.NOT_FOUND));
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDateTime formattedMinDate = LocalDate.parse(minDate, dateFormatter).atStartOfDay();
        ;
        LocalDateTime formattedMaxDate = LocalDateTime.of(LocalDate.parse(maxDate, dateFormatter), LocalTime.MAX);

        return organizationService.findPartialsByOrganization(oid, isHomeWorkPath, minAge, maxAge, formattedMinDate, formattedMaxDate, LocalTime.parse(minTime), LocalTime.parse(maxTime));
    }

    @PutMapping("refund-coupons")
    public Map<String, String> refundCoupons(@RequestParam(defaultValue = "", required = false) Long oid, @RequestBody(required = false) Set<String> codes) {
        Set<Coupon> coupons = getCoupons(oid, codes);
        return couponService.refund(coupons);
    }

    @PutMapping("reverse-refund-coupons")
    public Map<String, String> reverseRefundCoupons(@RequestParam(defaultValue = "", required = false) Long oid, @RequestBody(required = false) Set<String> codes) {
        Set<Coupon> coupons = getCoupons(oid, codes);
        return couponService.reverseRefund(coupons);
    }

    @NotNull
    private Set<Coupon> getCoupons(Long oid, Set<String> codes) {
        Set<Coupon> coupons = new HashSet<>();
        codes.forEach(c -> {
            Optional<Coupon> coupon;
            if (c != null && (coupon = couponService.findByCode(c)).isPresent()) {
                if ((coupon.get().getOrganization() == null && oid != null) || (coupon.get().getOrganization() != null && !coupon.get().getOrganization().getId().equals(oid))) {
                    throw new LismoveException("Coupon owned by another organization", HttpStatus.FORBIDDEN);
                }
            } else {
                throw new LismoveException("Coupon not found", HttpStatus.NOT_FOUND);
            }
            coupons.add(coupon.get());
        });
        if (oid != null) {
            organizationService.findById(oid).orElseThrow(() -> new LismoveException("Organization not found", HttpStatus.NOT_FOUND));
        }
        return coupons;
    }

    @GetMapping("coupons")
    public List<CouponDto> getCoupons(@RequestParam(defaultValue = "", required = false) Long oid) {
        if (oid == null) {
            return couponMapper.couponToDto(couponService.findAllNational());
        }
        Organization org = organizationService.findById(oid).orElseThrow(() -> new LismoveException("Organization not found", HttpStatus.NOT_FOUND));
        return couponMapper.couponToDto(couponService.findByOrganization(org));
    }
}
