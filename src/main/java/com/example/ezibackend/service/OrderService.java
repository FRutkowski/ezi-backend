package com.example.ezibackend.service;

import com.example.ezibackend.controller.dto.OrderDTO;
import com.example.ezibackend.model.Client;
import com.example.ezibackend.model.ClientAction;
import com.example.ezibackend.model.Order;
import com.example.ezibackend.model.Product;
import com.example.ezibackend.repository.ClientActionRepository;
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
    private final ClientActionRepository clientActionRepository;

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public Optional<Order> getOrderById(Long id) {
        return orderRepository.findById(id);
    }

    public Order createOrder(OrderDTO orderDTO) {
        Client client = clientService.getClientById(Long.valueOf(orderDTO.getClientId())).orElseThrow(() ->
                new NotFoundException(Client.class, Long.valueOf(orderDTO.getClientId())));
        LocalDateTime dateTimeNow = LocalDateTime.now();
        Order order = new Order();
        order.setClient(client);
        List<Product> products = orderDTO.getProductIds().stream()
                .map(productId -> productService.getProductById(Long.valueOf(productId))
                        .orElseThrow(() -> new NotFoundException(Product.class, Long.valueOf(productId))))
                .collect(Collectors.toList());

        order.setProducts(products);
        order.setDate(dateTimeNow);
        order.setFinalPrice(orderDTO.getFinalPrice());

        orderDTO.getProductIds().forEach(productId -> {
                    ClientAction clientAction = new ClientAction();
                    clientAction.setClient(client);
                    clientAction.setType(ClientAction.Type.BUY_PRODUCT);
                    clientAction.setDate(dateTimeNow);
                    clientAction.setObjectId(Long.valueOf(productId));
                    clientActionRepository.save(clientAction);
                });

        return orderRepository.save(order);
    }

    public void deleteOrder(Long id) {
        orderRepository.deleteById(id);
    }

    public List<Order> getClientOrders(Long id) {
        return orderRepository.findByClientIdOrderByDateDesc(id);
    }
}
