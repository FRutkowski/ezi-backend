package com.example.ezibackend.service;

import com.example.ezibackend.controller.dto.OrderDTO;
import com.example.ezibackend.model.Client;
import com.example.ezibackend.model.Order;
import com.example.ezibackend.model.Product;
import com.example.ezibackend.repository.OrderRepository;
import com.example.ezibackend.service.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ClientService clientService;
    private final ProductService productService;

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public Optional<Order> getOrderById(Long id) {
        return orderRepository.findById(id);
    }

    public Order createOrder(OrderDTO orderDTO) {
        Order order = new Order();
        order.setClient(clientService.getClientById(Long.valueOf(orderDTO.getClientId())).orElseThrow(() ->
                new NotFoundException(Client.class, Long.valueOf(orderDTO.getClientId()))));
        List<Product> products = orderDTO.getProductIds().stream()
                .map(productId -> productService.getProductById(Long.valueOf(productId))
                        .orElseThrow(() -> new NotFoundException(Product.class, Long.valueOf(productId))))
                .collect(Collectors.toList());

        order.setProducts(products);
        order.setDate(LocalDateTime.now());
        order.setFinalPrice(orderDTO.getFinalPrice());
        return orderRepository.save(order);
    }

    public void deleteOrder(Long id) {
        orderRepository.deleteById(id);
    }

    public List<Order> getClientOrders(Long id) {
        return orderRepository.findByClientIdOrderByDateDesc(id);
    }
}
