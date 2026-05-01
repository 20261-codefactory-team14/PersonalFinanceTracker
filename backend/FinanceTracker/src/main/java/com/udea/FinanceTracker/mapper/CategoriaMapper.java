package com.udea.FinanceTracker.mapper;

import com.udea.FinanceTracker.dto.CategoriaDTO;
import com.udea.FinanceTracker.entity.Categoria;
import org.springframework.stereotype.Component;

@Component
public class CategoriaMapper {

    public CategoriaDTO toDTO(Categoria categoria) {
        CategoriaDTO dto = new CategoriaDTO();

        dto.setId(categoria.getId());
        dto.setNombre(categoria.getNombre());

        return dto;
    }

    public Categoria toEntity(CategoriaDTO dto) {
        Categoria categoria = new Categoria();

        categoria.setId(dto.getId());
        categoria.setNombre(dto.getNombre());

        return categoria;
    }
}