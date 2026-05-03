package com.udea.FinanceTracker.controller;

import com.udea.FinanceTracker.dto.GastoDTO;
import com.udea.FinanceTracker.dto.GastoResponseDTO;
import com.udea.FinanceTracker.service.GastoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/gastos")
@Tag(name = "Gastos", description = "Operaciones REST para gestionar gastos")
public class GastoController {

    private final GastoService gastoService;

    public GastoController(GastoService gastoService) {
        this.gastoService = gastoService;
    }

    @Operation(summary = "Crear gasto", description = "Crea un nuevo gasto asociado a un usuario y una categoría")
    @PostMapping
    public ResponseEntity<GastoResponseDTO> crearGasto(@RequestBody GastoDTO dto) {
        return ResponseEntity.ok(gastoService.crearGasto(dto));
    }

    @Operation(summary = "Listar gastos", description = "Obtiene todos los gastos registrados")
    @GetMapping
    public ResponseEntity<List<GastoResponseDTO>> listarGastos() {
        return ResponseEntity.ok(gastoService.listarGastos());
    }

    @Operation(summary = "Obtener gasto por ID", description = "Obtiene un gasto específico por su ID")
    @GetMapping("/{id}")
    public ResponseEntity<GastoResponseDTO> obtenerGastoPorId(@PathVariable Long id) {
        return ResponseEntity.ok(gastoService.obtenerGastoPorId(id));
    }

    @Operation(summary = "Listar gastos por usuario", description = "Obtiene los gastos asociados a un usuario")
    @GetMapping("/usuario/{idUsuario}")
    public ResponseEntity<List<GastoResponseDTO>> listarGastosPorUsuario(@PathVariable Long idUsuario) {
        return ResponseEntity.ok(gastoService.listarGastosPorUsuario(idUsuario));
    }

    @Operation(summary = "Listar gastos por categoría", description = "Obtiene los gastos asociados a una categoría")
    @GetMapping("/categoria/{idCategoria}")
    public ResponseEntity<List<GastoResponseDTO>> listarGastosPorCategoria(@PathVariable Long idCategoria) {
        return ResponseEntity.ok(gastoService.listarGastosPorCategoria(idCategoria));
    }

    @Operation(summary = "Actualizar gasto", description = "Actualiza un gasto existente")
    @PutMapping("/{id}")
    public ResponseEntity<GastoResponseDTO> actualizarGasto(
            @PathVariable Long id,
            @RequestBody GastoDTO dto
    ) {
        return ResponseEntity.ok(gastoService.actualizarGasto(id, dto));
    }

    @Operation(summary = "Eliminar gasto", description = "Elimina un gasto por su ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarGasto(@PathVariable Long id) {
        gastoService.eliminarGasto(id);
        return ResponseEntity.noContent().build();
    }
}