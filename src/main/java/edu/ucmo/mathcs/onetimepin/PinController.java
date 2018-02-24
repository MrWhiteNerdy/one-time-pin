package edu.ucmo.mathcs.onetimepin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;

@RestController
@RequestMapping(path = "/api")
public class PinController {
	
	@Autowired
	private PinRepository repository;
	
	@PostMapping(path = "/generate")
	public @ResponseBody
	String addPin(@RequestParam(required = false) String account, HttpServletRequest request) {
		if (account == null) return "{\"error\":\"account is required\"}";
		
		if (account.equals("")) return "{\"error\":\"invalid account\"}";
		
		Pin pin = new Pin();
		pin.setAccount(account);
		pin.setCreateUser(account);
		pin.setCreateIp(request.getRemoteAddr());
		
		try {
			pin.setPin(Utils.generatePin());
		} catch (DataIntegrityViolationException e) {
			pin.setPin(Utils.generatePin());
		}
		
		pin.setCreateTimestamp(Utils.getCurrentDate());
		pin.setExpireTimestamp(Utils.getExpireDate());
		
		repository.save(pin);
		
		return "{\"pin\":" + pin.getPin() + "}";
	}
	
	@PostMapping(path = "/claim")
	public @ResponseBody
	String claimPin(@RequestBody String acct, @RequestBody String pin, HttpServletRequest request) {
		Pin inAcct = repository.findPinByAccount(acct);
		Date curDate = new Date();
		if (inAcct.getPin().equals(pin)) { // Requested pin is valid for this account
			if (inAcct.getExpireTimestamp().before(curDate)) { // Expire time is before the current time.
				inAcct.setClaimTimestamp(curDate);
				inAcct.setClaimIp(request.getRemoteAddr());
				repository.save(inAcct);
				return "Claim successful";
			} else
				return "The requested pin was valid but has expired.";
		} else
			return "The requested pin was invalid";
	}
}
