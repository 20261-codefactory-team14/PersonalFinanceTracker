package com.udea.FinanceTracker.mapper;

import com.udea.FinanceTracker.dto.GastoDTO;
import com.udea.FinanceTracker.dto.GastoResponseDTO;
import com.udea.FinanceTracker.entity.Categoria;
import com.udea.FinanceTracker.entity.Gasto;
import com.udea.FinanceTracker.entity.Usuario;

public class GastoMapper {

    // 🔹 DTO → Entity
    public static Gasto toEntity(GastoDTO request, Usuario usuario, Categoria categoria) {
        Gasto gasto = new Gasto();
        gasto.setValor(request.getValor());
        gasto.setFecha(request.getFecha());
        gasto.setDescripcion(request.getDescripcion());
        gasto.setUsuario(usuario);
        gasto.setCategoria(categoria);
        return gasto;
    }

    // 🔹 Entity → ResponseDTO
    public static GastoResponseDTO toResponseDTO(Gasto gasto) {
        return new GastoResponseDTO(
                gasto.getId(),
                gasto.getValor(),
                gasto.getFecha(),
                gasto.getDescripcion(),
                gasto.getUsuario() != null ? gasto.getUsuario().getId() : null,
                gasto.getCategoria() != null ? gasto.getCategoria().getId() : null
        );
    }
}