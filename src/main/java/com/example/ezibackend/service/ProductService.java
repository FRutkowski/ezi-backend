package com.example.ezibackend.service;

import com.example.ezibackend.controller.dto.CartDTO;
import com.example.ezibackend.controller.dto.CommonProductsOccurrencesDTO;
import com.example.ezibackend.controller.dto.ProductDTO;
import com.example.ezibackend.model.*;
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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.example.ezibackend.model.Product.SuggestProductType.AND;
import static com.example.ezibackend.model.Product.SuggestProductType.OR;

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

    public List<Product> getORCategoryProducts(CartDTO cartDTO, List<Order> orders) {
        List<Product> products = getMostOftenBoughtWithProducts(
                cartDTO,
                orders,
                Product.SuggestProductType.OR
        );

        List<Product> product = productRepository.findByIdIn(cartDTO.getProductIds());
        List<Product> sameCategoryProducts = new ArrayList<Product>();
        for (int i = 0; i < products.size(); i++) {
            if (product.get(0).getCategory().getId().equals(products.get(i).getCategory().getId())) {
                sameCategoryProducts.add(products.get(i));
            }
        }

        return sameCategoryProducts;
    }

    public List<Product> getMostOftenBoughtWithProducts(CartDTO cartDTO, List<Order> orders, Product.SuggestProductType suggestProductType) {
        List<Product> cartsProducts = productRepository.findByIdIn(cartDTO.getProductIds());
        List<Order> ordersClone = new ArrayList<>(orders);
        log.info("Zawartość koszyka: ");
        // NOTE: Loop for debugging
        for (int i = 0; i < cartsProducts.size(); i++) {
            log.info("Product " + i);
            log.info("      ID: " + cartsProducts.get(i).getId());
            log.info("      Nazwa: " + cartsProducts.get(i).getName());
            log.info("      Kategoria: " + cartsProducts.get(i).getCategory());
            log.info("      Cena: " + cartsProducts.get(i).getPrice());
        }

//                log.info("Zamówienia: ");
//                log.info(orders);
        // NOTE: Loop for debugging
        for (int i = 0; i < orders.size(); i++) {
            log.info("Zamówienie " + i);
            log.info("      ID: " + orders.get(i).getId());
            log.info("      Data: " + orders.get(i).getDate());
            log.info("      Klient: " + orders.get(i).getClient());
            log.info("      Finalna cena: " + orders.get(i).getFinalPrice());
            List<Product> products = orders.get(i).getProducts();

            // NOTE: Loop for debugging
            for (int z = 0; z < products.size(); z++) {
                log.info("      Product " + z);
                log.info("            ID: " + products.get(z).getId());
                log.info("            Nazwa: " + products.get(z).getName());
                log.info("            Kategoria: " + products.get(z).getCategory());
                log.info("            Cena: " + products.get(z).getPrice());
            }
        }

        switch (suggestProductType) {
            case AND:
                List<Product> productsToSuggest = findProductsToSuggestAND(cartsProducts, ordersClone);
                if (productsToSuggest.size() > 5) {
                    log.info("Lista produktów przed zwróceniem: ");
                    for (int z = 0; z < productsToSuggest.size(); z++) {
                        log.info("      Product " + z);
                        log.info("            ID: " + productsToSuggest.get(z).getId());
                        log.info("            Nazwa: " + productsToSuggest.get(z).getName());
                        log.info("            Kategoria: " + productsToSuggest.get(z).getCategory());
                        log.info("            Cena: " + productsToSuggest.get(z).getPrice());
                    }

                    return productsToSuggest;
                }
            case OR:
                return findProductsToSuggestOR(cartsProducts, ordersClone);
        }

        return null;
    }

    public List<Product> findProductsToSuggestAND(List<Product> cartsProducts, List<Order> orders) {
        log.info("AND:");
        HashMap<Long, Integer> productOccurrences = new HashMap<>();
        List<Order> ordersClone = new ArrayList<>(orders);
        for (int i = 0; i < ordersClone.size(); ++i) {
            List<Product> currentOrderProducts = ordersClone.get(i).getProducts();
            log.info("Iteracja: " + i);
            log.info("aktualna pula produktów: ");
            // NOTE: Loop for debugging
            for (int z = 0; z < currentOrderProducts.size(); z++) {
                log.info("Product " + z);
                log.info("      ID: " + currentOrderProducts.get(z).getId());
                log.info("      Nazwa: " + currentOrderProducts.get(z).getName());
                log.info("      Kategoria: " + currentOrderProducts.get(z).getCategory());
                log.info("      Cena: " + currentOrderProducts.get(z).getPrice());
            }

            if (new HashSet<>(currentOrderProducts).containsAll(cartsProducts)) {
                currentOrderProducts.removeAll(cartsProducts);

                log.info("Pozostałe produkty: ");
                // NOTE: Loop for debugging
                for (int z = 0; z < currentOrderProducts.size(); z++) {
                    log.info("Product");
                    log.info("      ID: " + currentOrderProducts.get(z).getId());
                    log.info("      Nazwa: " + currentOrderProducts.get(z).getName());
                    log.info("      Kategoria: " + currentOrderProducts.get(z).getCategory());
                    log.info("      Cena: " + currentOrderProducts.get(z).getPrice());
                }

                addOccurredProductsToMap(productOccurrences, currentOrderProducts);
                log.info("Wystąpienia produktów pod dodaniu do mapy: ");
                log.info(productOccurrences);
            }
        }

        log.info("Mapa po przejściach");
        log.info(productOccurrences);

        List<Product> productsToReturn = productOccurrences.entrySet().stream()
                .sorted(Map.Entry.<Long, Integer>comparingByValue().reversed())
                .map(entry -> getProductById(entry.getKey()).orElseThrow())
                .toList();

        return productsToReturn;
    }

    public void addOccurredProductsToMap(HashMap<Long, Integer> productOccurrences , List<Product> products) {
        for (Product product : products) {
            int occurrences = productOccurrences.getOrDefault(product.getId(), 0);
            productOccurrences.put(product.getId(), occurrences + 1);
        }
    }

    public List<Product> findProductsToSuggestOR(List<Product> cartsProducts, List<Order> orders) {
        List<CommonProductsOccurrencesDTO> commonProductsOccurrences = new ArrayList<>();

        log.info("OR: ");
        for (int i = 0; i < orders.size(); i++) {
            List<Product> currentOrderProducts = orders.get(i).getProducts();
            AtomicInteger amountOfCommonProducts = new AtomicInteger(0);
            log.info("Iteracja: " + i);
            log.info("aktualna pula produktów: ");
            // NOTE: Loop for debugging
            for (int z = 0; z < currentOrderProducts.size(); z++) {
                log.info("Product " + z);
                log.info("      ID: " + currentOrderProducts.get(z).getId());
                log.info("      Nazwa: " + currentOrderProducts.get(z).getName());
                log.info("      Kategoria: " + currentOrderProducts.get(z).getCategory());
                log.info("      Cena: " + currentOrderProducts.get(z).getPrice());
            }

            cartsProducts.stream()
                    .filter(currentOrderProducts::contains)
                    .forEach(product -> {
                        currentOrderProducts.remove(product);
                        amountOfCommonProducts.incrementAndGet();
                    });

            log.info("Pozostałe produkty po usunięciu tych które sie zawierały: ");
            // NOTE: Loop for debugging
            for (int z = 0; z < currentOrderProducts.size(); z++) {
                log.info("Product " + z);
                log.info("      ID: " + currentOrderProducts.get(z).getId());
                log.info("      Nazwa: " + currentOrderProducts.get(z).getName());
                log.info("      Kategoria: " + currentOrderProducts.get(z).getCategory());
                log.info("      Cena: " + currentOrderProducts.get(z).getPrice());
            }


            log.info("Liczba wspólnych produktów ");
            log.info(amountOfCommonProducts);

            log.info("Common products occurrences przed ");
            commonProductsOccurrences.forEach(currentProductOccurrence -> {
                log.info("prId: " + currentProductOccurrence.getProductId() +
                        " commonPrd: " + currentProductOccurrence.getAmountOfCommonProducts() +
                        " occurrences: " + currentProductOccurrence.getAmountOfOccurrences());
            });

            log.info(amountOfCommonProducts);
            currentOrderProducts
                    .forEach(currentProduct -> commonProductsOccurrences.stream()
                            .filter(product -> product.getProductId().equals(currentProduct.getId()) &&
                                    product.getAmountOfCommonProducts() == amountOfCommonProducts.get())
                            .findFirst()
                            .ifPresentOrElse(foundProduct -> {
                                foundProduct.setAmountOfOccurrences(foundProduct.getAmountOfOccurrences() + 1);
                            }, () -> {
                                commonProductsOccurrences.add(new CommonProductsOccurrencesDTO(currentProduct.getId(), amountOfCommonProducts.get(), 1));
                            }));


        }

        List<CommonProductsOccurrencesDTO> commonProductsOccurrencesSorted = commonProductsOccurrences.stream()
                .sorted(Comparator.comparing(CommonProductsOccurrencesDTO::getAmountOfCommonProducts).reversed()
                        .thenComparing(Comparator.comparing(CommonProductsOccurrencesDTO::getAmountOfOccurrences).reversed()))
                .toList();

        log.info("Common products occurrences po ");
        commonProductsOccurrencesSorted.forEach(currentProductOccurrence -> {
            log.info("prId: " + currentProductOccurrence.getProductId() +
                    " commonPrd: " + currentProductOccurrence.getAmountOfCommonProducts() +
                    " occurrences: " + currentProductOccurrence.getAmountOfOccurrences());
        });

        List<Product> sortedProducts = commonProductsOccurrencesSorted.stream()
                .map(CommonProductsOccurrencesDTO::getProductId)
                .map(productId -> getProductById(productId).orElse(null))
                .filter(Objects::nonNull)
                .toList();

        return sortedProducts;
    }
}
