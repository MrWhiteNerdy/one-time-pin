package edu.ucmo.mathcs.onetimepin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@RestController
@RequestMapping(path = "/api")
public class PinController {
	
	private final static Logger LOGGER = Logger.getLogger(PinController.class.getName());
	
	@Autowired
	private PinRepository repository;
	
	@PostMapping(path = "/generate", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody
	String addPin(@RequestBody(required = false) Pin pin, HttpServletRequest request) {
		if (pin == null || pin.getAccount() == null) {
			LOGGER.severe("Account is required");
			return "{\"error\":\"account is required\"}";
		}
		
		if (pin.getAccount().equals("")) {
			LOGGER.severe("Empty account");
			return "{\"error\":\"account cannot be empty\"}";
		}
		
		if (pin.getCreateUser() == null) {
			LOGGER.severe("Create user is required");
			return "{\"error\":\"create user is required\"}";
		}
		
		if (pin.getCreateUser().equals("")) {
			LOGGER.severe("Empty create user");
			return "{\"error\":\"create user cannot be empty\"}";
		}
		
		String ipAddress = request.getRemoteAddr();
		pin.setCreateIp(ipAddress);
		
		try {
			pin.setPin(Utils.generatePin());
		} catch (DataIntegrityViolationException e) {
			pin.setPin(Utils.generatePin());
		}
		
		Date currentDate = Utils.getCurrentDate();
		pin.setCreateTimestamp(currentDate);
		
		Date expireDate = Utils.getExpireDate();
		pin.setExpireTimestamp(expireDate);
		
		Pin returnPin = repository.save(pin);
		
		LOGGER.info("User " + pin.getCreateUser() + " at IP address " + request.getRemoteAddr() + " generated pin "
				+ returnPin.getPin() + " on " + currentDate + " with an expiration date of " + expireDate);
		
		return "{\"pin\":\"" + returnPin.getPin() + "\"}";
	}
	
	@PostMapping(path = "/claim", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody
	String claimPin(@RequestBody(required = false) Pin pin, HttpServletRequest request) {
		if (pin == null || pin.getAccount() == null) {
			LOGGER.severe("Account is required");
			return "{\"error\":\"account is required\"}";
		}
		
		if (pin.getAccount().equals("")) {
			LOGGER.severe("Empty account");
			return "{\"error\":\"account cannot be empty\"}";
		}
		
		if (pin.getPin() == null) {
			LOGGER.severe("Pin is required");
			return "{\"error\":\"pin is required\"}";
		}
		
		if (pin.getPin().equals("")) {
			LOGGER.severe("Empty pin");
			return "{\"error\":\"pin cannot be empty\"}";
		}
		
		if (!Utils.luhnCheck(pin.getPin())) {
			LOGGER.severe("Pin not in correct format");
			return "{\"error\":\"pin not in correct format\"}";
		}
		
		if (pin.getClaimUser() == null) {
			LOGGER.severe("Claim user is required");
			return "{\"error\":\"claim user is required\"}";
		}
		
		if (pin.getClaimUser().equals("")) {
			LOGGER.severe("Empty claim user");
			return "{\"error\":\"claim user cannot be empty\"}";
		}
		
		List<Pin> inAcct = repository.findPinsByAccount(pin.getAccount());
		Date curDate = new Date();
		
		if (inAcct.contains(pin)) {
			Pin equalPin = new Pin();
			for (Pin loopPin : inAcct) {
				if (loopPin.getPin().equals(pin.getPin())) {
					equalPin = loopPin;
					break;
				}
			}
			
			if (equalPin.getExpireTimestamp().after(curDate)) {
				if (equalPin.getClaimIp() == null || equalPin.getClaimIp().equals("")) {
					equalPin.setClaimTimestamp(curDate);
					equalPin.setClaimIp(request.getRemoteAddr());
					equalPin.setClaimUser(pin.getClaimUser());
					repository.save(equalPin);
					
					LOGGER.info("Pin " + equalPin.getPin() + " has been successfully claimed by "
							+ equalPin.getClaimUser() + " on " + equalPin.getClaimTimestamp());
					return "{\"success\":\"pin has been successfully claimed\"}";
				} else {
					LOGGER.severe("Requested pin " + pin.getPin() + " has already been claimed");
					return "{\"error\":\"requested pin has already been claimed\"}";
				}
			} else {
				LOGGER.severe("Requested pin " + pin.getPin() + " was valid but has expired");
				return "{\"error\":\"requested pin was valid but has expired\"}";
			}
		} else {
			LOGGER.severe("Requested pin " + pin.getPin() + " was invalid");
			return "{\"error\":\"requested pin was invalid\"}";
		}
	}
	
}
