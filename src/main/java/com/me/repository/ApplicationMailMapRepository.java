package com.me.repository;

import com.me.modal.ApplicationMailMapping;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ApplicationMailMapRepository extends JpaRepository<ApplicationMailMapping, Long>{

	
	List<ApplicationMailMapping>	findAllByApplicationId(String id);
	
	List<ApplicationMailMapping>  findAllBytemplateId(String templateId);
}
