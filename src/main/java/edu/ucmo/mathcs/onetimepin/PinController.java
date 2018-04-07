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

	private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
	
	@Autowired
	private PinRepository repository;
	
	@PostMapping(path = "/generate", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody
	String addPin(@RequestBody(required = false) Pin pin, HttpServletRequest request) {
		try {
			MyLogger.setup();
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("Problems with creating the log files");
		}

        LOGGER.setLevel(Level.ALL);

		if (pin == null || pin.getAccount() == null) {
			LOGGER.severe("Account is required");
			return "{\"error\":\"account is required\"}";
		}

		pin.setCreateIp(request.getRemoteAddr());
		
		if (pin.getAccount().equals("")) {
			LOGGER.severe("Invalid account");
			return "{\"error\":\"invalid account\"}";
		}

		if(pin.getCreateUser() == null) return "{\"error\":\"create user is required\"}";

		if(pin.getCreateUser().equals("")) return "{\"error\":\"empty create user\"}";

		String ipAddress = request.getRemoteAddr();
		pin.setCreateIp(ipAddress);
		LOGGER.info("IP Address: " + ipAddress);

        String generatedPin = Utils.generatePin();
		try {
			pin.setPin(generatedPin);
            LOGGER.info("PIN: " + generatedPin);
		} catch (DataIntegrityViolationException e) {
			pin.setPin(generatedPin);
            LOGGER.info("PIN: " + generatedPin);
		}

		Date currentDate = Utils.getCurrentDate();
		pin.setCreateTimestamp(currentDate);
		LOGGER.info("Current Date: " + currentDate);
        Date expireDate = Utils.getExpireDate();
		pin.setExpireTimestamp(expireDate);
		LOGGER.info("Expire Date: " + expireDate);
		
		pin.setCreateTimestamp(Utils.getCurrentDate());
		pin.setExpireTimestamp(Utils.getExpireDate());

		Pin returnPin = repository.save(pin);
        LOGGER.info(ipAddress + " generated " + generatedPin + " on " + currentDate + " with an expiration date of " + expireDate);
		
		return "{\"pin\":\"" + returnPin.getPin() + "\"}";
	}
	
	@PostMapping(path = "/claim", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody
	String claimPin(@RequestBody(required = false) Pin pin, HttpServletRequest request) {
	    if(pin == null || pin.getAccount() == null) {
            LOGGER.severe("Account is required");
            return "{\"error\":\"account is required\"}";  // See if they include the account in the function call
        }

        if(pin.getAccount().equals("")) {
            LOGGER.severe("Empty account");
            return "{\"error\":\"empty account\"}";   // Make sure they input something for the account
        }

        if(pin.getPin() == null) {
            LOGGER.severe("PIN is required");
            return "{\"error\":\"pin is required\"}";       // See if they include the pin in the function call
        }

        if(pin.getPin().equals("")) {
            LOGGER.severe("Empty PIN");
            return "{\"error\":\"empty pin\"}";        // Make sure they input something for the pin
        }

        if (!Utils.luhnCheck(pin.getPin())) {
            LOGGER.severe("PIN not in correct format");
            return "{\"error\":\"pin not in correct format\"}";
        }

		if(pin.getClaimUser() == null) return "{\"error\":\"claim user is required\"}";	// Make sure they include the creating user

		if(pin.getClaimUser().equals("")) return "{\"error\":\"empty claim user\"}";	// Make sure they input something for the creating user

		List<Pin> inAcct = repository.findPinsByAccount(pin.getAccount());
		Date curDate = new Date();

		if (inAcct.contains(pin)) { // Requested pin is valid for this account
			Pin equalPin = new Pin();
			for(Pin loopPin : inAcct) {
				if(loopPin.getPin().equals(pin.getPin())) {
					equalPin = loopPin;
					break;
				}
			}
			if (equalPin.getExpireTimestamp().after(curDate)) { // Expire time is after the current time.
				if(equalPin.getClaimIp() == null || equalPin.getClaimIp().equals("")) {
					equalPin.setClaimTimestamp(curDate);
					equalPin.setClaimIp(request.getRemoteAddr());
					equalPin.setClaimUser(pin.getClaimUser());
					repository.save(equalPin);
                    LOGGER.info("The PIN has been successfully claimed");
					return "{\"success\":\"The pin has been successfully claimed\"}";
				} else
                    LOGGER.severe("The requested PIN has already been claimed");
					return "{\"error\":\"The requested pin has already been claimed\"}";
			} else
                LOGGER.severe("The requested PIN was valid but has expired");
				return "{\"error\":\"The requested pin was valid but has expired\"}";
		} else
		    LOGGER.severe("The requested PIN was invalid");
			return "{\"error\":\"The requested pin was invalid\"}";
	}
}
