package com.example.ezibackend.controller;

import com.example.ezibackend.controller.dto.ProductDTO;
import com.example.ezibackend.model.Client;
import com.example.ezibackend.model.ClientAction;
import com.example.ezibackend.model.Order;
import com.example.ezibackend.model.Product;
import com.example.ezibackend.repository.ClientActionRepository;
import com.example.ezibackend.service.ClientService;
import com.example.ezibackend.service.OrderService;
import com.example.ezibackend.service.ProductService;
import com.example.ezibackend.service.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final OrderService orderService;
    private final ClientService clientService;
    private final ClientActionRepository clientActionRepository;

    @GetMapping
    public List<Product> getAllProducts() {
        return productService.getAllProducts();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id, @RequestParam(name = "clientId") Long clientId) {
        Optional<Product> product = productService.getProductById(id);
        Client client = clientService.getClientById(clientId).orElseThrow(() ->
                new NotFoundException(Client.class, clientId));

        if (product.isPresent()) {
            ClientAction clientAction = new ClientAction();
            clientAction.setClient(client);
            clientAction.setType(ClientAction.Type.VIEW_PRODUCT);
            clientAction.setDate(LocalDateTime.now());
            clientAction.setObjectId(id);
            clientActionRepository.save(clientAction);
        }
        return product.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PostMapping
    public ResponseEntity<Product> createProduct(@RequestPart("product") ProductDTO product,
                                                 @RequestPart("photo") MultipartFile photo) throws IOException {
        Product createdProduct = productService.createProduct(product, photo);
        return new ResponseEntity<>(createdProduct, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable Long id, @RequestPart("product") ProductDTO updatedProduct,
                                                 @RequestPart(value = "photo", required = false) MultipartFile photo) throws IOException {
        Product product = productService.updateProduct(id, updatedProduct, photo);
        return product != null ? new ResponseEntity<>(product, HttpStatus.OK)
                : new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("details-suggestion/{id}")
    public List<Product> getDetailsSuggestion(@PathVariable Long id, @RequestParam(name = "clientId") Long clientId) {
        List<Order> orders = orderService.getAllOrders();
        return productService.getDetailsSuggestion(id, clientId);
    }

    @GetMapping("/cart-suggestion")
    public List<Product> getRelatedProducts(@RequestParam(name = "clientId") Long clientId) {
        List<Order> orders = orderService.getClientOrders(clientId);
        List<Product> cartsProducts = new ArrayList<>();
        Optional<Product> firstProduct = productService.getProductById(5L);
        cartsProducts.add(firstProduct.orElseGet(null));

        Optional<Product> secondProduct = productService.getProductById(12L);
        cartsProducts.add(secondProduct.orElseGet(null));

        Optional<Product> thirdProduct = productService.getProductById(8L);
        cartsProducts.add(thirdProduct.orElseGet(null));
//        Optional<Product> thirdProduct = productService.getProductById(5L);
//        cartsProducts.add(thirdProduct.orElseGet(null));

        return productService.getMostOftenBoughtWithProducts(
                cartsProducts,
                orders,
                cartsProducts.size() > 5 ? Product.SuggestProductType.OR : Product.SuggestProductType.AND
        );
    }
}
