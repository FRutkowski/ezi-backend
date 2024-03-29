package com.example.ezibackend.service;

import com.example.ezibackend.controller.dto.ProductDTO;
import com.example.ezibackend.model.Category;
import com.example.ezibackend.model.Client;
import com.example.ezibackend.model.ClientAction;
import com.example.ezibackend.model.Product;
import com.example.ezibackend.repository.ClientActionRepository;
import com.example.ezibackend.repository.ProductRepository;
import com.example.ezibackend.service.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Log4j2
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryService categoryService;
    private final ClientActionRepository clientActionRepository;

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

    public List<Product> getDetailsSuggestion(Long id, Long clientId) {
        Category productCategory = getProductById(id).orElseThrow(() -> new NotFoundException(Client.class, id)).getCategory();
        log.info("suggestion for product with id: " + id + " with category: " + productCategory.getName());

        List<Product> sameCategoryProductIds = productRepository.getProductsByCategoryAndIdNot(productCategory, id);
        log.info("all products from this category: " + sameCategoryProductIds.stream().map(Product::getId).toList());

        List<ClientAction> viewProductActions = clientActionRepository.getClientActionByClientIdAndTypeAndDateAfterAndObjectIdIn(
                clientId, ClientAction.Type.VIEW_PRODUCT, LocalDateTime.now().minusMonths(3), sameCategoryProductIds.stream().map(Product::getId).toList());
        log.info("view product ids: " + viewProductActions.stream().map(ClientAction::getObjectId).toList());

        List<ClientAction> buyProductActions = clientActionRepository.getClientActionByClientIdAndTypeAndDateAfterAndObjectIdIn(
                clientId, ClientAction.Type.BUY_PRODUCT, LocalDateTime.now().minusMonths(3), sameCategoryProductIds.stream().map(Product::getId).toList());
        log.info("buy product ids: " + buyProductActions.stream().map(ClientAction::getObjectId).toList());

        // excluded products that were bought after viewing it
        List<ClientAction> filteredViewProductActions = viewProductActions.stream()
                .filter(view -> buyProductActions.stream()
                        .noneMatch(buy -> Objects.equals(buy.getObjectId(), view.getObjectId()) &&
                                buy.getDate().isAfter(view.getDate())))
                .toList();
        log.info("product ids without buy after view: " + filteredViewProductActions.stream().map(ClientAction::getObjectId).toList());

        // counted view action for each product
        Map<Long, Long> productViewCounts = filteredViewProductActions.stream()
                .collect(Collectors.groupingBy(ClientAction::getObjectId, Collectors.counting()));
        log.info("product ids view count (id:count): " + productViewCounts.entrySet().stream()
                .map(entry -> entry.getKey() + ":" + entry.getValue())
                .collect(Collectors.joining(", ")));

        // sorted by view action count
        List<Product> mostViewedProduct = new ArrayList<>(productViewCounts.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .map(Map.Entry::getKey)
                .map(productRepository::findById)
                .map(Optional::orElseThrow)
                .toList());
        log.info("product ids sorted by count: " + mostViewedProduct.stream().map(Product::getId).toList());

        // added rest of product from the same category (bought products placed at the end of the list.)
        List<Product> sortedProducts = sameCategoryProductIds.stream()
                .sorted(Comparator.comparing(product -> buyProductActions.stream().map(ClientAction::getObjectId).toList().contains(product.getId())))
                .toList();
        log.info("all products from this category (without previous sort, bought products at the end): " + sortedProducts.stream().map(Product::getId).toList());

        mostViewedProduct.addAll(sortedProducts);
        mostViewedProduct = mostViewedProduct.stream().distinct().toList();
        log.info("final results: " + mostViewedProduct.stream().map(Product::getId).toList());
        return mostViewedProduct;
    }
}

