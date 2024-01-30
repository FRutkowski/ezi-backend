package com.example.ezibackend.repository;

import com.example.ezibackend.model.Category;
import com.example.ezibackend.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>{

    List<Product> getProductsByCategoryAndIdNot(Category productCategory, Long id);

    List<Product> findByIdIn(List<Long> productIds);
}
