package com.me.repository;

import com.me.modal.Otp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;



public interface OtpRepository  extends JpaRepository<Otp, Long>{
Otp findAllByOtpId(String otpId);
@Query(value = "select max(id) from me_otp_template", nativeQuery = true)
Long getOtpId();

@Modifying
@Transactional
@Query(value = "delete from me_otp_template where id=?1", nativeQuery = true)
void deleteOTPId(Long id);
}
