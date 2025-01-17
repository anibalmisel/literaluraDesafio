package com.aluracursos.literalura.model;

import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "libros")
public class Libro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(length = 500)
    private String titulo;
    @ManyToOne
    @JoinColumn(name = "autor_id") // Nombre de la columna que mapea la relación con la tabla "autor"
    private Autor escritor; // Cambiado a tipo Autor

    private String idioma;

    private Double descargas;

    public Libro() {
    }

    public Libro(DatosLibro libroBuscado, List<Autor> autores) {
        this.titulo = libroBuscado.titulo();
        this.escritor = autores.get(0); // Selecciona el primer autor (puedes ajustar esto)
        this.idioma = libroBuscado.idiomas().get(0);
        try{
            this.descargas = Double.valueOf(libroBuscado.descargas());
        }catch (NumberFormatException e){
            this.descargas = 0.0;
        }
    }
    public Libro(String titulo, List<String> idiomas, Integer descargas) {
        this.titulo = titulo;
        this.idioma = idiomas.get(0);
        // Asegúrate de que descargas sea un valor adecuado
        this.descargas = (descargas != null) ? Double.valueOf(descargas) : 0.0;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public Autor getEscritor() {
        return escritor;
    }

    public void setEscritor(Autor escritor) {
        this.escritor = escritor;
    }

    public String getIdioma() {
        return idioma;
    }

    public void setIdioma(String idioma) {
        this.idioma = idioma;
    }

    public Double getDescargas() {
        return descargas;
    }

    public void setDescargas(Double descargas) {
        this.descargas = descargas;
    }

    @Override
    public String toString() {
        return "Libro: " + '\'' +
                ", titulo = '" + titulo + '\'' +
                ", escritor = '" + (escritor != null ? escritor.getNombre() : "N/A") + '\'' +
                ", idioma = '" + idioma + '\'' +
                ", descargas = " + descargas;
    }
}