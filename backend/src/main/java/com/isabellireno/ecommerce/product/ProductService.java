package com.isabellireno.ecommerce.product;

import com.isabellireno.ecommerce.category.Category;
import com.isabellireno.ecommerce.category.CategoryRepository;
import com.isabellireno.ecommerce.common.BusinessException;
import com.isabellireno.ecommerce.common.NotFoundException;
import com.isabellireno.ecommerce.common.PageResponse;
import com.isabellireno.ecommerce.product.ProductDtos.ProductFilter;
import com.isabellireno.ecommerce.product.ProductDtos.ProductRequest;
import com.isabellireno.ecommerce.product.ProductDtos.ProductResponse;
import com.isabellireno.ecommerce.storage.StorageService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import static com.isabellireno.ecommerce.product.ProductSpecifications.inCategory;
import static com.isabellireno.ecommerce.product.ProductSpecifications.inStock;
import static com.isabellireno.ecommerce.product.ProductSpecifications.isActive;
import static com.isabellireno.ecommerce.product.ProductSpecifications.nameContains;
import static com.isabellireno.ecommerce.product.ProductSpecifications.priceAtLeast;
import static com.isabellireno.ecommerce.product.ProductSpecifications.priceAtMost;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final StorageService storageService;

    public ProductService(ProductRepository productRepository, CategoryRepository categoryRepository,
                          StorageService storageService) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.storageService = storageService;
    }

    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> search(ProductFilter filter, Pageable pageable, boolean includeInactive) {
        if (filter.minPrice() != null && filter.maxPrice() != null
                && filter.minPrice().compareTo(filter.maxPrice()) > 0) {
            throw new IllegalArgumentException("minPrice não pode ser maior que maxPrice.");
        }
        Specification<Product> spec = Specification.allOf(
                includeInactive ? null : isActive(),
                nameContains(filter.q()),
                inCategory(filter.categoryId()),
                priceAtLeast(filter.minPrice()),
                priceAtMost(filter.maxPrice()),
                inStock(filter.inStock()));
        return PageResponse.of(productRepository.findAll(spec, pageable), ProductResponse::from);
    }

    @Transactional(readOnly = true)
    public ProductResponse findActive(Long id) {
        Product product = getOrThrow(id);
        if (!product.isActive()) {
            throw new NotFoundException("Produto não encontrado.");
        }
        return ProductResponse.from(product);
    }

    @Transactional
    public ProductResponse create(ProductRequest request) {
        Product product = new Product(request.name().trim(), request.description(), request.price(),
                request.stock(), getCategory(request.categoryId()));
        if (request.active() != null) {
            product.setActive(request.active());
        }
        return ProductResponse.from(productRepository.save(product));
    }

    @Transactional
    public ProductResponse update(Long id, ProductRequest request) {
        Product product = getOrThrow(id);
        product.setName(request.name().trim());
        product.setDescription(request.description());
        product.setPrice(request.price());
        product.setStock(request.stock());
        product.setCategory(getCategory(request.categoryId()));
        if (request.active() != null) {
            product.setActive(request.active());
        }
        return ProductResponse.from(product);
    }

    /** "Exclusão" lógica: o produto some do catálogo mas continua nos pedidos antigos. */
    @Transactional
    public void deactivate(Long id) {
        getOrThrow(id).setActive(false);
    }

    @Transactional
    public ProductResponse uploadImage(Long id, MultipartFile file) {
        Product product = getOrThrow(id);
        String oldImage = product.getImageUrl();
        product.setImageUrl(storageService.store(file));
        if (oldImage != null) {
            storageService.delete(oldImage);
        }
        return ProductResponse.from(product);
    }

    private Product getOrThrow(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Produto não encontrado."));
    }

    private Category getCategory(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new BusinessException("Categoria inválida."));
    }
}
