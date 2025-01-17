package com.aluracursos.literalura.repository;

import com.aluracursos.literalura.model.Libro;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LibroRepository extends JpaRepository<Libro, Long> {
    // Buscar libro por título
    Optional<Libro> findByTitulo(String titulo);

    // Listar libros por idioma
    List<Libro> findByIdiomaContaining(String idioma);

    // Listar todos los libros
    List<Libro> findAll();

    boolean existsByTitulo(String titulo);
}
