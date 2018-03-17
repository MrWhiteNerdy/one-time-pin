package edu.ucmo.mathcs.onetimepin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping(path = "/api")
public class PinController {
	
	@Autowired
	private PinRepository repository;
	
	@PostMapping(path = "/generate", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody
	String addPin(@RequestBody(required = false) Pin pin, HttpServletRequest request) {
		if (pin == null || pin.getAccount() == null) return "{\"error\":\"account is required\"}";
		
		if (pin.getAccount().equals("")) return "{\"error\":\"invalid account\"}";

		pin.setCreateIp(request.getRemoteAddr());
		
		try {
			pin.setPin(Utils.generatePin());
		} catch (DataIntegrityViolationException e) {
			pin.setPin(Utils.generatePin());
		}
		
		pin.setCreateTimestamp(Utils.getCurrentDate());
		pin.setExpireTimestamp(Utils.getExpireDate());
		
		Pin returnPin = repository.save(pin);
		
		return "{\"pin\":" + returnPin.getPin() + "}";
	}
	
	@PostMapping(path = "/claim", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody
	String claimPin(@RequestBody(required = false) Pin pin, HttpServletRequest request) {
	    if(pin == null || pin.getAccount() == null) return "{\"error\":\"account is required\"}";  // See if they include the account in the function call

        if(pin.getAccount().equals("")) return "{\"error\":\"empty account\"}";   // Make sure they input something for the account

        if(pin.getPin() == null) return "{\"error\":\"pin is required\"}";       // See if they include the pin in the function call

        if(pin.getPin().equals("")) return "{\"error\":\"empty pin\"}";        // Make sure they input something for the pin

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
					equalPin.setClaimUser(pin.getCreateUser());
					repository.save(equalPin);
					return "{\"success\":\"The pin has been successfully claimed\"}";
				} else
					return "{\"error\":\"The requested pin has already been claimed\"}";
			} else
				return "{\"error\":\"The requested pin was valid but has expired\"}";
		} else
			return "{\"error\":\"The requested pin was invalid\"}";
	}
}
