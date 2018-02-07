package edu.ucmo.mathcs.onetimepin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(path = "/api")
public class PinController {
	
	@Autowired
	private PinRepository repository;
	
	/**
	 * Example for adding pin to database from endpoint
	 * @param account The account in the POST body to create the pin
	 * @return An example message
	 */
	@PostMapping(path = "/add")
	public @ResponseBody String addPin(@RequestBody String account) {
		Pin pin = new Pin();
		pin.setAccount(account);
		pin.setPin("123456");
		repository.save(pin);
		
		return "Added pin";
	}
	
}
