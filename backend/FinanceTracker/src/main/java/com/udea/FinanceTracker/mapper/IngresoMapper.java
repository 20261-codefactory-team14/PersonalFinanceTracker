package com.udea.FinanceTracker.mapper;

import com.udea.FinanceTracker.dto.IngresoDTO;
import com.udea.FinanceTracker.entity.Ingreso;
import com.udea.FinanceTracker.entity.Usuario;

public class IngresoMapper {

    public static Ingreso toEntity(IngresoDTO request, Usuario usuario) {
        Ingreso ingreso = new Ingreso();
        ingreso.setValor(request.getValor());
        ingreso.setFecha(request.getFecha());
        ingreso.setDescripcion(request.getDescripcion());
        ingreso.setUsuario(usuario);
        return ingreso;
    }
}