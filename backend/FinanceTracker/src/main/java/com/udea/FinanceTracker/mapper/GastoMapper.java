package com.udea.FinanceTracker.mapper;

import com.udea.FinanceTracker.dto.GastoDTO;
/** TODO AGREAGAR CATEGORIA
 * import com.udea.FinanceTracker.entity.Categoria;
 * */

import com.udea.FinanceTracker.entity.Gasto;
import com.udea.FinanceTracker.entity.Usuario;

public class GastoMapper {

    // public static Gasto toEntity(GastoDTO request, Usuario usuario, Categoria categoria) {
    public static Gasto toEntity(GastoDTO request, Usuario usuario) {
        Gasto gasto = new Gasto();
        gasto.setValor(request.getValor());
        gasto.setFecha(request.getFecha());
        gasto.setDescripcion(request.getDescripcion());
        gasto.setUsuario(usuario);
       // gasto.setCategoria(categoria);
        return gasto;
    }
}