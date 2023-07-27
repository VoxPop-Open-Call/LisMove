package net.nextome.lismove.services;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.auth.UserRecord;
import net.nextome.lismove.config.SecurityServiceProperties;
import net.nextome.lismove.exceptions.LismoveException;
import net.nextome.lismove.models.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class FirebaseAuthService {

	@Autowired
	private SecurityServiceProperties serviceProperties;

	private final Logger logger = LoggerFactory.getLogger(getClass());

	public UserRecord createUser(User u, String password) {
		UserRecord.CreateRequest request = new UserRecord.CreateRequest();
		request.setEmail(u.getEmail());
		request.setEmailVerified(true);
		request.setPassword(password);
		try {
			return FirebaseAuth.getInstance().createUser(request);
		} catch(FirebaseAuthException e) {
			logger.error(e.getAuthErrorCode().name());
			throw new LismoveException(e.getMessage());
		}
	}

	public UserRecord updateUser(User u, String password) {
		UserRecord.UpdateRequest request = new UserRecord.UpdateRequest(u.getUid());
		request.setEmail(u.getEmail());
		request.setPassword(password);
		try {
			return FirebaseAuth.getInstance().updateUser(request);
		} catch (FirebaseAuthException e) {
			logger.error(e.getAuthErrorCode().name());
			throw new LismoveException(e.getMessage());
		}
	}

	/**
	 * lancia un errore se il token di creazione utente non è valido
	 * @param token token firebase
	 * @param uid uid dell'utente creato
	 */
	public void checkToken(String token, String uid){
		try {
			FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(token.replace(serviceProperties.getTokenPrefix(), ""));
			if(!decodedToken.getUid().equals(uid)) {
				throw new LismoveException("UID mismatches with token", HttpStatus.UNAUTHORIZED);
			}
		} catch(NullPointerException | FirebaseAuthException e) {
			throw new LismoveException(e.getMessage(), HttpStatus.UNAUTHORIZED);
		}
	}

}
