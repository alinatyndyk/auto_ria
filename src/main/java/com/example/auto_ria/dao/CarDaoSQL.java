package com.example.auto_ria.dao;

import com.example.auto_ria.enums.EBrand;
import com.example.auto_ria.models.CarSQL;
import com.example.auto_ria.models.user.SellerSQL;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.query.QueryByExampleExecutor;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public interface CarDaoSQL extends JpaRepository<CarSQL, Integer>, JpaSpecificationExecutor<CarSQL>, QueryByExampleExecutor<CarSQL> {

    List<CarSQL> findBySeller(SellerSQL sellerSQL);
//    List<CarSQL> findByIsActivatedIsFalse();

    @Query("SELECT c FROM CarSQL c WHERE c.isActivated = true")
    List<CarSQL> findByIsActivatedIsTrue(); //todo !!!!!!!!!!

    Page<CarSQL> findAllBySeller(SellerSQL sellerSQL, Pageable pageable);

    @Query("SELECT new map(c.currency as currency, c.price as price) " +
            "FROM CarSQL c WHERE c.brand = :#{#params['brand']} AND c.region = :#{#params['region']}")
    List<Map<String, Object>> findPricesByBrandAndRegion(@Param("params") HashMap<String, Object> params);
//todo look though model
    long count();

    long countByBrandAndRegion(EBrand brand, String region);

//    @Override
//    <S extends CarSQL> @NotNull Page<S> findAll(@NotNull Example<S> example, @NotNull Pageable pageable);

}