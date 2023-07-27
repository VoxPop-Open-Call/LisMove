package net.nextome.lismove.security;

import net.nextome.lismove.models.User;
import net.nextome.lismove.models.enums.UserType;
import org.springframework.security.core.authority.AuthorityUtils;

public class NextomeUserDetails extends org.springframework.security.core.userdetails.User {

	private User userData;
	private String token;

	public NextomeUserDetails(User user) {
		super(user.getEmail(),
				user.getEmail(),
				user.getEnabled() != null ? user.getEnabled() : true,
				true,
				true,
				true,
				AuthorityUtils.createAuthorityList((user.getUserType() == null ? UserType.ROLE_LISMOVER : user.getUserType()).name())
		);
		userData = user;
	}

	public User getUserData() {
		return userData;
	}

	public void setUserData(User userData) {
		this.userData = userData;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}
}
