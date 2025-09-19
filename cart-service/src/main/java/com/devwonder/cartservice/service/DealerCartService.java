package com.devwonder.cartservice.service;

import com.devwonder.cartservice.dto.AddToCartRequest;
import com.devwonder.cartservice.dto.CartResponse;
import com.devwonder.cartservice.entity.DealerCart;
import com.devwonder.cartservice.entity.DealerCartId;
import com.devwonder.cartservice.repository.DealerCartRepository;
import com.devwonder.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DealerCartService {

    private final DealerCartRepository dealerCartRepository;

    @Transactional
    public CartResponse addProductToCart(AddToCartRequest request) {
        log.info("Adding product {} to dealer {} cart with quantity {}",
                request.getProductId(), request.getDealerId(), request.getQuantity());

        // Check if dealer already has this product in cart
        DealerCartId cartId = new DealerCartId(request.getDealerId(), request.getProductId());
        Optional<DealerCart> existingCart = dealerCartRepository.findById(cartId);

        if (existingCart.isPresent()) {
            // Update quantity and price
            DealerCart cart = existingCart.get();
            cart.setQuantity(cart.getQuantity() + request.getQuantity());
            cart.setUnitPrice(request.getUnitPrice());
            dealerCartRepository.save(cart);
            log.info("Updated existing cart item for dealer {} product {}, new quantity: {}, price: {}",
                    request.getDealerId(), request.getProductId(), cart.getQuantity(), cart.getUnitPrice());
        } else {
            // Create new cart item
            DealerCart newCart = DealerCart.builder()
                    .id(cartId)
                    .quantity(request.getQuantity())
                    .unitPrice(request.getUnitPrice())
                    .build();
            dealerCartRepository.save(newCart);
            log.info("Created new cart item for dealer {} product {} with quantity {} and price {}",
                    request.getDealerId(), request.getProductId(), request.getQuantity(), request.getUnitPrice());
        }

        return getDealerCart(request.getDealerId());
    }

    @Transactional(readOnly = true)
    public CartResponse getDealerCart(Long dealerId) {
        log.info("Retrieving cart for dealer {}", dealerId);

        List<DealerCart> cartItems = dealerCartRepository.findByDealerId(dealerId);

        List<CartResponse.CartItemResponse> items = cartItems.stream()
                .map(cart -> {
                    double subtotal = cart.getQuantity() * cart.getUnitPrice();
                    return CartResponse.CartItemResponse.builder()
                            .productId(cart.getId().getIdProduct())
                            .quantity(cart.getQuantity())
                            .unitPrice(cart.getUnitPrice())
                            .subtotal(subtotal)
                            .addedAt(cart.getCreatedAt())
                            .build();
                })
                .collect(Collectors.toList());

        Integer totalItems = cartItems.stream()
                .mapToInt(DealerCart::getQuantity)
                .sum();

        Double totalPrice = cartItems.stream()
                .mapToDouble(cart -> cart.getQuantity() * cart.getUnitPrice())
                .sum();

        return CartResponse.builder()
                .dealerId(dealerId)
                .items(items)
                .totalItems(totalItems)
                .totalPrice(totalPrice)
                .lastUpdated(cartItems.stream()
                        .map(DealerCart::getCreatedAt)
                        .max(java.time.LocalDateTime::compareTo)
                        .orElse(null))
                .build();
    }

    @Transactional
    public void removeProductFromCart(Long dealerId, Long productId) {
        log.info("Removing product {} from dealer {} cart", productId, dealerId);

        DealerCartId cartId = new DealerCartId(dealerId, productId);
        dealerCartRepository.deleteById(cartId);

        log.info("Removed product {} from dealer {} cart", productId, dealerId);
    }

    @Transactional
    public CartResponse updateProductQuantity(Long dealerId, Long productId, Integer quantity, Double unitPrice) {
        log.info("Updating product {} quantity to {} with price {} for dealer {}", productId, quantity, unitPrice, dealerId);

        DealerCartId cartId = new DealerCartId(dealerId, productId);
        DealerCart cart = dealerCartRepository.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found in dealer cart"));

        if (quantity <= 0) {
            dealerCartRepository.delete(cart);
            log.info("Removed product {} from dealer {} cart (quantity was 0 or negative)", productId, dealerId);
        } else {
            cart.setQuantity(quantity);
            cart.setUnitPrice(unitPrice);
            dealerCartRepository.save(cart);
            log.info("Updated product {} quantity to {} with price {} for dealer {}", productId, quantity, unitPrice, dealerId);
        }

        return getDealerCart(dealerId);
    }
}