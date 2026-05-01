package com.udea.FinanceTracker.service;

/** TODO Agregar categoria */
import com.udea.FinanceTracker.dto.GastoDTO;
//import com.udea.FinanceTracker.entity.Categoria;
import com.udea.FinanceTracker.entity.Gasto;
import com.udea.FinanceTracker.entity.Usuario;
import com.udea.FinanceTracker.mapper.GastoMapper;
//import com.udea.FinanceTracker.repository.CategoriaRepository;
import com.udea.FinanceTracker.repository.GastoRepository;
import com.udea.FinanceTracker.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GastoService {

    private final GastoRepository gastoRepository;
    private final UsuarioRepository usuarioRepository;
   // private final CategoriaRepository categoriaRepository;

    public GastoService(GastoRepository gastoRepository,
                        UsuarioRepository usuarioRepository) {
                        // CategoriaRepository categoriaRepository) {

        this.gastoRepository = gastoRepository;
        this.usuarioRepository = usuarioRepository;
        //this.categoriaRepository = categoriaRepository;
    }

    public Gasto crearGasto(GastoDTO dto) {
        Usuario usuario = usuarioRepository.findById(dto.getIdUsuario())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        /**
        Categoria categoria = categoriaRepository.findById(dto.getIdCategoria())
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));
**/
        //Gasto gasto = GastoMapper.toEntity(dto, usuario, categoria);
        Gasto gasto = GastoMapper.toEntity(dto, usuario);

        return gastoRepository.save(gasto);
    }

    public List<Gasto> listarGastos() {
        return gastoRepository.findAll();
    }

    public Gasto obtenerGastoPorId(Long id) {
        return gastoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Gasto no encontrado"));
    }

    public List<Gasto> listarGastosPorUsuario(Long idUsuario) {
        return gastoRepository.findByUsuarioId(idUsuario);
    }
/**
    public List<Gasto> listarGastosPorCategoria(Long idCategoria) {
        return gastoRepository.findByCategoriaId(idCategoria);
    }
**/
    public Gasto actualizarGasto(Long id, GastoDTO dto) {
        Gasto gasto = gastoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Gasto no encontrado"));

        Usuario usuario = usuarioRepository.findById(dto.getIdUsuario())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
/**
        Categoria categoria = categoriaRepository.findById(dto.getIdCategoria())
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));
**/
        gasto.setValor(dto.getValor());
        gasto.setFecha(dto.getFecha());
        gasto.setDescripcion(dto.getDescripcion());
        gasto.setUsuario(usuario);
       // gasto.setCategoria(categoria);

        return gastoRepository.save(gasto);
    }

    public void eliminarGasto(Long id) {
        if (!gastoRepository.existsById(id)) {
            throw new RuntimeException("Gasto no encontrado");
        }

        gastoRepository.deleteById(id);
    }
}