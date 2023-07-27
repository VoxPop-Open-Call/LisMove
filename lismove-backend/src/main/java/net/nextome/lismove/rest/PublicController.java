package net.nextome.lismove.rest;

import net.nextome.lismove.exceptions.LismoveException;
import net.nextome.lismove.models.Organization;
import net.nextome.lismove.models.User;
import net.nextome.lismove.rest.dto.OrganizationDto;
import net.nextome.lismove.rest.mappers.OrganizationMapper;
import net.nextome.lismove.services.OrganizationService;
import net.nextome.lismove.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("public")
public class PublicController {

	@Autowired
	private OrganizationService organizationService;
	@Autowired
	private OrganizationMapper organizationMapper;
	@Autowired
	private UserService userService;

	@GetMapping("organizations/{oid}")
	public OrganizationDto get(@PathVariable Long oid) {
		Organization org = organizationService.findById(oid).orElseThrow(() -> new LismoveException("Organization not found", HttpStatus.NOT_FOUND));
		return organizationMapper.organizationToDto(org);
	}

	@GetMapping("/coin-wallet")
	public Map<String, BigDecimal> getWallet(@RequestParam("addr") String addr) {
		User user = userService.findByCoinWallet(addr).orElseThrow(() -> new LismoveException("Wallet not found", HttpStatus.NOT_FOUND));
		Map<String, BigDecimal> map = new HashMap<>();
		map.put("BIKE", Optional.ofNullable(user.getCoin()).orElse(BigDecimal.ZERO).movePointRight(18));
		return map;
	}
}
