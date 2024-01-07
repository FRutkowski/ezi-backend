package com.example.ezibackend.service;

import com.example.ezibackend.controller.dto.ProductDTO;
import com.example.ezibackend.model.Client;
import com.example.ezibackend.model.Product;
import com.example.ezibackend.repository.ProductRepository;
import com.example.ezibackend.service.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryService categoryService;

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    public Product createProduct(ProductDTO productDTO, MultipartFile photo) throws IOException {
        Product product = new Product();
        product.setPrice(productDTO.getPrice());
        product.setName(productDTO.getName());
        product.setPhoto(photo.getBytes());
        product.setCategory(categoryService.getCategoryById(Long.valueOf(productDTO.getCategoryId())).orElseThrow(() ->
                new NotFoundException(Client.class, Long.valueOf(productDTO.getCategoryId()))));
        return productRepository.save(product);
    }

    public Product updateProduct(Long id, ProductDTO updatedProduct, MultipartFile photo) throws IOException {
        Optional<Product> existingProduct = productRepository.findById(id);
        if (existingProduct.isPresent()) {
            Product product = existingProduct.get();
            product.setName(updatedProduct.getName());
            product.setPrice(updatedProduct.getPrice());
            if (photo != null) {
                product.setPhoto(photo.getBytes());
            }
            product.setPhoto(updatedProduct.getPhoto());
            product.setCategory(categoryService.getCategoryById(Long.valueOf(updatedProduct.getCategoryId()))
                    .orElseThrow(() -> new NotFoundException(Client.class, Long.valueOf(updatedProduct.getCategoryId()))));
            return productRepository.save(product);
        }
        return null;
    }

    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }
}

