package com.aluracursos.literalura.principal;

import com.aluracursos.literalura.model.*;
import com.aluracursos.literalura.repository.AutorRepository;
import com.aluracursos.literalura.repository.LibroRepository;
import com.aluracursos.literalura.service.ConsumoAPI;
import com.aluracursos.literalura.service.ConvierteDatos;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class Principal {
    private final ConsumoAPI consumoAPI = new ConsumoAPI();
    private final ConvierteDatos conversor = new ConvierteDatos();
    private final String URL_BASE = "https://gutendex.com/books/";
    private final String URL_BUSQUEDA = "?search=";
    private final String URL_AUTOR_ANO= "?author_year_end=";
    private final Scanner teclado = new Scanner(System.in);
    private final LibroRepository libroRepository;
    private final AutorRepository autorRepository;
    private List<DatosLibro> datosLibros=new ArrayList<>();

    //https://gutendex.com/books/?search=cervantes

    public Principal(LibroRepository libroRepository, AutorRepository autorRepository) {
        this.libroRepository = libroRepository;
        this.autorRepository = autorRepository;
    }

    public void menu(){
        System.out.println(" ");

        var opcion = -1;
        while (opcion != 0) {
            var menu = """        
            Elija la opcion a traves de su numero:
            
            1- Buscar libro por titulo
            2- Listar libros registrados
            3- Listar autores registrados
            4- Listar autores vivos en un determinado ano
            5- Listar libros por idioma
            6- Buscar Autor
            
            0- Salir
            """;
            System.out.println(" ");
            System.out.println(menu);
            opcion = teclado.nextInt();
            teclado.nextLine();

            switch (opcion) {
                case 1 -> buscarLibroPorTitulo();
                case 2 -> listarLibros();
                case 3 -> listarAutores();
                case 4 -> listarAutoresVivosEnAno();
                case 5 -> listarLibrosPorIdioma();
                case 6 -> buscarAutorDeLibros();
                case 0 -> System.out.println("Cerrando la aplicación...");
                default -> System.out.println("Opción inválida");
            }
        }
    }
//METODOS PRIVADOS
    private Datos getDatosLibro(String tituloLibro) {
        var json = consumoAPI.obtenerDatos(URL_BASE + URL_BUSQUEDA + tituloLibro.replace(" ", "+"));
        //System.out.println(json);//DATO CRUDO
        Datos datos=conversor.obtenerDatos(json, Datos.class);
        return datos;
    }

    //METODOS PUBLICOS
    // Método público para buscar un libro por su título
    public void buscarLibroPorTitulo() {
        System.out.println("_______________________________________________");
        System.out.println("Escriba el título del libro que desea buscar:");
        var tituloLibro = teclado.nextLine();
        System.out.println(" ");

        // Obtén los datos de la API
        Datos datosCrudos = getDatosLibro(tituloLibro);

        // Filtra los resultados usando Streams
        List<DatosLibro> librosEncontrados = datosCrudos.resultados().stream()
                .filter(libro -> libro.titulo().toLowerCase().contains(tituloLibro.toLowerCase()))
                .toList();

        if (!librosEncontrados.isEmpty()) {
            System.out.println("Libros encontrados:");

            // Verifica si todos los libros ya existen en la base de datos
            boolean todosExisten = librosEncontrados.stream()
                    .allMatch(this::libroYaExisteEnBaseDeDatos);

            if (todosExisten) {
                System.out.println("El libro solicitado ya existe en la base de datos.");
            } else {
                // Procesa y guarda únicamente los libros que no están en la base de datos
                librosEncontrados.forEach(libro -> {
                    if (libroYaExisteEnBaseDeDatos(libro)) {
                        // No hacer nada si ya existe
                    } else {
                        System.out.println(libro); // Muestra los datos del libro antes de guardarlo
                        guardarLibroEnBaseDeDatos(libro);
                        guardarAutores(libro); // Guardar autores relacionados, si corresponde
                        System.out.println("El libro '" + libro.titulo() + "' ha sido guardado de manera exitosa en la base de datos.");
                    }
                });
            }
        } else {
            System.out.println("No se encontraron libros con el título proporcionado.");
        }
    }
    // Método para verificar si el libro ya existe en la base de datos
    private boolean libroYaExisteEnBaseDeDatos(DatosLibro libro) {
        // Implementa la lógica para verificar si el libro ya está guardado en la base de datos
        // Por ejemplo, puede ser por título o por algún otro campo único
        return libroRepository.existsByTitulo(libro.titulo());
    }

    // Método para guardar el libro en la base de datos
    private void guardarLibroEnBaseDeDatos(DatosLibro libro) {
        libroRepository.save(new Libro(libro.titulo(), libro.idiomas(), libro.descargas()));
    }

    // Método para guardar los autores relacionados con el libro
    private void guardarAutores(DatosLibro libro) {
        libro.autores().forEach(autor -> {
            if (!autorRepository.existsByNombre(autor.nombre())) {
                autorRepository.save(new Autor(autor.nombre(), autor.fechaDeNacimiento(), autor.fechaDeMuerte()));
            }
        });
    }

    private void guardarLibrosYAutores(List<DatosLibro> librosEncontrados) {
        librosEncontrados.forEach(libro -> {
            // Obtén la lista de autores
            List<Autor> autores = libro.autores().stream()
                    .map(autor -> autorRepository.findByNombre(autor.nombre())
                            .orElseGet(() -> autorRepository.save(new Autor(autor.nombre(), autor.fechaDeNacimiento(), autor.fechaDeMuerte()))))
                    .toList();

            // Si el libro ya existe en la base de datos, actualízalo. De lo contrario, guárdalo.
            Optional<Libro> libroExistente = libroRepository.findByTitulo(libro.titulo());
            if (libroExistente.isEmpty()) {
                // Guarda el nuevo libro con sus autores asociados
                libroRepository.save(new Libro(libro, autores));
            } else {
                // Si el libro ya existe, puedes actualizarlo si lo deseas
                Libro existingLibro = libroExistente.get();
                existingLibro.setEscritor(autores.get(0)); // Se asigna el primer autor (puedes modificar esto si es necesario)
                libroRepository.save(existingLibro); // Opción de actualización
            }
        });
    }
    public void listarLibros() {
        System.out.println("_______________________________________________");
        System.out.println("Libros registrados:");

        List<Libro> libros = libroRepository.findAll(); // Encuentra todos los libros
        if (libros.isEmpty()) {
            System.out.println("No hay libros registrados.");
        } else {
            libros.forEach(libro -> {
                System.out.println("Título: " + libro.getTitulo());
                System.out.println("Idiomas: " + libro.getIdioma());
                System.out.println("Descargas: " + libro.getDescargas());
                System.out.println("-----");
            });
        }
    }
    public void listarAutores() {
        System.out.println("_______________________________________________");
        System.out.println("Autores registrados:");

        List<Autor> autores = autorRepository.findAll(); // Encuentra todos los autores
        if (autores.isEmpty()) {
            System.out.println("No hay autores registrados.");
        } else {
            autores.forEach(autor -> {
                System.out.println("Nombre: " + autor.getNombre());
                System.out.println("Fecha de nacimiento: " + autor.getFechaDeNacimiento());
                System.out.println("Fecha de muerte: " + autor.getFechaDeMuerte());
                System.out.println("-----");
            });
        }
    }
    public void listarAutoresVivosEnAno() {
        System.out.println("_______________________________________________");
        System.out.println("Escriba el año para listar autores vivos en ese año:");
        int ano = teclado.nextInt();
        teclado.nextLine(); // Consumir el salto de línea

        // Llama al repositorio con el año
        List<Autor> autoresVivos = autorRepository.findAutoresVivosEnAno(ano);

        // Procesa los resultados
        if (autoresVivos.isEmpty()) {
            System.out.println("No se encontraron autores vivos en ese año.");
        } else {
            autoresVivos.forEach(autor -> {
                System.out.println("Nombre: " + autor.getNombre());
                System.out.println("Año de nacimiento: " + autor.getFechaDeNacimiento());
                System.out.println("Año de muerte: " + (autor.getFechaDeMuerte() == null ? "Vivo" : autor.getFechaDeMuerte()));
                System.out.println("-----");
            });
        }
    }

    public void listarLibrosPorIdioma() {
        System.out.println("_______________________________________________");
        var opciones = """        
            Escriba las inciales del idioma a buscar:
            
            es-Español
            fr-Francés
            en-English
            pt-Portugués
            """;
        System.out.println(opciones);
        String idioma = teclado.nextLine().trim();

        List<Libro> librosPorIdioma = libroRepository.findAll().stream()
                .filter(libro -> libro.getIdioma().toLowerCase().contains(idioma.toLowerCase()))
                .collect(Collectors.toList());

        if (librosPorIdioma.isEmpty()) {
            System.out.println("No se encontraron libros en el idioma proporcionado.");
        } else {
            librosPorIdioma.forEach(libro -> {
                System.out.println("Título: " + libro.getTitulo());
                System.out.println("Idioma: " + libro.getIdioma());
                System.out.println("Descargas: " + libro.getDescargas());
                System.out.println("-----");
            });
        }
    }

    public void buscarAutorDeLibros() {
        System.out.println("________________________________________________");
        System.out.println("Escriba el nombre del autor que desea buscar:");
        var nombreAutor = teclado.nextLine().trim().toLowerCase();  // Trim y convertir a minúsculas

        // Obtén los datos crudos de la API
        Datos datosCrudos = getDatosLibro(nombreAutor);

        // Filtrar libros cuyos autores contengan una coincidencia exacta o parcial del nombre proporcionado
        List<DatosLibro> librosDeAutor = datosCrudos.resultados().stream()
                .filter(libro -> libro.autores().stream()
                        .anyMatch(autor -> autor.nombre().toLowerCase().contains(nombreAutor))) // Usamos contains pero mejorado
                .toList();

        if (!librosDeAutor.isEmpty()) {
            System.out.println("Autores encontrados para el autor: " + nombreAutor);

            // Usar un Set para evitar autores duplicados
            Set<DatosAutor> autoresUnicos = new HashSet<>();

            // Recorre los libros y agrega los autores al Set
            librosDeAutor.forEach(libro ->
                    libro.autores().forEach(autor -> autoresUnicos.add(autor))
            );

            // Filtrar autores que contengan el nombre exacto (por ejemplo, "cervantes")
            Set<DatosAutor> autoresFiltrados = autoresUnicos.stream()
                    .filter(autor -> autor.nombre().toLowerCase().contains(nombreAutor))  // Aquí comparamos el nombre del autor de manera más precisa
                    .collect(Collectors.toSet());

            // Imprime los autores únicos
            autoresFiltrados.forEach(autor -> {
                System.out.println("Nombre: " + autor.nombre());
                System.out.println("Fecha de nacimiento: " + autor.fechaDeNacimiento());
                System.out.println("Fecha de muerte: " + autor.fechaDeMuerte());
                System.out.println("-----");
            });

            // Procesa los libros y sus autores (mantener la lógica existente)
            guardarLibrosYAutores(librosDeAutor);
        } else {
            System.out.println("No se encontraron libros para el autor proporcionado.");
        }
    }
}