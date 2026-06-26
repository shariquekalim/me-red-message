package com.me.repository;

import com.me.modal.ApplicationRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ApplicationRegistrationRepository extends JpaRepository<ApplicationRegistration, Long>{

//	@Modifying
//	@Transactional
	 @Query(value = "select * from me_application_registration a where a.id=?1", nativeQuery = true)
	ApplicationRegistration	findAppDataById(Long id);
}
