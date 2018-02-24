package edu.ucmo.mathcs.onetimepin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

import static edu.ucmo.mathcs.onetimepin.Utils.generatePin;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Date;


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
	@PostMapping(path = "/claim")
	public @ResponseBody String claimPin(@RequestBody String acct, @RequestBody String pin, HttpServletRequest request) {
		Pin inAcct = repository.findPinByAccount(acct);
		Date curDate = new Date();
		if(inAcct.getPin().equals(pin)) { // Requested pin is valid for this account
			if(inAcct.getExpireTimestamp().before(curDate)) { // Expire time is before the current time.
				inAcct.setClaimTimestamp(curDate);
				inAcct.setClaimIp(request.getRemoteAddr());
				repository.save(inAcct);
				return "Claim successful";
			}
			else
				return "The requested pin was valid but has expired.";
		}
		else
			return "The requested pin was invalid";
	}
}
