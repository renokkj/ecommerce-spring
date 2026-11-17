package com.isabellireno.ecommerce.product;

import com.isabellireno.ecommerce.common.PageResponse;
import com.isabellireno.ecommerce.product.ProductDtos.ProductFilter;
import com.isabellireno.ecommerce.product.ProductDtos.ProductRequest;
import com.isabellireno.ecommerce.product.ProductDtos.ProductResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

@RestController
@Tag(name = "Produtos")
public class ProductController {

    private static final int MAX_PAGE_SIZE = 50;
    private static final Set<String> SORTABLE = Set.of("name", "price", "createdAt");

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/api/products")
    @Operation(summary = "Catálogo paginado com filtros",
            description = "Ordenação: sort=name|price|createdAt e direction=asc|desc. Tamanho máximo da página: 50.")
    public PageResponse<ProductResponse> list(@ParameterObject ProductFilter filter,
                                              @RequestParam(defaultValue = "0") int page,
                                              @RequestParam(defaultValue = "12") int size,
                                              @RequestParam(defaultValue = "name") String sort,
                                              @RequestParam(defaultValue = "asc") String direction) {
        return productService.search(filter, pageable(page, size, sort, direction), false);
    }

    @GetMapping("/api/products/{id}")
    @Operation(summary = "Detalhe de um produto ativo")
    public ProductResponse get(@PathVariable Long id) {
        return productService.findActive(id);
    }

    @GetMapping("/api/admin/products")
    @Operation(summary = "Lista todos os produtos, inclusive inativos (admin)")
    public PageResponse<ProductResponse> adminList(@ParameterObject ProductFilter filter,
                                                   @RequestParam(defaultValue = "0") int page,
                                                   @RequestParam(defaultValue = "20") int size,
                                                   @RequestParam(defaultValue = "createdAt") String sort,
                                                   @RequestParam(defaultValue = "desc") String direction) {
        return productService.search(filter, pageable(page, size, sort, direction), true);
    }

    @PostMapping("/api/admin/products")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cria um produto (admin)")
    public ProductResponse create(@Valid @RequestBody ProductRequest request) {
        return productService.create(request);
    }

    @PutMapping("/api/admin/products/{id}")
    @Operation(summary = "Atualiza um produto (admin)")
    public ProductResponse update(@PathVariable Long id, @Valid @RequestBody ProductRequest request) {
        return productService.update(id, request);
    }

    @DeleteMapping("/api/admin/products/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Desativa um produto (admin)")
    public void delete(@PathVariable Long id) {
        productService.deactivate(id);
    }

    @PostMapping(value = "/api/admin/products/{id}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Envia a imagem do produto (JPEG, PNG ou WebP, até 5 MB) (admin)")
    public ProductResponse uploadImage(@PathVariable Long id, @RequestPart("file") MultipartFile file) {
        return productService.uploadImage(id, file);
    }

    private Pageable pageable(int page, int size, String sort, String direction) {
        if (page < 0) {
            throw new IllegalArgumentException("page deve ser maior ou igual a 0.");
        }
        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw new IllegalArgumentException("size deve estar entre 1 e " + MAX_PAGE_SIZE + ".");
        }
        if (!SORTABLE.contains(sort)) {
            throw new IllegalArgumentException("sort deve ser um de: " + SORTABLE + ".");
        }
        Sort.Direction dir = "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;
        return PageRequest.of(page, size, Sort.by(dir, sort).and(Sort.by("id")));
    }
}
