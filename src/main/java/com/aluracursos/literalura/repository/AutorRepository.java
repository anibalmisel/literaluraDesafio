package com.aluracursos.literalura.repository;

import com.aluracursos.literalura.model.Autor;
import com.aluracursos.literalura.model.Libro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AutorRepository extends JpaRepository<Autor, Long> {
    // Listar autores vivos en un determinado año
    List<Autor> findByFechaDeNacimientoLessThanEqualAndFechaDeMuerteGreaterThanEqual(String fechaDeNacimiento, String fechaDeMuerte);

    Optional<Autor>findByNombre(String nombre);

    boolean existsByNombre(String nombre);
    @Query("SELECT a FROM Autor a WHERE " +
            "CAST(a.fechaDeNacimiento AS integer) <= :ano AND " +
            "(a.fechaDeMuerte IS NULL OR CAST(a.fechaDeMuerte AS integer) >= :ano)")
    List<Autor> findAutoresVivosEnAno(@Param("ano") Integer ano);

}