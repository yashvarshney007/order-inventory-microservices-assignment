package com.yash.inventory.repository;

import com.yash.inventory.entity.Batch;

import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;

import java.util.List;

@Repository

public interface BatchRepository extends JpaRepository < Batch, Long > {

  List < Batch > findByProductIdOrderByExpiryDateAsc(Long productId);

  List < Batch > findByProductProductCodeOrderByExpiryDateAsc(String productCode);

  List < Batch > findByProductIdOrderByReceivedDateAsc(Long productId);

}