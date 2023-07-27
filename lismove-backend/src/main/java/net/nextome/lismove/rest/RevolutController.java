package net.nextome.lismove.rest;

import net.nextome.lismove.integrations.revolut.RevolutService;
import net.nextome.lismove.integrations.revolut.models.Counterparty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("revolut")
public class RevolutController {

	@Autowired
	private RevolutService revolutService;

	@GetMapping("token")
	public String getToken(@RequestParam("code") String code) {
		return revolutService.generateToken(code);
	}

	@GetMapping("accounts")
	public String getAccount() {
		return revolutService.getAccounts();
	}

	@GetMapping("counterparties")
	public List<Counterparty> counterpartyList() {
		return revolutService.listCounterparties().stream().filter(c -> c.getAccounts() != null && c.getAccounts().size() > 0).collect(Collectors.toList());
	}

	@PostMapping("massive")
	public void counterpartyMassive(@RequestParam("csvfile") MultipartFile csvfile, @RequestParam("type") String type, HttpServletResponse response) throws IOException {
		response.setContentType("text/csv");
		response.setHeader("Content-Disposition", "attachment; filename=counterparties.csv");
		if(csvfile.getOriginalFilename().isEmpty()) {
			response.getWriter().println("No selected file to upload! Please do the checking");
			return;
		}
		if(type.equals("c")) {
			revolutService.massiveCounterpartiesCreation(csvfile.getInputStream(), response.getWriter());
		} else if(type.equals("p")) {
			revolutService.massivePaymentCreation(csvfile.getInputStream(), response.getWriter());
		}

	}

	@GetMapping("payments/massive")
	public void paymentsMassive(HttpServletResponse response) throws IOException {
		response.setContentType("text/csv");
		response.setHeader("Content-Disposition", "attachment; filename=pay.csv");
		revolutService.massivePaymentCreation(new ClassPathResource("pay.csv").getInputStream(), response.getWriter());
	}

	@GetMapping("counterparties/new")
	public Counterparty create() {
		Counterparty counterparty = new Counterparty();
		counterparty.setCompany_name("Company name");
		counterparty.setBank_country("");
		counterparty.setCurrency("");
		counterparty.setIban("");
		counterparty.setBic("");
		counterparty.setEmail("");
		return revolutService.create(counterparty);
	}

	@PostMapping("/single")
	public void uploadSingleCSVFile(@RequestParam("csvfile") MultipartFile csvfile, HttpServletResponse response) throws IOException {

		// Checking the upload-file's name before processing
		if(csvfile.getOriginalFilename().isEmpty()) {
			response.getWriter().println("No selected file to upload! Please do the checking");
			return;
		}


		// checking the upload file's type is CSV or NOT

		response.setContentType("text/csv");
		response.setHeader("Content-Disposition", "attachment; filename=customers.csv");
	}
}
