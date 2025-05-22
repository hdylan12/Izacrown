package com.web.tech.repository;

import com.web.tech.model.Orders;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends MongoRepository<Orders, String> {
    Optional<Orders> findByOrderNumber(String orderNumber);
    List<Orders> findByUserId(String userId);
}