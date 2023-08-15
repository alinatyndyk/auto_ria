package com.example.auto_ria.dao;

import com.example.auto_ria.enums.EBrand;
import com.example.auto_ria.enums.ERegion;
import com.example.auto_ria.models.CarSQL;
import com.example.auto_ria.models.SellerSQL;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Example;
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
    Page<CarSQL> findBySellerAndActivatedTrue(SellerSQL sellerSQL, Pageable pageable, boolean isActivated);
//    Page<CarSQL> findBySeller(SellerSQL sellerSQL, Pageable pageable);

    List<CarSQL> findAllBySeller(SellerSQL sellerSQL);

    @Query("SELECT new map(c.currency as currency, c.price as price) FROM CarSQL c WHERE c.brand = :#{#params['brand']} AND c.region = :#{#params['region']}")
    List<Map<String, Object>> findPricesByBrandAndRegion(@Param("params") HashMap<String, Object> params);

    long count();

    long countByBrandAndRegion(EBrand brand, ERegion region);

    @Override
    <S extends CarSQL> @NotNull Page<S> findAll(@NotNull Example<S> example, @NotNull Pageable pageable);
    <S extends CarSQL> @NotNull Page<S> findByActivatedIsTrue(@NotNull Example<S> example, @NotNull Pageable pageable, boolean isActivated);

}
