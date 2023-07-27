package net.nextome.lismove.security;

import net.nextome.lismove.models.User;
import net.nextome.lismove.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class NextomeUserDetailsService implements UserDetailsService {
	@Autowired
	private UserService userRepository;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		final User user = userRepository.findByUid(username).orElseThrow(() -> new NextomeSecurityException("User '" + username + "' not found", HttpStatus.UNAUTHORIZED));
		return new NextomeUserDetails(user);
	}

	public void registerLogin(String uid, String appVersion, String appOs) {
		userRepository.registerLogin(uid, appVersion, appOs);
	}
}
