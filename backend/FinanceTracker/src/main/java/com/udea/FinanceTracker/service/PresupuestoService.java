package com.udea.FinanceTracker.service;

import com.udea.FinanceTracker.dto.CrearPresupuestoRequest;
import com.udea.FinanceTracker.dto.PresupuestoDTO;
import com.udea.FinanceTracker.entity.Presupuesto;
import com.udea.FinanceTracker.mapper.PresupuestoMapper;
import com.udea.FinanceTracker.repository.PresupuestoRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.math.BigDecimal;

import java.time.LocalDate;
import java.util.Optional;

@Service
public class PresupuestoService {

    private final PresupuestoRepository presupuestoRepository;
    private final PresupuestoMapper presupuestoMapper;

    public PresupuestoService(PresupuestoRepository presupuestoRepository,
                              PresupuestoMapper presupuestoMapper) {
        this.presupuestoRepository = presupuestoRepository;
        this.presupuestoMapper = presupuestoMapper;
    }

    /**
     * Crea un presupuesto mensual para el usuario.
     * Lanza excepción si ya tiene uno activo.
     *
     * ==================== CORRECCIÓN DE ERRORES 6 y 7 ====================
     * Se agrega validación para rechazar valores 0 o negativos
     * Esto cumple con el criterio de aceptación de HU 2.4
     * ==================== FIN CORRECCIÓN ====================
     */
    public PresupuestoDTO crearPresupuesto(Long idUsuario, CrearPresupuestoRequest request) throws Exception {
        // Validar que el valor sea mayor a cero
        if (request.getValor() == null || request.getValor().compareTo(BigDecimal.ZERO) <= 0) {
            throw new Exception("El valor del presupuesto debe ser mayor a cero");
        }

        LocalDate fechaLimite = LocalDate.now().minusMonths(1);

        Boolean tienePresupuestoActivo = presupuestoRepository
                .existsPresupuestoActivoByUsuario(idUsuario, fechaLimite);

        if (tienePresupuestoActivo) {
            throw new Exception("El usuario ya tiene un presupuesto activo este mes");
        }

        Presupuesto presupuesto = new Presupuesto(
                request.getValor(),
                LocalDate.now(),
                idUsuario
        );

        presupuesto = presupuestoRepository.save(presupuesto);
        return presupuestoMapper.toDTO(presupuesto);
    }

    /**
     * Obtiene el presupuesto activo del usuario.
     * Lanza excepción si no tiene ninguno activo.
     */
    public PresupuestoDTO obtenerPresupuestoActivo(Long idUsuario) throws Exception {
        LocalDate fechaLimite = LocalDate.now().minusMonths(1);

        Optional<Presupuesto> presupuestoOpt = presupuestoRepository
                .findPresupuestoActivoByUsuario(idUsuario, fechaLimite);

        if (presupuestoOpt.isEmpty()) {
            throw new Exception("El usuario no tiene un presupuesto activo");
        }

        return presupuestoMapper.toDTO(presupuestoOpt.get());
    }

    public List<PresupuestoDTO> obtenerTodosLosPresupuestos(Long idUsuario) throws Exception {
        List<Presupuesto> presupuestos = presupuestoRepository
                .findByIdUsuarioOrderByFechaDesc(idUsuario);

        if (presupuestos.isEmpty()) {
            throw new Exception("El usuario no tiene presupuestos registrados");
        }

        return presupuestos.stream()
                .map(presupuestoMapper::toDTO)
                .toList();
    }

}