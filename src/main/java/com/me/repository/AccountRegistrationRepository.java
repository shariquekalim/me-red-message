package com.me.repository;

import com.me.modal.AccountRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface AccountRegistrationRepository  extends JpaRepository<AccountRegistration, Long>{
	
	@Query(value = "select * from me_account_registration a where a.id=?1", nativeQuery = true)
	AccountRegistration findAccountDataById(Long id);
}
