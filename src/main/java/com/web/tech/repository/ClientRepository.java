package com.web.tech.repository;

import com.web.tech.model.Clients;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClientRepository extends MongoRepository<Clients, String> {
    // You can define custom queries here if needed
}

