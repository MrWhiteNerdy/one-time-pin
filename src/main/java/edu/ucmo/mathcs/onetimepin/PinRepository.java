package edu.ucmo.mathcs.onetimepin;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PinRepository extends JpaRepository<Pin, Long> {
	
	public Pin findPinByAccount(String account);
	
}
