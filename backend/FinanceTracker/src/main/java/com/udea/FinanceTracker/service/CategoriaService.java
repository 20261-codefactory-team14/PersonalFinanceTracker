package com.udea.FinanceTracker.service;

import com.udea.FinanceTracker.dto.ActualizarCategoriaRequest;
import com.udea.FinanceTracker.dto.CategoriaDTO;
import com.udea.FinanceTracker.dto.CrearCategoriaRequest;
import com.udea.FinanceTracker.entity.Categoria;
import com.udea.FinanceTracker.mapper.CategoriaMapper;
import com.udea.FinanceTracker.repository.CategoriaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final CategoriaMapper categoriaMapper;

    public CategoriaService(CategoriaRepository categoriaRepository,
                            CategoriaMapper categoriaMapper) {
        this.categoriaRepository = categoriaRepository;
        this.categoriaMapper = categoriaMapper;
    }

    public CategoriaDTO crearCategoria(CrearCategoriaRequest request) throws Exception {
        String nombreNormalizado = request.getNombre().trim();

        Boolean existeCategoria = categoriaRepository.existsByNombreIgnoreCase(nombreNormalizado);

        if (existeCategoria) {
            throw new Exception("Ya existe una categoría con ese nombre");
        }

        Categoria categoria = new Categoria(nombreNormalizado);

        categoria = categoriaRepository.save(categoria);
        return categoriaMapper.toDTO(categoria);
    }

    public CategoriaDTO actualizarCategoria(Long categoryId, ActualizarCategoriaRequest request) throws Exception {
        Categoria categoria = categoriaRepository.findById(categoryId)
                .orElseThrow(() -> new Exception("Categoría no encontrada"));

        String nombreNormalizado = request.getNombre().trim();

        Boolean existeOtraCategoriaConNombre = categoriaRepository
                .existsByNombreIgnoreCaseAndIdNot(nombreNormalizado, categoryId);

        if (existeOtraCategoriaConNombre) {
            throw new Exception("Ya existe otra categoría con ese nombre");
        }

        categoria.setNombre(nombreNormalizado);

        categoria = categoriaRepository.save(categoria);
        return categoriaMapper.toDTO(categoria);
    }

    public CategoriaDTO obtenerCategoriaPorId(Long categoryId) throws Exception {
        Categoria categoria = categoriaRepository.findById(categoryId)
                .orElseThrow(() -> new Exception("Categoría no encontrada"));

        return categoriaMapper.toDTO(categoria);
    }

    public List<CategoriaDTO> obtenerCategorias() {
        return categoriaRepository.findAllByOrderByNombreAsc()
                .stream()
                .map(categoriaMapper::toDTO)
                .toList();
    }

    public void eliminarCategoria(Long categoryId) throws Exception {
        Categoria categoria = categoriaRepository.findById(categoryId)
                .orElseThrow(() -> new Exception("Categoría no encontrada"));

        categoriaRepository.delete(categoria);
    }
}