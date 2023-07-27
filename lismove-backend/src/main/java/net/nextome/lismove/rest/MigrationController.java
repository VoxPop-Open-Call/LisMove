package net.nextome.lismove.rest;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import com.google.maps.errors.ApiException;
import net.nextome.lismove.models.Address;
import net.nextome.lismove.rest.dto.AddressOverviewDto;
import net.nextome.lismove.services.GoogleMapsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("migrations")
public class MigrationController {

	@Autowired
	private GoogleMapsService gmapsService;

	@GetMapping("uid")
	public String getUid(@RequestParam("email") String email) {
		FirebaseAuth auth = FirebaseAuth.getInstance();
		UserRecord userRecord;
		try {
			userRecord = auth.getUserByEmail(email);
		} catch(FirebaseAuthException e) {
			userRecord = null;
		}
		if(userRecord == null) {
			UserRecord.CreateRequest req = new UserRecord.CreateRequest();
			req.setEmail(email);
			req.setEmailVerified(true);
			req.setDisabled(false);
			req.setPassword(UUID.randomUUID().toString());
			try {
				userRecord = auth.createUser(req);
			} catch(FirebaseAuthException e) {
				e.printStackTrace();
				return e.getMessage();
			}
		}
		return userRecord.getUid();
	}

	@PostMapping("address")
	public AddressOverviewDto getAddress(@RequestBody AddressOverviewDto address) {
		Address addr = gmapsService.reverseGeocoding(address.getLatitude(), address.getLongitude());
		address.setAddress(addr.getAddress());
		if(addr.getCity() != null) {
			address.setCity(addr.getCity().getIstatId());
		}
		address.setNumber(addr.getNumber());
		return address;
	}

	@PostMapping("generateAddress")
	public AddressOverviewDto generateAddress(@RequestBody AddressOverviewDto address) throws IOException, InterruptedException, ApiException {
		if(address.getAddress() != null && !address.getAddress().isEmpty()){
			Address addr = gmapsService.generateAddress(address.getAddress());
			address.setNumber(addr.getNumber());
			address.setLatitude(addr.getLatitude());
			address.setLongitude(addr.getLongitude());
		}
		return address;
	}
}
