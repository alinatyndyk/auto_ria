package com.example.auto_ria.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.auto_ria.enums.EModel;
import com.example.auto_ria.models.CarSQL;
import com.example.auto_ria.models.user.UserSQL;

@Repository
public interface CarDaoSQL
        extends JpaRepository<CarSQL, Integer>, JpaSpecificationExecutor<CarSQL> { //check git if doesnt work

    List<CarSQL> findByUser(UserSQL userSQLSQL);

    Page<CarSQL> findAllByUser(UserSQL userSQL, Pageable pageable);

    @Query("SELECT p FROM CarSQL p WHERE p.user = :user AND p.isActivated = true")
    Page<CarSQL> findAllByUserAndActivatedTrue(@Param("user") UserSQL userSQL, Pageable pageable);

    @Query("SELECT new map(c.currency as currency, c.price as price) " +
            "FROM CarSQL c WHERE c.model = :#{#params['model']} AND c.region = :#{#params['region']}")
    List<Map<String, Object>> findPricesByBrandAndRegion(@Param("params") HashMap<String, Object> params);

    long count();

    long countByModelAndRegion(EModel model, String region);
}