package com.udea.FinanceTracker.controller;

import com.udea.FinanceTracker.dto.ActualizarCategoriaRequest;
import com.udea.FinanceTracker.dto.CategoriaDTO;
import com.udea.FinanceTracker.dto.CrearCategoriaRequest;
import com.udea.FinanceTracker.service.CategoriaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/categories")
public class CategoriaController {

    private final CategoriaService categoriaService;

    public CategoriaController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    @Operation(
            summary = "Create expense category",
            description = "Creates a new expense category.",
            security = { @SecurityRequirement(name = "bearerAuth") }
    )
    @PostMapping
    public ResponseEntity<?> crearCategoria(
            @Parameter(hidden = true) @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody CrearCategoriaRequest request) {
        try {
            CategoriaDTO categoria = categoriaService.crearCategoria(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(categoria);

        } catch (Exception e) {
            return construirError(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @Operation(
            summary = "Update expense category",
            description = "Updates an existing expense category.",
            security = { @SecurityRequirement(name = "bearerAuth") }
    )
    @PutMapping("/{categoryId}")
    public ResponseEntity<?> actualizarCategoria(
            @Parameter(hidden = true) @RequestHeader("Authorization") String authHeader,
            @Parameter(description = "Category ID", example = "1", required = true)
            @PathVariable Long categoryId,
            @Valid @RequestBody ActualizarCategoriaRequest request) {
        try {
            CategoriaDTO categoria = categoriaService.actualizarCategoria(categoryId, request);
            return ResponseEntity.ok(categoria);

        } catch (Exception e) {
            return construirError(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @Operation(
            summary = "Get categories",
            description = "Returns all expense categories.",
            security = { @SecurityRequirement(name = "bearerAuth") }
    )
    @GetMapping
    public ResponseEntity<?> obtenerCategorias(
            @Parameter(hidden = true) @RequestHeader("Authorization") String authHeader) {
        try {
            List<CategoriaDTO> categorias = categoriaService.obtenerCategorias();
            return ResponseEntity.ok(categorias);

        } catch (Exception e) {
            return construirError(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @Operation(
            summary = "Get category by ID",
            description = "Returns a specific expense category.",
            security = { @SecurityRequirement(name = "bearerAuth") }
    )
    @GetMapping("/{categoryId}")
    public ResponseEntity<?> obtenerCategoriaPorId(
            @Parameter(hidden = true) @RequestHeader("Authorization") String authHeader,
            @Parameter(description = "Category ID", example = "1", required = true)
            @PathVariable Long categoryId) {
        try {
            CategoriaDTO categoria = categoriaService.obtenerCategoriaPorId(categoryId);
            return ResponseEntity.ok(categoria);

        } catch (Exception e) {
            return construirError(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @Operation(
            summary = "Delete category",
            description = "Deletes an expense category.",
            security = { @SecurityRequirement(name = "bearerAuth") }
    )
    @DeleteMapping("/{categoryId}")
    public ResponseEntity<?> eliminarCategoria(
            @Parameter(hidden = true) @RequestHeader("Authorization") String authHeader,
            @Parameter(description = "Category ID", example = "1", required = true)
            @PathVariable Long categoryId) {
        try {
            categoriaService.eliminarCategoria(categoryId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Category deleted successfully");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return construirError(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    private ResponseEntity<?> construirError(String mensaje, HttpStatus status) {
        Map<String, String> error = new HashMap<>();
        error.put("error", mensaje);
        return ResponseEntity.status(status).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> errores = new HashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(fieldError ->
                errores.put(fieldError.getField(), fieldError.getDefaultMessage())
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errores);
    }
}