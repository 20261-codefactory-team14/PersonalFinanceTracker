package com.udea.FinanceTracker.controller;

import com.udea.FinanceTracker.dto.IngresoDTO;
import com.udea.FinanceTracker.dto.IngresoResponseDTO;
import com.udea.FinanceTracker.service.IngresoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ingresos")
@Tag(name = "Ingresos", description = "Operaciones REST para gestionar ingresos")
public class IngresoController {

    private final IngresoService ingresoService;

    public IngresoController(IngresoService ingresoService) {
        this.ingresoService = ingresoService;
    }

    @Operation(summary = "Crear ingreso", description = "Crea un nuevo ingreso para un usuario")
    @PostMapping
    public ResponseEntity<IngresoResponseDTO> crearIngreso(@RequestBody IngresoDTO dto) {
        return ResponseEntity.ok(ingresoService.crearIngreso(dto));
    }

    @Operation(summary = "Listar ingresos", description = "Obtiene todos los ingresos registrados")
    @GetMapping
    public ResponseEntity<List<IngresoResponseDTO>> listarIngresos() {
        return ResponseEntity.ok(ingresoService.listarIngresos());
    }

    /**
     * Obtiene un ingreso específico por su ID
     *
     * ==================== CORRECCIÓN DE ERROR 4 ====================
     * Cuando el ingreso no existe, se retorna 404 Not Found en lugar de 500
     * ==================== FIN CORRECCIÓN ====================
     */
    @Operation(summary = "Obtener ingreso por ID", description = "Obtiene un ingreso específico por su ID")
    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerIngresoPorId(@PathVariable Long id) {
        try {
            IngresoResponseDTO response = ingresoService.obtenerIngresoPorId(id);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            if (e.getMessage() != null && e.getMessage().contains("Ingreso no encontrado")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @Operation(summary = "Listar ingresos por usuario", description = "Obtiene los ingresos asociados a un usuario")
    @GetMapping("/usuario/{idUsuario}")
    public ResponseEntity<List<IngresoResponseDTO>> listarIngresosPorUsuario(@PathVariable Long idUsuario) {
        return ResponseEntity.ok(ingresoService.listarIngresosPorUsuario(idUsuario));
    }

    @Operation(summary = "Actualizar ingreso", description = "Actualiza un ingreso existente")
    @PutMapping("/{id}")
    public ResponseEntity<IngresoResponseDTO> actualizarIngreso(
            @PathVariable Long id,
            @RequestBody IngresoDTO dto
    ) {
        return ResponseEntity.ok(ingresoService.actualizarIngreso(id, dto));
    }

    @Operation(summary = "Eliminar ingreso", description = "Elimina un ingreso por su ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarIngreso(@PathVariable Long id) {
        ingresoService.eliminarIngreso(id);
        return ResponseEntity.noContent().build();
    }
}