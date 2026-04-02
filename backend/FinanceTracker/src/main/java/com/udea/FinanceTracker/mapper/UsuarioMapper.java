package com.udea.FinanceTracker.mapper;

import com.udea.FinanceTracker.dto.UsuarioDTO;
import com.udea.FinanceTracker.entity.Usuario;
import org.springframework.stereotype.Component;

@Component
public class UsuarioMapper {

    public UsuarioDTO toDTO(Usuario usuario) {
        if (usuario == null) {
            return null;
        }

        return UsuarioDTO.builder()
                .id(usuario.getId())
                .nombre(usuario.getNombre())
                .email(usuario.getEmail())
                .googleId(usuario.getGoogleId())
                .idGenero(usuario.getIdGenero())
                .fechaNacimiento(usuario.getFechaNacimiento() != null ?
                        usuario.getFechaNacimiento().toString() : null)
                .salario(usuario.getSalario())
                .idOcupacion(usuario.getIdOcupacion())
                .profileCompleted(usuario.getProfileCompleted())
                .createdAt(usuario.getCreatedAt())
                .build();
    }

    public Usuario toEntity(UsuarioDTO dto) {
        if (dto == null) {
            return null;
        }

        Usuario usuario = new Usuario();
        usuario.setId(dto.getId());
        usuario.setNombre(dto.getNombre());
        usuario.setEmail(dto.getEmail());
        usuario.setGoogleId(dto.getGoogleId());
        usuario.setIdGenero(dto.getIdGenero());
        usuario.setSalario(dto.getSalario());
        usuario.setIdOcupacion(dto.getIdOcupacion());
        usuario.setProfileCompleted(dto.getProfileCompleted());

        return usuario;
    }
}

