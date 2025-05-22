package com.web.tech.repository;

import com.web.tech.model.Products;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.math.BigDecimal;
import java.util.List;

public interface ProductRepository extends MongoRepository<Products, String> {

    List<Products> findByNameContainingIgnoreCase(String name);

    Page<Products> findByNameContainingIgnoreCase(String name, Pageable pageable);

    List<Products> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);

    Page<Products> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);

    List<Products> findByNameContainingIgnoreCaseAndPriceBetween(
            String name, BigDecimal minPrice, BigDecimal maxPrice);

    Page<Products> findByNameContainingIgnoreCaseAndPriceBetween(
            String name, BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);

    @Query("{$and: [" +
            "{ $or: [ { name: { $regex: ?0, $options: 'i' } }, { name: null } ] }," +
            "{ $or: [ { price: { $gte: ?1 } }, { price: null } ] }," +
            "{ $or: [ { price: { $lte: ?2 } }, { price: null } ] }" +
            "]}")
    Page<Products> findByNameAndPriceRange(
            String name, BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);

    @Query(value = "{ 'category': ?0 }", count = true)
    Long countByCategory(String category);

    @Query("{ stock: { $lte: ?0 } }")
    Long countByStockLessThanEqual(int stock);
}