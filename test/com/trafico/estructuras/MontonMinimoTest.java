package com.trafico.estructuras;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Pruebas Unitarias - Montón Mínimo (Min-Heap / Cola de Prioridad)")
class MontonMinimoTest {

    private MontonMinimo<Integer> heap;

    @BeforeEach
    void setUp() {
        heap = new MontonMinimo<>();
    }

    @Test
    @DisplayName("Debe iniciar vacío con tamaño 0")
    void testInicializacionVacia() {
        assertThat(heap.estaVacio()).isTrue();
        assertThat(heap.getTamanio()).isEqualTo(0);
    }

    @Test
    @DisplayName("Debe extraer elementos siempre en orden ascendente (Propiedad Min-Heap)")
    void testOrdenDeExtraccion() {
        heap.insertar(45);
        heap.insertar(12);
        heap.insertar(89);
        heap.insertar(3);
        heap.insertar(27);

        assertThat(heap.getTamanio()).isEqualTo(5);
        assertThat(heap.verMinimo()).isEqualTo(3);

        assertThat(heap.extraerMinimo()).isEqualTo(3);
        assertThat(heap.extraerMinimo()).isEqualTo(12);
        assertThat(heap.extraerMinimo()).isEqualTo(27);
        assertThat(heap.extraerMinimo()).isEqualTo(45);
        assertThat(heap.extraerMinimo()).isEqualTo(89);

        assertThat(heap.estaVacio()).isTrue();
    }

    @Test
    @DisplayName("Debe redimensionar dinámicamente cuando supera la capacidad inicial (16 elementos)")
    void testRedimensionamientoDinamico() {
        for (int i = 50; i >= 1; i--) {
            heap.insertar(i);
        }

        assertThat(heap.getTamanio()).isEqualTo(50);
        assertThat(heap.verMinimo()).isEqualTo(1);

        for (int i = 1; i <= 50; i++) {
            assertThat(heap.extraerMinimo()).isEqualTo(i);
        }

        assertThat(heap.estaVacio()).isTrue();
    }

    @Test
    @DisplayName("Debe lanzar NoSuchElementException al extraer o ver mínimo en heap vacío")
    void testExcepcionesEnVacio() {
        assertThatThrownBy(() -> heap.extraerMinimo())
                .isInstanceOf(NoSuchElementException.class);

        assertThatThrownBy(() -> heap.verMinimo())
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    @DisplayName("Debe lanzar IllegalArgumentException al intentar insertar null")
    void testInsercionNula() {
        assertThatThrownBy(() -> heap.insertar(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Debe vaciar todos los elementos correctamente")
    void testVaciar() {
        heap.insertar(10);
        heap.insertar(20);
        heap.vaciar();

        assertThat(heap.estaVacio()).isTrue();
        assertThat(heap.getTamanio()).isEqualTo(0);
    }
}
