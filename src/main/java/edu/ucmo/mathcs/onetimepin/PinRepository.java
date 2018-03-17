package edu.ucmo.mathcs.onetimepin;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PinRepository extends JpaRepository<Pin, Long> {
	
	List<Pin> findPinsByAccount(String account);
	
}
