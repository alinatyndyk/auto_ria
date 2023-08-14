package com.example.auto_ria.dao;

import com.example.auto_ria.models.CarSQL;
import com.example.auto_ria.models.SellerSQL;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.QueryByExampleExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CarDaoSQL extends JpaRepository<CarSQL, Integer>, JpaSpecificationExecutor<CarSQL>, QueryByExampleExecutor<CarSQL> {
    List<CarSQL> findBySeller(SellerSQL sellerSQL);

    @Query("SELECT c.price, c.currency FROM CarSQL c")
    List<Object[]> findAllPricesAndCurrencies();
    @Override
    <S extends CarSQL> Page<S> findAll(Example<S> example, Pageable pageable);

}
