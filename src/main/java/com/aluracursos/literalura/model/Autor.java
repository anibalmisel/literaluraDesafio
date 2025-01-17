package com.aluracursos.literalura.model;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
@Entity
@Table(name = "autor")
public class Autor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nombre;
    private String fechaDeNacimiento;
    private String fechaDeMuerte;
    @OneToMany(mappedBy = "escritor", cascade = CascadeType.ALL)
    private List<Libro> libros;

    public Autor() {
    }

    public Autor(String nombre) {
    }

    public Autor(Long id, String nombre, String fechaDeNacimiento, String fechaDeMuerte, List<Libro> libros) {
        this.id = id;
        this.nombre = nombre;
        this.fechaDeNacimiento = fechaDeNacimiento;
        this.fechaDeMuerte = fechaDeMuerte;
        this.libros = libros;
    }

    public Autor(String nombre, String fechaDeNacimiento, String fechaDeMuerte) {
        this.nombre = nombre;
        this.fechaDeNacimiento = fechaDeNacimiento;
        this.fechaDeMuerte = fechaDeMuerte;
        this.libros = new ArrayList<>(); // Se puede inicializar con una lista vacía si es necesario
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getFechaDeNacimiento() {
        return fechaDeNacimiento;
    }

    public void setFechaDeNacimiento(String fechaDeNacimiento) {
        this.fechaDeNacimiento = fechaDeNacimiento;
    }

    public String getFechaDeMuerte() {
        return fechaDeMuerte;
    }

    public void setFechaDeMuerte(String fechaDeMuerte) {
        this.fechaDeMuerte = fechaDeMuerte;
    }

    public List<Libro> getLibros() {
        return libros;
    }

    public void setLibros(List<Libro> libros) {
        this.libros = libros;
    }

    @Override
    public String toString() {
        return "Autor: "+ '\'' +
                ", nombre=' " + nombre + '\'' +
                ", fechaDeNacimiento=' " + fechaDeNacimiento + '\'' +
                ", fechaDeMuerte=' " + fechaDeMuerte + '\'' +
                ", libros= " + libros;
    }
}