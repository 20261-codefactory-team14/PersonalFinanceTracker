package com.udea.FinanceTracker.controller;

import com.udea.FinanceTracker.dto.CrearPresupuestoRequest;
import com.udea.FinanceTracker.dto.PresupuestoDTO;
import com.udea.FinanceTracker.service.PresupuestoService;
import com.udea.FinanceTracker.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/presupuesto")
public class PresupuestoController {

    private final PresupuestoService presupuestoService;
    private final UsuarioService usuarioService;

    public PresupuestoController(PresupuestoService presupuestoService,
                                 UsuarioService usuarioService) {
        this.presupuestoService = presupuestoService;
        this.usuarioService = usuarioService;
    }

    // ─────────────────────────────────────────────
    // POST /api/presupuesto
    // ─────────────────────────────────────────────
    @Operation(
            summary = "Crear presupuesto mensual",
            description = "Crea un presupuesto mensual para el usuario autenticado. Solo se permite uno activo a la vez.",
            security = { @SecurityRequirement(name = "bearerAuth") }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Presupuesto creado exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PresupuestoDTO.class),
                            examples = @ExampleObject(value = """
                            {
                              "id": 1,
                              "valor": 2500000,
                              "fecha": "2026-04-21",
                              "fechaVencimiento": "2026-05-21",
                              "idUsuario": 12,
                              "activo": true
                            }
                            """)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "El usuario ya tiene un presupuesto activo",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                            {
                              "error": "El usuario ya tiene un presupuesto activo este mes"
                            }
                            """)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Token inválido o expirado",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                            {
                              "error": "Token inválido o expirado"
                            }
                            """)
                    )
            )
    })
    @PostMapping
    public ResponseEntity<?> crearPresupuesto(
            @Parameter(hidden = true) @RequestHeader("Authorization") String authHeader,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Valor del presupuesto mensual",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CrearPresupuestoRequest.class),
                            examples = @ExampleObject(value = """
                            {
                              "valor": 2500000
                            }
                            """)
                    )
            )
            @Valid @RequestBody CrearPresupuestoRequest request) {
        try {
            String token = authHeader.substring(7);
            String email = usuarioService.getEmailFromToken(token);
            Long idUsuario = usuarioService.getUserByEmail(email).getId();

            PresupuestoDTO presupuesto = presupuestoService.crearPresupuesto(idUsuario, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(presupuesto);

        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> errores = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(fieldError ->
                errores.put(fieldError.getField(), fieldError.getDefaultMessage())
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errores);
    }

    // ─────────────────────────────────────────────
    // GET /api/presupuesto
    // ─────────────────────────────────────────────
    @Operation(
            summary = "Obtener presupuesto activo",
            description = "Retorna el presupuesto activo del usuario autenticado.",
            security = { @SecurityRequirement(name = "bearerAuth") }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Presupuesto activo encontrado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PresupuestoDTO.class),
                            examples = @ExampleObject(value = """
                            {
                              "id": 1,
                              "valor": 2500000,
                              "fecha": "2026-04-21",
                              "fechaVencimiento": "2026-05-21",
                              "idUsuario": 12,
                              "activo": true
                            }
                            """)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "No tiene presupuesto activo",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                            {
                              "error": "El usuario no tiene un presupuesto activo"
                            }
                            """)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Token inválido o expirado",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                            {
                              "error": "Token inválido o expirado"
                            }
                            """)
                    )
            )
    })
    @GetMapping
    public ResponseEntity<?> obtenerPresupuestoActivo(
            @Parameter(hidden = true) @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7);
            String email = usuarioService.getEmailFromToken(token);
            Long idUsuario = usuarioService.getUserByEmail(email).getId();

            PresupuestoDTO presupuesto = presupuestoService.obtenerPresupuestoActivo(idUsuario);
            return ResponseEntity.ok(presupuesto);

        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    @Operation(
            summary = "Obtener todos los presupuestos",
            description = "Retorna el historial completo de presupuestos del usuario autenticado, ordenados del más reciente al más antiguo.",
            security = { @SecurityRequirement(name = "bearerAuth") }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de presupuestos obtenida exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                        [
                          {
                            "id": 2,
                            "valor": 3000000,
                            "fecha": "2026-04-21",
                            "fechaVencimiento": "2026-05-21",
                            "idUsuario": 12,
                            "activo": true
                          },
                          {
                            "id": 1,
                            "valor": 2500000,
                            "fecha": "2026-03-21",
                            "fechaVencimiento": "2026-04-21",
                            "idUsuario": 12,
                            "activo": false
                          }
                        ]
                        """)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "El usuario no tiene presupuestos registrados",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                        {
                          "error": "El usuario no tiene presupuestos registrados"
                        }
                        """)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Token inválido o expirado",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                        {
                          "error": "Token inválido o expirado"
                        }
                        """)
                    )
            )
    })
    @GetMapping("/historial")
    public ResponseEntity<?> obtenerTodosLosPresupuestos(
            @Parameter(hidden = true) @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7);
            String email = usuarioService.getEmailFromToken(token);
            Long idUsuario = usuarioService.getUserByEmail(email).getId();

            List<PresupuestoDTO> presupuestos = presupuestoService.obtenerTodosLosPresupuestos(idUsuario);
            return ResponseEntity.ok(presupuestos);

        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }


    @Operation(
            summary = "Actualizar presupuesto activo",
            description = "Actualiza el valor del presupuesto activo del usuario autenticado.",
            security = { @SecurityRequirement(name = "bearerAuth") }
    )
    @PutMapping
    public ResponseEntity<?> actualizarPresupuesto(
            @Parameter(hidden = true) @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody CrearPresupuestoRequest request
    ) {
        try {
            String token = authHeader.substring(7);
            String email = usuarioService.getEmailFromToken(token);
            Long idUsuario = usuarioService.getUserByEmail(email).getId();

            PresupuestoDTO presupuesto = presupuestoService.actualizarPresupuesto(idUsuario, request);

            return ResponseEntity.ok(presupuesto);

        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }
}