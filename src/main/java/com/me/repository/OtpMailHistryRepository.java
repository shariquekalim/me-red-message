package com.me.repository;

import com.me.modal.OtpMailHistry;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OtpMailHistryRepository extends JpaRepository<OtpMailHistry, Integer>{
boolean existsByUniqueId(String uniqueId);

OtpMailHistry findAllByUniqueId(String uniqueId);
}
