# 🚦 Sistema de Simulación y Gestión de Tráfico Urbano en Java

[![Java 17](https://img.shields.io/badge/Java-17%2B-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![Java 17 CI](https://github.com/frankitoromas-tech/simulador-trafico-dijkstra-java/actions/workflows/ci-build.yml/badge.svg)](https://github.com/frankitoromas-tech/simulador-trafico-dijkstra-java/actions/workflows/ci-build.yml)
[![Algorithms: Dijkstra](https://img.shields.io/badge/Algoritmo-Dijkstra%20O((V%2BE)logV)-blue?style=for-the-badge)](https://en.wikipedia.org/wiki/Dijkstra%27s_algorithm)
[![Structures: AVL Trees](https://img.shields.io/badge/Estructura-Árboles%20AVL%20Balanceados-brightgreen?style=for-the-badge)](https://en.wikipedia.org/wiki/AVL_tree)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg?style=for-the-badge)](LICENSE)
[![UTP](https://img.shields.io/badge/UTP-Ingeniería%20de%20Sistemas-red?style=for-the-badge)](https://www.utp.edu.pe/)

Motor algorítmico y simulador de alto rendimiento para el análisis, enrutamiento óptimo y gestión semafórica de redes de transporte urbano multimodal. Desarrollado en **Java 17**, el sistema implementa estructuras de datos avanzadas construidas desde cero sin librerías externas de terceros: **Grafos Dirigidos Ponderados**, **Árboles AVL Auto-balanceables**, **Colas de Prioridad con Min-Heap binario** y **Listas Doblemente Enlazadas Circulares**.

---

## 🏛️ Arquitectura & Estructuras de Datos

```
                                  ┌───────────────────────────────┐
                                  │      RedVial (Grafo Dirigido) │
                                  │    Vértices: Intersecciones   │
                                  │       Aristas: Vías/Calles    │
                                  └──────────────┬────────────────┘
                                                 │
                   ┌─────────────────────────────┼─────────────────────────────┐
                   ▼                             ▼                             ▼
       ┌───────────────────────┐   ┌───────────────────────┐   ┌───────────────────────┐
       │   RutaMasCorta        │   │    ArbolAVL           │   │  ControladorSemaforos │
       │ (Dijkstra + MinHeap)  │   │ Registro Histórico    │   │ Sincronización Tiempo │
       │ Complejidad O((V+E)logV)│ │ Búsqueda O(log N)     │   │ Patrón Observer       │
       └───────────────────────┘   └───────────────────────┘   └───────────────────────┘
```

---

## ⚡ Matriz de Complejidad Algorítmica

| Estructura / Algoritmo | Implementación Concreta | Operación Principal | Complejidad Temporal | Complejidad Espacial |
| :--- | :--- | :--- | :--- | :--- |
| **Algoritmo de Dijkstra** | `RutaMasCorta` + `ColaPrioridadMinHeap` | Enrutamiento de costo mínimo | $\mathcal{O}((V + E) \log V)$ | $\mathcal{O}(V)$ |
| **Árbol AVL Auto-balanceable**| `ArbolAVL` + Rotaciones (LL, RR, LR, RL) | Indexación y búsqueda de congestión | $\mathcal{O}(\log N)$ | $\mathcal{O}(N)$ |
| **Min-Heap Binario** | `ColaPrioridadMinHeap` | Extracción de nodo con menor distancia | $\mathcal{O}(\log V)$ inserción / extracción | $\mathcal{O}(V)$ |
| **Grafo Dirigido** | `GrafoDirigido` (Listas de Adyacencia) | Inserción de vías y consulta de vecinos | $\mathcal{O}(1)$ inserción / $\mathcal{O}(grado(v))$ | $\mathcal{O}(V + E)$ |
| **Control Semafórico** | `ControladorSemaforos` (Lista Circular) | Ciclos de cambio verde/amarillo/rojo | $\mathcal{O}(1)$ transición de fase | $\mathcal{O}(S)$ |

---

## 📂 Organización Modular del Código

```
simulador-trafico-dijkstra-java/
├── .github/workflows/
│   └── ci-build.yml                  # Pipeline automatizado de compilación Java 17
├── src/com/trafico/
│   ├── estructuras/                  # Estructuras de datos fundamentales (zero-dependency)
│   │   ├── ArbolAVL.java             # Árbol binario auto-balanceado por factor de equilibrio
│   │   ├── ColaPrioridadMinHeap.java # Min-Heap binario para optimización de Dijkstra
│   │   ├── GrafoDirigido.java        # Representación de topología vial por listas de adyacencia
│   │   ├── ListaDoblementeEnlazada.java
│   │   └── NodoAVL.java
│   ├── modelo/                       # Entidades del dominio vial
│   │   ├── Interseccion.java         # Nodos del grafo (coordenadas, semáforos)
│   │   ├── Via.java                  # Aristas ponderadas (longitud, velocidad, congestión)
│   │   ├── RedVial.java              # Topología urbana completa
│   │   └── vehiculos/                # Jerarquía polimórfica (Autos, Buses, Emergencia)
│   ├── navegacion/                   # Estrategias de enrutamiento (Patrón Strategy)
│   │   ├── RutaMasCorta.java         # Implementación formal del algoritmo de Dijkstra
│   │   ├── RutaMenosSemaforos.java   # Ponderación heurística orientada a fluidez
│   │   └── RutaOptimizada.java       # Factor ponderado por congestión en tiempo real
│   ├── semaforos/                    # Gestión reactiva de tránsito
│   │   ├── Semaforo.java             # Máquina de estados (VERDE, AMARILLO, ROJO)
│   │   └── ControladorSemaforos.java # Coordinación sincrónica de corredores viales
│   ├── simulacion/                   # Motor temporal y generador de métricas
│   │   ├── MotorSimulacion.java      # Loop discreto de actualización por tick
│   │   ├── ControladorFlota.java     # Despacho y rastreo dinámico de vehículos
│   │   └── InformeTrafico.java       # Exportación y telemetría de tiempos de viaje
│   └── principal/
│       └── Principal.java            # Punto de entrada y escenarios de prueba interactivos
├── LICENSE                           # Licencia MIT
└── SECURITY.md                       # Política de seguridad y reporte responsable
```

---

## 🚀 Compilación y Ejecución

### Requisitos
- **JDK 17** o superior.
- Git y terminal Bash / PowerShell.

### 1. Clonar el Repositorio
```bash
git clone https://github.com/frankitoromas-tech/simulador-trafico-dijkstra-java.git
cd simulador-trafico-dijkstra-java
```

### 2. Compilar Todas las Clases
```bash
# En Windows (PowerShell) o Linux/macOS
mkdir -p bin
javac -d bin -sourcepath src src/com/trafico/principal/Principal.java
```

### 3. Ejecutar la Simulación
```bash
java -cp bin com.trafico.principal.Principal
```

---

## 🧪 Escenarios de Prueba Validados

1. **Ruta Crítica de Emergencia:** Despacho prioritario de ambulancias donde los semáforos conmutan a fase verde inmediata mediante el patrón Observer.
2. **Congestión Dinámica en Horas Punta:** Demostración de recalculo automático de ruta cuando el factor de congestión $lpha$ incrementa el peso efectivo de la vía: $W_{efectivo} = rac{Distancia}{Velocidad 	imes (1 - Congestion)}$.
3. **Balanceo de Árbol AVL:** Inserción secuencial de 1,000 registros de telemetría manteniendo una altura balanceada $h \le 1.44 \log_2(N)$ con cero degradación de rendimiento.

---

## 👨‍💻 Autor & Créditos
- **Ingeniero & Desarrollador:** **Φραγκοσύνη / francus 🐦‍🔥** (Frank Emiliano Vargas Huamán)
- **Especialidad:** Backend Java Enterprise, Algoritmos & Estructuras de Datos Complejas
- **GitHub:** [@frankitoromas-tech](https://github.com/frankitoromas-tech)
- **LinkedIn:** [Frank Emiliano Vargas](https://www.linkedin.com/in/frank-emiliano-vargas-huam%C3%A1n-6a010a378/)
