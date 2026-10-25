package com.trafico.estructuras;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Pruebas Unitarias - Árbol AVL Auto-Balanceado")
class ArbolAVLTest {

    private ArbolAVL<Integer, String> arbol;

    @BeforeEach
    void setUp() {
        arbol = new ArbolAVL<>();
    }

    @Test
    @DisplayName("Debe iniciar vacío con tamaño cero")
    void testInicializacionVacia() {
        assertThat(arbol.estaVacio()).isTrue();
        assertThat(arbol.getTamanio()).isEqualTo(0);
        assertThat(arbol.buscar(10)).isNull();
    }

    @Test
    @DisplayName("Debe insertar elementos y permitir su búsqueda exitosa")
    void testInsercionYBusqueda() {
        arbol.insertar(50, "Nodo-50");
        arbol.insertar(30, "Nodo-30");
        arbol.insertar(70, "Nodo-70");

        assertThat(arbol.estaVacio()).isFalse();
        assertThat(arbol.getTamanio()).isEqualTo(3);
        assertThat(arbol.buscar(50)).isEqualTo("Nodo-50");
        assertThat(arbol.buscar(30)).isEqualTo("Nodo-30");
        assertThat(arbol.buscar(70)).isEqualTo("Nodo-70");
    }

    @Test
    @DisplayName("Debe actualizar el valor si la clave ya existe sin alterar el tamaño")
    void testActualizacionClaveDuplicada() {
        arbol.insertar(10, "Version-1");
        arbol.insertar(10, "Version-2");

        assertThat(arbol.getTamanio()).isEqualTo(1);
        assertThat(arbol.buscar(10)).isEqualTo("Version-2");
    }

    @Test
    @DisplayName("Debe auto-balancear ante inserciones desbalanceadas (Caso Izquierda-Izquierda / Rotación Derecha)")
    void testAutoBalanceoIzquierdaIzquierda() {
        // Inserción secuencial decreciente: 30 -> 20 -> 10
        arbol.insertar(30, "30");
        arbol.insertar(20, "20");
        arbol.insertar(10, "10");

        assertThat(arbol.getTamanio()).isEqualTo(3);
        assertThat(arbol.getClaveRaiz()).isEqualTo(20);
        assertThat(arbol.getAltura()).isEqualTo(2);
        assertThat(arbol.estaBalanceado()).isTrue();
        assertThat(arbol.buscar(10)).isEqualTo("10");
        assertThat(arbol.buscar(20)).isEqualTo("20");
        assertThat(arbol.buscar(30)).isEqualTo("30");
    }

    @Test
    @DisplayName("Debe auto-balancear ante inserciones desbalanceadas (Caso Derecha-Derecha / Rotación Izquierda)")
    void testAutoBalanceoDerechaDerecha() {
        // Insercion secuencial creciente: 10 -> 20 -> 30
        arbol.insertar(10, "10");
        arbol.insertar(20, "20");
        arbol.insertar(30, "30");

        assertThat(arbol.getTamanio()).isEqualTo(3);
        assertThat(arbol.getClaveRaiz()).isEqualTo(20);
        assertThat(arbol.getAltura()).isEqualTo(2);
        assertThat(arbol.estaBalanceado()).isTrue();
        assertThat(arbol.buscar(10)).isEqualTo("10");
        assertThat(arbol.buscar(20)).isEqualTo("20");
        assertThat(arbol.buscar(30)).isEqualTo("30");
    }

    @Test
    @DisplayName("Debe auto-balancear ante inserciones en zigzag (Caso Izquierda-Derecha / Doble Rotacion LR)")
    void testAutoBalanceoIzquierdaDerecha() {
        // Insercion en zigzag izquierdo: 30 -> 10 -> 20
        arbol.insertar(30, "30");
        arbol.insertar(10, "10");
        arbol.insertar(20, "20");

        assertThat(arbol.getTamanio()).isEqualTo(3);
        assertThat(arbol.getClaveRaiz()).isEqualTo(20);
        assertThat(arbol.getAltura()).isEqualTo(2);
        assertThat(arbol.estaBalanceado()).isTrue();
        assertThat(arbol.buscar(10)).isEqualTo("10");
        assertThat(arbol.buscar(20)).isEqualTo("20");
        assertThat(arbol.buscar(30)).isEqualTo("30");
    }

    @Test
    @DisplayName("Debe auto-balancear ante inserciones en zigzag (Caso Derecha-Izquierda / Doble Rotacion RL)")
    void testAutoBalanceoDerechaIzquierda() {
        // Insercion en zigzag derecho: 10 -> 30 -> 20
        arbol.insertar(10, "10");
        arbol.insertar(30, "30");
        arbol.insertar(20, "20");

        assertThat(arbol.getTamanio()).isEqualTo(3);
        assertThat(arbol.getClaveRaiz()).isEqualTo(20);
        assertThat(arbol.getAltura()).isEqualTo(2);
        assertThat(arbol.estaBalanceado()).isTrue();
        assertThat(arbol.buscar(10)).isEqualTo("10");
        assertThat(arbol.buscar(20)).isEqualTo("20");
        assertThat(arbol.buscar(30)).isEqualTo("30");
    }

    @Test
    @DisplayName("Debe mantener el invariante de balance AVL ante inserciones secuenciales masivas")
    void testInvarianteBalanceMasivo() {
        for (int i = 1; i <= 100; i++) {
            arbol.insertar(i, "Valor-" + i);
        }

        assertThat(arbol.getTamanio()).isEqualTo(100);
        assertThat(arbol.estaBalanceado()).isTrue();
        // Cota superior teorica de altura AVL: h <= 1.4404 * log2(100 + 2) ~ 9.6 -> h <= 10
        assertThat(arbol.getAltura()).isLessThanOrEqualTo(10);

        for (int i = 1; i <= 100; i++) {
            assertThat(arbol.buscar(i)).isEqualTo("Valor-" + i);
        }
    }


    @Test
    @DisplayName("Debe eliminar nodos correctamente y reajustar tamaño")
    void testEliminacion() {
        arbol.insertar(100, "Cien");
        arbol.insertar(50, "Cincuenta");
        arbol.insertar(150, "Ciento Cincuenta");

        boolean eliminado = arbol.eliminar(50);
        assertThat(eliminado).isTrue();
        assertThat(arbol.getTamanio()).isEqualTo(2);
        assertThat(arbol.buscar(50)).isNull();
        assertThat(arbol.buscar(100)).isEqualTo("Cien");
    }

    @Test
    @DisplayName("Debe retornar false al intentar eliminar una clave inexistente")
    void testEliminacionInexistente() {
        arbol.insertar(1, "Uno");
        boolean eliminado = arbol.eliminar(999);
        assertThat(eliminado).isFalse();
        assertThat(arbol.getTamanio()).isEqualTo(1);
    }

    @Test
    @DisplayName("Debe lanzar IllegalArgumentException si la clave es nula")
    void testClaveNula() {
        assertThatThrownBy(() -> arbol.insertar(null, "Invalido"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
