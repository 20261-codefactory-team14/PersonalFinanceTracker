package com.udea.FinanceTracker.service;

import com.udea.FinanceTracker.dto.IngresoDTO;
import com.udea.FinanceTracker.entity.Ingreso;
import com.udea.FinanceTracker.entity.Usuario;
import com.udea.FinanceTracker.mapper.IngresoMapper;
import com.udea.FinanceTracker.repository.IngresoRepository;
import com.udea.FinanceTracker.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class IngresoService {

    private final IngresoRepository ingresoRepository;
    private final UsuarioRepository usuarioRepository;

    public IngresoService(IngresoRepository ingresoRepository,
                          UsuarioRepository usuarioRepository) {
        this.ingresoRepository = ingresoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public Ingreso crearIngreso(IngresoDTO dto) {
        Usuario usuario = usuarioRepository.findById(dto.getIdUsuario())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Ingreso ingreso = IngresoMapper.toEntity(dto, usuario);

        return ingresoRepository.save(ingreso);
    }

    public List<Ingreso> listarIngresos() {
        return ingresoRepository.findAll();
    }

    public Ingreso obtenerIngresoPorId(Long id) {
        return ingresoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ingreso no encontrado"));
    }

    public List<Ingreso> listarIngresosPorUsuario(Long idUsuario) {
        return ingresoRepository.findByUsuario_Id(idUsuario);
    }

    public Ingreso actualizarIngreso(Long id, IngresoDTO dto) {
        Ingreso ingreso = ingresoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ingreso no encontrado"));

        Usuario usuario = usuarioRepository.findById(dto.getIdUsuario())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        ingreso.setValor(dto.getValor());
        ingreso.setFecha(dto.getFecha());
        ingreso.setDescripcion(dto.getDescripcion());
        ingreso.setUsuario(usuario);

        return ingresoRepository.save(ingreso);
    }

    public void eliminarIngreso(Long id) {
        if (!ingresoRepository.existsById(id)) {
            throw new RuntimeException("Ingreso no encontrado");
        }

        ingresoRepository.deleteById(id);
    }
}