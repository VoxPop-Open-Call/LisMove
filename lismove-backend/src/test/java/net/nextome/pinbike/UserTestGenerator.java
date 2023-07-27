package net.nextome.lismove;

import net.nextome.lismove.models.User;
import net.nextome.lismove.models.enums.UserType;

import java.math.BigDecimal;

public class UserTestGenerator extends User {

	public static User generate(String uid, String username) {
		User user = new User();
		user.setUid(uid);
		user.setUsername(username);
//        user.setEarnedUrbanPoints(0D);
		user.setEarnedNationalPoints(BigDecimal.ZERO);
		return user;
	}

	public static User generate(String uid, String username, String email, UserType role) {
		User user = generate(uid, username);
		user.setEmail(email);
		user.setUserType(role);
		return user;
	}
}
