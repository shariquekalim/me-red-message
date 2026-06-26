package com.me.repository;

import com.me.modal.ApplicationTempMapping;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ApplicationTempMapRepository extends JpaRepository<ApplicationTempMapping, Long>{
	List<ApplicationTempMapping> findAllByApplicationId(Long id);
}
