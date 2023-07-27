package net.nextome.lismove.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("nextome.security")
public class SecurityServiceProperties {
	private boolean jwt;
	private boolean custom;
	private boolean oauth;
	private String secret = "";
	Long expirationTime = (long) (1000 * 60 * 60 * 24 * 10); // 10 days
	String tokenPrefix = "Bearer ";
	String jwt_header = "Authorization";
	String developer_header = "developer";
	String token_header = "token";
	String login_url = "/login";
	String mockuser;

	public boolean isJwt() {
		return jwt;
	}

	public void setJwt(boolean jwt) {
		this.jwt = jwt;
	}

	public boolean isCustom() {
		return custom;
	}

	public void setCustom(boolean custom) {
		this.custom = custom;
	}

	public boolean isOauth() {
		return oauth;
	}

	public void setOauth(boolean oauth) {
		this.oauth = oauth;
	}

	public String getSecret() {
		return secret;
	}

	public void setSecret(String secret) {
		this.secret = secret;
	}

	public Long getExpirationTime() {
		return expirationTime;
	}

	public void setExpirationTime(Long expirationTime) {
		this.expirationTime = expirationTime;
	}

	public String getTokenPrefix() {
		return tokenPrefix;
	}

	public void setTokenPrefix(String tokenPrefix) {
		this.tokenPrefix = tokenPrefix;
	}

	public String getJwt_header() {
		return jwt_header;
	}

	public void setJwt_header(String jwt_header) {
		this.jwt_header = jwt_header;
	}

	public String getDeveloper_header() {
		return developer_header;
	}

	public void setDeveloper_header(String developer_header) {
		this.developer_header = developer_header;
	}

	public String getToken_header() {
		return token_header;
	}

	public void setToken_header(String token_header) {
		this.token_header = token_header;
	}

	public String getLogin_url() {
		return login_url;
	}

	public void setLogin_url(String login_url) {
		this.login_url = login_url;
	}

	public String getMockuser() {
		return mockuser;
	}

	public void setMockuser(String mockuser) {
		this.mockuser = mockuser;
	}
}
