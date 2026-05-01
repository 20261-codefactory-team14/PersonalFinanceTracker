package com.udea.FinanceTracker.controller;

import com.udea.FinanceTracker.dto.IngresoDTO;
import com.udea.FinanceTracker.entity.Ingreso;
import com.udea.FinanceTracker.service.IngresoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public ResponseEntity<Ingreso> crearIngreso(@RequestBody IngresoDTO dto) {
        return ResponseEntity.ok(ingresoService.crearIngreso(dto));
    }

    @Operation(summary = "Listar ingresos", description = "Obtiene todos los ingresos registrados")
    @GetMapping
    public ResponseEntity<List<Ingreso>> listarIngresos() {
        return ResponseEntity.ok(ingresoService.listarIngresos());
    }

    @Operation(summary = "Obtener ingreso por ID", description = "Obtiene un ingreso específico por su ID")
    @GetMapping("/{id}")
    public ResponseEntity<Ingreso> obtenerIngresoPorId(@PathVariable Long id) {
        return ResponseEntity.ok(ingresoService.obtenerIngresoPorId(id));
    }

    @Operation(summary = "Listar ingresos por usuario", description = "Obtiene los ingresos asociados a un usuario")
    @GetMapping("/usuario/{idUsuario}")
    public ResponseEntity<List<Ingreso>> listarIngresosPorUsuario(@PathVariable Long idUsuario) {
        return ResponseEntity.ok(ingresoService.listarIngresosPorUsuario(idUsuario));
    }

    @Operation(summary = "Actualizar ingreso", description = "Actualiza un ingreso existente")
    @PutMapping("/{id}")
    public ResponseEntity<Ingreso> actualizarIngreso(
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