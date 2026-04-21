package com.udea.FinanceTracker.mapper;

import com.udea.FinanceTracker.dto.PresupuestoDTO;
import com.udea.FinanceTracker.entity.Presupuesto;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class PresupuestoMapper {

    public PresupuestoDTO toDTO(Presupuesto presupuesto) {
        PresupuestoDTO dto = new PresupuestoDTO();

        dto.setId(presupuesto.getId());
        dto.setValor(presupuesto.getValor());
        dto.setFecha(presupuesto.getFecha());
        dto.setIdUsuario(presupuesto.getIdUsuario());

        // Calcula fecha de vencimiento (1 mes después de fecha)
        LocalDate fechaVencimiento = presupuesto.getFecha().plusMonths(1);
        dto.setFechaVencimiento(fechaVencimiento);

        // Calcula si sigue activo
        Boolean activo = LocalDate.now().isBefore(fechaVencimiento);
        dto.setActivo(activo);

        return dto;
    }

    public Presupuesto toEntity(PresupuestoDTO dto) {
        Presupuesto presupuesto = new Presupuesto();

        presupuesto.setId(dto.getId());
        presupuesto.setValor(dto.getValor());
        presupuesto.setFecha(dto.getFecha());
        presupuesto.setIdUsuario(dto.getIdUsuario());

        return presupuesto;
    }
}