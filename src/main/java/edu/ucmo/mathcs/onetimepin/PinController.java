package edu.ucmo.mathcs.onetimepin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

import static edu.ucmo.mathcs.onetimepin.Utils.generatePin;
import java.sql.*;

@Controller
@RequestMapping(path = "/api")
public class PinController {
	
	@Autowired
	private PinRepository repository;

	@PostMapping(path = "/generate")
	public @ResponseBody String addPin(@RequestParam String account, HttpServletRequest request) {
		Pin pin = new Pin();
        String ipAddress = request.getRemoteAddr();
		pin.setAccount(account);
		try {
            pin.setPin(generatePin());
        } catch(DataIntegrityViolationException e) {
            pin.setPin(generatePin());
        }
		pin.setCreateIp(ipAddress);
		repository.save(pin);

        return "{\"pin\":" + pin.getPin() + "}";
	}

}
