package com.example.auto_ria.dao;

import com.example.auto_ria.enums.EBrand;
import com.example.auto_ria.enums.EModel;
import com.example.auto_ria.models.CarSQL;
import com.example.auto_ria.models.user.SellerSQL;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.query.QueryByExampleExecutor;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public interface CarDaoSQL extends JpaRepository<CarSQL, Integer>, JpaSpecificationExecutor<CarSQL>, QueryByExampleExecutor<CarSQL> {

    List<CarSQL> findBySeller(SellerSQL sellerSQL);

    Page<CarSQL> findAllBySeller(SellerSQL sellerSQL, Pageable pageable);

    @Query("SELECT p FROM CarSQL p WHERE p.seller = :seller AND p.isActivated = true")
    Page<CarSQL> findAllBySellerAndActivatedTrue(@Param("seller") SellerSQL sellerSQL, Pageable pageable);

    @Query("SELECT new map(c.currency as currency, c.price as price) " +
            "FROM CarSQL c WHERE c.model = :#{#params['model']} AND c.region = :#{#params['region']}")
    List<Map<String, Object>> findPricesByBrandAndRegion(@Param("params") HashMap<String, Object> params);

    long count();

    long countByModelAndRegion(EModel model, String region);
}