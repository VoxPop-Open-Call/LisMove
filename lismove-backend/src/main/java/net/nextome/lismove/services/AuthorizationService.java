package net.nextome.lismove.services;

import net.nextome.lismove.models.enums.UserType;
import net.nextome.lismove.security.NextomeUserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuthorizationService {
	public boolean belongsToOrganization(NextomeUserDetails user, Long oid) {
		return user.getUserData().getUserType().equals(UserType.ROLE_ADMIN) || user.getUserData().getOrganization().getId().equals(oid);
	}
}
