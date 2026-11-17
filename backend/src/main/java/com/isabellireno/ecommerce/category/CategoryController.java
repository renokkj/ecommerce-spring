package com.isabellireno.ecommerce.category;

import com.isabellireno.ecommerce.common.BusinessException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Tag(name = "Categorias")
public class CategoryController {

    public record CategoryRequest(@NotBlank(message = "Informe o nome.") @Size(max = 80) String name) {}

    public record CategoryResponse(Long id, String name) {
        static CategoryResponse from(Category c) {
            return new CategoryResponse(c.getId(), c.getName());
        }
    }

    private final CategoryRepository repository;

    public CategoryController(CategoryRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/api/categories")
    @Operation(summary = "Lista as categorias")
    public List<CategoryResponse> list() {
        return repository.findAllByOrderByNameAsc().stream().map(CategoryResponse::from).toList();
    }

    @PostMapping("/api/admin/categories")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cria uma categoria (admin)")
    public CategoryResponse create(@Valid @RequestBody CategoryRequest request) {
        String name = request.name().trim();
        if (repository.existsByNameIgnoreCase(name)) {
            throw new BusinessException("Categoria já existe.");
        }
        return CategoryResponse.from(repository.save(new Category(name)));
    }
}
