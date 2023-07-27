package net.nextome.lismove.integrations.revolut;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import io.netty.handler.logging.LogLevel;
import net.nextome.lismove.exceptions.LismoveException;
import net.nextome.lismove.integrations.revolut.models.Counterparty;
import net.nextome.lismove.integrations.revolut.models.RevolutIndividual;
import net.nextome.lismove.integrations.revolut.models.RevolutPayment;
import net.nextome.lismove.integrations.revolut.models.RevolutPaymentReceiver;
import net.nextome.lismove.security.OauthToken;
import net.nextome.lismove.services.SettingsService;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.transport.logging.AdvancedByteBufFormat;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RevolutService {
	private static final String serverHost = "https://b2b.revolut.com/api/1.0/";
	private final WebClient revolutClient;
	private final Logger logger = LoggerFactory.getLogger(getClass());
	@Autowired
	private SettingsService settingsService;

	public RevolutService() {
		HttpClient httpClient = HttpClient
				.create()
				.wiretap("reactor.netty.http.client.HttpClient",
						LogLevel.DEBUG, AdvancedByteBufFormat.TEXTUAL);
		revolutClient = WebClient.builder()
				.baseUrl(serverHost)
				.clientConnector(new ReactorClientHttpConnector(httpClient))
				.filter(logRequest())
				.build();
	}

	private ExchangeFilterFunction logRequest() {
		return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
			logger.info("Request: {} {}", clientRequest.method(), clientRequest.url());
			clientRequest.headers().forEach((name, values) -> values.forEach(value -> logger.info("{}={}", name, value)));
			return Mono.just(clientRequest);
		});
	}

	public String generateToken(String code) {
		settingsService.set("revolut-code", code);
		settingsService.set("revolut-code-date", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
		String clientId = settingsService.get("revolut-clientid", "mVrx88RkCKfgwZVamrBPoZhm5TCnTP2yA7sfZn_2RWs");
		try {
			RSAPrivateKey privateKey = readPrivateKey(new ClassPathResource("certs/revolut-priv-pkcs8.pem").getInputStream());
			RSAPublicKey publicKey = readPublicKey(new ClassPathResource("certs/revolut-pub.cer").getInputStream());
			Algorithm algorithmRS = Algorithm.RSA256(publicKey, privateKey);
			String token = JWT.create()
					.withIssuer("lismove-test.nextome.xyz")
					.withSubject(clientId)
					.withAudience("https://revolut.com")
					.withExpiresAt(Date.from(LocalDateTime.now().plusDays(90).atZone(ZoneId.systemDefault())
							.toInstant()))
					.sign(algorithmRS);
			settingsService.set("revolut-token", token);
			OauthToken tokens = revolutClient.post()
					.uri("auth/token")
					.contentType(MediaType.APPLICATION_FORM_URLENCODED)
					.body(BodyInserters.fromFormData("grant_type", "authorization_code")
							.with("code", code)
							.with("client_id", clientId)
							.with("client_assertion_type", "urn:ietf:params:oauth:client-assertion-type:jwt-bearer")
							.with("client_assertion", token))
					.retrieve()
					.bodyToMono(OauthToken.class)
					.block();
			settingsService.set("revolut_refresh_token", tokens.getRefresh_token());
			settingsService.set("revolut_access_token", tokens.getAccess_token());
			settingsService.set("revolut_access_token_expires", LocalDateTime.now().plusSeconds(tokens.getExpires_in()).format(DateTimeFormatter.ISO_DATE_TIME));
			return tokens.getAccess_token();
		} catch(WebClientResponseException e) {
			throw new LismoveException(e.getResponseBodyAsString());
		} catch(Exception e) {
			logger.error(e.getMessage());
			return null;
		}
	}

	public String getAccounts() {
		return revolutClient.get().uri("accounts").header("Authorization", "Bearer " + getAccessToken()).retrieve().bodyToMono(String.class).block();
	}

	public Counterparty create(Counterparty c) {
		return revolutClient.post().uri("counterparty").header("Authorization", "Bearer " + getAccessToken()).body(Mono.just(c), Counterparty.class).retrieve().bodyToMono(Counterparty.class).block();
	}

	public List<Counterparty> listCounterparties() {
		return revolutClient.get().uri("counterparties").header("Authorization", "Bearer " + getAccessToken()).retrieve().bodyToMono(new ParameterizedTypeReference<List<Counterparty>>() {
		}).block();
	}

	public RevolutPayment makePayment(RevolutPayment payment) {
		return revolutClient.post().uri("pay").header("Authorization", "Bearer " + getAccessToken()).body(Mono.just(payment), RevolutPayment.class).retrieve().bodyToMono(RevolutPayment.class).block();
	}

	public void massiveCounterpartiesCreation(InputStream in, Writer writer) {
		BufferedReader fileReader = null;
		CSVParser csvParser = null;
		CSVPrinter csvPrinter = null;

		try {
			fileReader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
			csvParser = new CSVParser(fileReader,
					CSVFormat.EXCEL.withDelimiter(';').withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim());
			csvPrinter = new CSVPrinter(writer,
					CSVFormat.EXCEL.withDelimiter(';').withHeader("UUID", "Ragione sociale", "Nome", "Cognome", "email", "iban", "bic", "Esito"));
			Iterable<CSVRecord> csvRecords = csvParser.getRecords();

			List<String> ibanList = new LinkedList<>();
			for(Counterparty counterparty : listCounterparties()) {
				if(counterparty.getAccounts() != null) {
					ibanList.addAll(counterparty.getAccounts().stream().map(Counterparty::getIban).collect(Collectors.toList()));
				}
			}

			for(CSVRecord csvRecord : csvRecords) {
				Counterparty c = new Counterparty();
				String result = "OK";
				try {
					c.setCompany_name(!csvRecord.get(0).isEmpty() ? csvRecord.get(0) : null);
					c.setIndividual_name(!csvRecord.get(1).isEmpty() ? new RevolutIndividual(csvRecord.get(1), csvRecord.get(2)) : null);
					c.setEmail(csvRecord.get(3));
					c.setIban(csvRecord.get(4));
					c.setBic(csvRecord.get(5));
					c.setBank_country(csvRecord.get(4).substring(0, 2));
					c.setCurrency("EUR");
					if(ibanList.contains(c.getIban())) {
						result = "IBAN duplicato";
					} else {
						c = create(c);
					}
				} catch(WebClientResponseException e) {
					logger.error(e.getResponseBodyAsString());
					result = e.getResponseBodyAsString();
				} catch(Exception e) {
					logger.error(e.getMessage());
					result = e.getMessage();
				}
				List<String> data = Arrays.asList(
						c.getId() != null ? c.getId().toString() : "",
						csvRecord.get(0),
						csvRecord.get(1),
						csvRecord.get(2),
						csvRecord.get(3), csvRecord.get(4), csvRecord.get(5), result);
				csvPrinter.printRecord(data);
				logger.info(String.join(";", data));
			}
		} catch(IOException e) {
			throw new LismoveException(e.getMessage());
		}
	}

	public void massivePaymentCreation(InputStream in, Writer writer) {
		BufferedReader fileReader = null;
		CSVParser csvParser = null;
		CSVPrinter csvPrinter = null;

		try {
			fileReader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
			csvParser = new CSVParser(fileReader,
					CSVFormat.EXCEL.withDelimiter(';').withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim());
			csvPrinter = new CSVPrinter(writer,
					CSVFormat.EXCEL.withDelimiter(';').withHeader("iban", "totale", "causale", "esito"));
			Iterable<CSVRecord> csvRecords = csvParser.getRecords();

			Map<String, UUID> ibanMap = new HashMap<>();
			for(Counterparty counterparty : listCounterparties()) {
				if(counterparty.getAccounts() != null) {
					counterparty.getAccounts().forEach(c -> ibanMap.put(c.getIban(), counterparty.getId()));
				}
			}

			for(CSVRecord csvRecord : csvRecords) {
				String result = null;
				RevolutPayment payment = new RevolutPayment();
				try {
					String iban = csvRecord.get(0);
					payment.setAccount_id(UUID.fromString(settingsService.get("revolut_account", "39fd9593-1e0c-487b-b026-f2611c7e9b71")));
					payment.setRequest_id(System.currentTimeMillis() + iban.substring(iban.length() - 10, iban.length() - 1) + csvRecord.get(1).replace(".", ""));
					payment.setReceiver(new RevolutPaymentReceiver(ibanMap.get(iban)));
					payment.setAmount(Double.parseDouble(csvRecord.get(1)));
					payment.setCurrency("EUR");
					payment.setReference(csvRecord.get(2));
					payment = makePayment(payment);
					result = payment.getState();
				} catch(WebClientResponseException e) {
					logger.error(e.getResponseBodyAsString());
					result = e.getResponseBodyAsString();
				} catch(Exception e) {
					logger.error(e.getMessage());
					result = e.getMessage();
				}
				List<String> data = Arrays.asList(
						csvRecord.get(0),
						csvRecord.get(1),
						csvRecord.get(2),
						result);
				csvPrinter.printRecord(data);
				logger.info(String.join(";", data));
			}
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	private String refreshToken() {
		logger.info("Refresh Token");
		String refresh = settingsService.get("revolut_refresh_token", "");
		OauthToken tokens = revolutClient.post()
				.uri("auth/token")
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.body(BodyInserters.fromFormData("grant_type", "refresh_token")
						.with("refresh_token", refresh)
						.with("client_id", settingsService.get("revolut-clientid", "mVrx88RkCKfgwZVamrBPoZhm5TCnTP2yA7sfZn_2RWs"))
						.with("client_assertion_type", "urn:ietf:params:oauth:client-assertion-type:jwt-bearer")
						.with("client_assertion", settingsService.get("revolut-token", "")))
				.retrieve()
				.bodyToMono(OauthToken.class)
				.block();
		settingsService.set("revolut_access_token", tokens.getAccess_token());
		settingsService.set("revolut_access_token_expires", LocalDateTime.now().plusSeconds(tokens.getExpires_in()).format(DateTimeFormatter.ISO_DATE_TIME));
		return tokens.getAccess_token();
	}

	private String getAccessToken() {
		LocalDateTime expires = LocalDateTime.parse(settingsService.get("revolut_access_token_expires", LocalDateTime.MIN.format(DateTimeFormatter.ISO_DATE_TIME)), DateTimeFormatter.ISO_DATE_TIME);
		if(expires.isBefore(LocalDateTime.now())) {
			return refreshToken();
		}
		return settingsService.get("revolut_access_token", "");
	}

	private RSAPublicKey readPublicKey(InputStream file) throws Exception {
		try(InputStreamReader keyReader = new InputStreamReader(file);
		    PemReader pemReader = new PemReader(keyReader)) {

			PemObject pemObject = pemReader.readPemObject();
			byte[] content = pemObject.getContent();

			CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
			InputStream in = new ByteArrayInputStream(content);
			X509Certificate certificate = (X509Certificate) certFactory.generateCertificate(in);
			return ((RSAPublicKey) certificate.getPublicKey());
		}
	}

	private RSAPrivateKey readPrivateKey(InputStream file) throws Exception {
		KeyFactory factory = KeyFactory.getInstance("RSA");

		try(InputStreamReader keyReader = new InputStreamReader(file);
		    PemReader pemReader = new PemReader(keyReader)) {

			PemObject pemObject = pemReader.readPemObject();
			byte[] content = pemObject.getContent();
			PKCS8EncodedKeySpec privKeySpec = new PKCS8EncodedKeySpec(content);
			return (RSAPrivateKey) factory.generatePrivate(privKeySpec);
		}
	}

}
