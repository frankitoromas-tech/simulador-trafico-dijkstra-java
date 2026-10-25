# 📐 Análisis Formal de Invariantes Algorítmicos y Complejidad Computacional

> **Repositorio:** `frankitoromas-tech/simulador-trafico-dijkstra-java`  
> **Sistema:** Simulador de Tráfico Urbano y Ruteo Óptimo Multimodal  
> **Autor & Auditor:** Frankosyni (`Φρανκοσύνη/Francus.`) — Frank Emiliano Vargas Huamán  
> **Fecha de Auditoría:** Octubre 2026  
> **Estado de la Suite:** 19/19 Pruebas Unitarias Pasando (100% Éxito)

---

## 📑 Tabla de Contenidos
1. [Introducción & Alcance de la Auditoría](#1-introducción--alcance-de-la-auditoría)
2. [Algoritmo de Dijkstra (`RutaMasCorta.java`)](#2-algoritmo-de-dijkstra-rutamascortajava)
   - [Modelado Matemático del Grafo y Funciones de Costo](#modelado-matemático-del-grafo-y-funciones-de-costo)
   - [Invariantes Algorítmicos y Demostración de Correctitud](#invariantes-algorítmicos-y-demostración-de-correctitud)
   - [Complejidad Asintótica Temporal y Espacial](#complejidad-asintótica-temporal-y-espacial)
   - [Reconstrucción LIFO de Trayectorias](#reconstrucción-lifo-de-trayectorias)
3. [Árbol AVL Auto-Balanceado (`ArbolAVL.java`)](#3-árbol-avl-auto-balanceado-arbolavljava)
   - [Propiedades Topológicas y Cota de Altura de Fibonacci](#propiedades-topológicas-y-cota-de-altura-de-fibonacci)
   - [Álgebra y Taxonomía de las 4 Rotaciones (LL, RR, LR, RL)](#álgebra-y-taxonomía-de-las-4-rotaciones-ll-rr-lr-rl)
   - [Invariantes en Inserción y Eliminación](#invariantes-en-inserción-y-eliminación)
   - [Complejidad Asintótica](#complejidad-asintótica)
4. [Montón Mínimo Binario (`MontonMinimo.java`)](#4-montón-mínimo-binario-montonminimoojava)
   - [Estructura en Arreglo Plano e Indexación](#estructura-en-arreglo-plano-e-indexación)
   - [Invariantes Estructural y de Orden Parcial](#invariantes-estructural-y-de-orden-parcial)
   - [Dinámica de Percolación (Sift-Up / Sift-Down)](#dinámica-de-percolación-sift-up--sift-down)
   - [Análisis Amortizado de Capacidad](#análisis-amortizado-de-capacidad)
5. [Auditoría y Estado de la Suite de Pruebas Unitarias](#5-auditoría-y-estado-de-la-suite-de-pruebas-unitarias)
   - [Diagnóstico de Cobertura Inicial](#diagnóstico-de-cobertura-inicial)
   - [Ampliación de Casos de Prueba](#ampliación-de-casos-de-prueba)
   - [Matriz de Verificación JUnit 5](#matriz-de-verificación-junit-5)

---

## 1. Introducción & Alcance de la Auditoría

El presente documento formaliza el rigor matemático, los invariantes algorítmicos de inducción y el análisis asintótico de las tres estructuras centrales desarrolladas desde cero en el proyecto `simulador-trafico-dijkstra-java`:
1. El motor de búsqueda de camino de menor costo basado en el **Algoritmo de Dijkstra** guiado por cola de prioridad.
2. El **Árbol AVL Genérico** con factor de balance estricto y las 4 rotaciones canónicas (LL, RR, LR, RL).
3. El **Montón Mínimo Binario (Min-Heap)** implementado sobre búfer continuo de memoria con amortización geométrica.

---

## 2. Algoritmo de Dijkstra (`RutaMasCorta.java`)

### Modelado Matemático del Grafo y Funciones de Costo

La red vial se modela como un grafo dirigido ponderado $G = (V, E, w)$, donde:
- $V$: conjunto finito de vértices que representan intersecciones viales (`Interseccion`).
- $E \subseteq V \times V$: conjunto de aristas dirigidas que representan tramos viales (`Calle`).
- $w: E \to \mathbb{R}^+$: función de peso dinámico calculada en función de la longitud física de la vía, la velocidad reglamentaria y el factor de congestión en tiempo real $\alpha \in [0, 0.95]$:

$$w(u, v) = \frac{\text{distancia}(u, v)}{v_{\text{reglamentaria}} \times (1 - \alpha)}$$

Dado que $\alpha < 1.0$ y $\text{distancia} > 0$, se garantiza contractualmente que:

$$\forall (u, v) \in E, \quad w(u, v) > 0$$

### Invariantes Algorítmicos y Demostración de Correctitud

El algoritmo mantiene cuatro invariantes fundamentales durante su ciclo de ejecución:

#### Invariante 1: No Negatividad de Aristas (Edge Non-Negativity)
$$\forall e \in E, \quad w(e) \ge 0$$
Esta precondición garantiza que la función de costo acumulado sea monótona no decreciente a lo largo de cualquier camino simple, imposibilitando la existencia de ciclos negativos y asegurando que una vez asentado un vértice, ningún camino alternativo posterior pueda ofrecer un costo menor.

#### Invariante 2: Optimalidad de Nodos Asentados (Settled Vertex Optimality)
Sea $S \subseteq V$ el conjunto de nodos visitados y cerrados (representado por `Set<String> visitados`).
$$\forall u \in S, \quad d[u] = \delta(\text{origen}, u)$$
*Demostración por inducción sobre $|S|$:*
- **Caso base:** Al inicio, $S = \emptyset$. Tras la primera extracción, se extrae el nodo $\text{origen}$ con costo $d[\text{origen}] = 0$. Puesto que todas las aristas tienen peso no negativo, $\delta(\text{origen}, \text{origen}) = 0 = d[\text{origen}]$, cumpliéndose para $|S| = 1$.
- **Paso inductivo:** Supóngase que la propiedad se cumple para $|S| = k$. Sea $u$ el siguiente nodo extraído de la cola de prioridad `MontonMinimo`. Si existiese un camino más corto $P$ de $\text{origen}$ a $u$, dicho camino debe abandonar $S$ en algún punto a través de una arista $(x, y)$ donde $x \in S$ y $y \notin S$. Dado que $x \in S$, $d[x] = \delta(\text{origen}, x)$, y tras relajar $(x, y)$, $d[y] \le \delta(\text{origen}, x) + w(x, y) = \text{costo}(P_{xy}) \le \text{costo}(P) < d[u]$. Pero como la cola de prioridad siempre extrae el elemento con mínimo $d$, debe cumplirse $d[u] \le d[y]$, lo cual contradice $d[y] < d[u]$. Por reducción al absurdo, $d[u] = \delta(\text{origen}, u)$.

#### Invariante 3: Cota Superior de Relajación (Upper Bound Property)
$$\forall v \in V, \quad d[v] \ge \delta(\text{origen}, v)$$
En cualquier instante, $d[v]$ es la longitud de algún camino real de $\text{origen}$ a $v$, o $\infty$ si aún no se ha descubierto camino. La operación de relajación:
$$d[v] \gets \min(d[v], d[u] + w(u, v))$$
es monótonamente decreciente y converge exactamente al valor óptimo $\delta(\text{origen}, v)$.

#### Invariante 4: Subgrafo de Predecesores Acíclico (Predecessor Tree)
El mapa de predecesores $\pi: V \to V$ satisface que el grafo inducido $G_\pi = (V_\pi, E_\pi)$, con $E_\pi = \{(\pi[v], v) : v \in V, \pi[v] \neq \text{null}\}$, constituye un árbol dirigido enraizado en $\text{origen}$ que conecta la fuente con cada vértice explorado.

### Complejidad Asintótica Temporal y Espacial

La implementación utiliza una cola de prioridad basada en `MontonMinimo<NodoCosto>` con política de **actualización perezosa (lazy decrease-key)**, donde en lugar de reubicar un elemento en el heap, se inserta una nueva tupla `(vecinoId, nuevoCosto)` y se ignoran duplicados obsoletos mediante comprobación $O(1)$ en `visitados.contains(actual.id)`.

| Operación | Frecuencia Máxima | Coste por Operación | Coste Total |
| :--- | :--- | :--- | :--- |
| Inicialización de distancias | $|V|$ vértices | $O(1)$ | $O(|V|)$ |
| Extracciones del Min-Heap (`extraerMinimo`) | $|E|$ inserciones en peor caso | $O(\log |E|) = O(\log |V|)$ | $O(|E| \log |V|)$ |
| Verificación de visitados | $|E|$ elementos | $O(1)$ amortizado | $O(|E|)$ |
| Exploración de aristas adyacentes | $\sum_{u \in V} \text{deg}^+(u) = |E|$ | $O(1)$ por arista | $O(|E|)$ |
| Inserciones en Min-Heap (`insertar`) | A lo sumo $|E|$ relajaciones | $O(\log |E|) = O(\log |V|)$ | $O(|E| \log |V|)$ |
| **Tiempo Asintótico Total** | — | — | **$\mathcal{O}((|V| + |E|) \log |V|)$** |

> **Observación Topológica para Redes Viales Urbanas:**  
> Las redes de transporte son grafos planares o cuasi-planares con grado de salida acotado ($\text{deg}^+(u) \le 6$ en intersecciones típicas), por lo que $|E| \le 3|V| - 6 \in \mathcal{O}(|V|)$. En este régimen, la complejidad degenera elegantemente en:
> $$\mathcal{O}(|V| \log |V|)$$

- **Complejidad Espacial:** $\mathcal{O}(|V| + |E|)$ debido a:
  - `Map<String, Double> costos`: $|V|$ entradas.
  - `Map<String, String> predecesor`: $|V|$ entradas.
  - `Set<String> visitados`: $|V|$ entradas.
  - `MontonMinimo<NodoCosto> cola`: A lo sumo $|E|$ entradas simultáneas en el peor caso.

### Reconstrucción LIFO de Trayectorias

La reconstrucción del camino óptimo desde el vértice `destinoId` hacia `origenId` se realiza mediante `PilaRecorrido<String>`, una estructura LIFO implementada desde cero:
1. Se itera desde `destinoId` siguiendo la cadena de predecesores $\pi$: $O(L)$ pasos, donde $L$ es el número de aristas del camino mínimo ($L \le |V|$).
2. Cada nodo se apila en la estructura LIFO.
3. Se desapilan consecutivamente los elementos, invirtiendo el orden temporal a $\mathcal{O}(L)$ para emitir la secuencia directa $\text{origen} \to v_1 \to v_2 \to \dots \to \text{destino}$.

---

## 3. Árbol AVL Auto-Balanceado (`ArbolAVL.java`)

### Propiedades Topológicas y Cota de Altura de Fibonacci

Un árbol AVL es un árbol binario de búsqueda (BST) estrictamente balanceado en altura. Para cada nodo interno $u \in T$, se define el **factor de balanceo** como:

$$\text{BF}(u) = \text{altura}(u.\text{izquierdo}) - \text{altura}(u.\text{derecho})$$

El invariante de balance establece que:

$$\forall u \in T, \quad \text{BF}(u) \in \{-1, 0, +1\}$$

#### Teorema: Cota Superior de Altura Asintótica
El número mínimo de nodos $N(h)$ en un árbol AVL de altura $h$ viene dado por la relación de recurrencia de los números de Fibonacci:
$$N(0) = 0, \quad N(1) = 1, \quad N(h) = 1 + N(h - 1) + N(h - 2)$$
Resolviendo la ecuación en diferencias finitas:
$$N(h) = F_{h+2} - 1 \approx \frac{\phi^{h+2}}{\sqrt{5}} - 1, \quad \text{donde } \phi = \frac{1 + \sqrt{5}}{2} \approx 1.6180339$$
Despejando $h$:
$$h < \frac{\ln(N + 2)}{\ln \phi} - 0.328 \approx 1.4404 \log_2(N + 2) - 0.328$$

Esto garantiza que la altura máxima nunca excederá el **44%** de la altura de un árbol binario perfectamente balanceado, asegurando operaciones en tiempo logarítmico estricto en el peor caso posible.

### Álgebra y Taxonomía de las 4 Rotaciones (LL, RR, LR, RL)

Cuando una inserción o eliminación rompe la condición $|\text{BF}(u)| \le 1$, se aplica una de las cuatro transformaciones locales que restauran el invariante en tiempo $O(1)$ sin violar la propiedad de orden simétrico del BST:

```
      Caso LL (Rotación Simple Derecha)                Caso RR (Rotación Simple Izquierda)
             y (BF = +2)                                    x (BF = -2)
            / \                                            / \
           x   T3       ===>      x                       T1  y        ===>      y
          / \                    / \                         / \                / \
         z   T2                 z   y                       T2  z              x   z
                                   / \                                        / \
                                  T2  T3                                     T1  T2

      Caso LR (Doble Rotación Izq-Der)                 Caso RL (Doble Rotación Der-Izq)
             y (BF = +2)                                    y (BF = -2)
            / \                                            / \
           x   T4       ===> RotarIzq(x)                  T1  x        ===> RotarDer(x)
          / \                luego                             / \           luego
         T1  z             RotarDer(y)                        z   T4       RotarIzq(y)
```

#### 1. Rotación Simple a la Derecha (Caso Izquierda-Izquierda, LL)
- **Condición de activación:** $\text{BF}(y) > 1$ y $(\text{clave} < y.\text{izquierdo}.\text{clave} \lor \text{BF}(y.\text{izquierdo}) \ge 0)$.
- **Operación algebraica:**
  $$x \gets y.\text{izquierdo}, \quad T_2 \gets x.\text{derecho}$$
  $$x.\text{derecho} \gets y, \quad y.\text{izquierdo} \gets T_2$$
  $$\text{altura}(y) \gets 1 + \max(h(y.\text{izq}), h(y.\text{der}))$$
  $$\text{altura}(x) \gets 1 + \max(h(x.\text{izq}), h(x.\text{der}))$$

#### 2. Rotación Simple a la Izquierda (Caso Derecha-Derecha, RR)
- **Condición de activación:** $\text{BF}(x) < -1$ y $(\text{clave} > x.\text{derecho}.\text{clave} \lor \text{BF}(x.\text{derecho}) \le 0)$.
- **Operación algebraica:**
  $$y \gets x.\text{derecho}, \quad T_2 \gets y.\text{izquierdo}$$
  $$y.\text{izquierdo} \gets x, \quad x.\text{derecho} \gets T_2$$
  $$\text{altura}(x) \gets 1 + \max(h(x.\text{izq}), h(x.\text{der}))$$
  $$\text{altura}(y) \gets 1 + \max(h(y.\text{izq}), h(y.\text{der}))$$

#### 3. Doble Rotación Izquierda-Derecha (Caso Izquierda-Derecha, LR)
- **Condición de activación:** $\text{BF}(y) > 1$ y $(\text{clave} > y.\text{izquierdo}.\text{clave} \lor \text{BF}(y.\text{izquierdo}) < 0)$.
- **Operación:**
  $$y.\text{izquierdo} \gets \text{rotarIzquierda}(y.\text{izquierdo})$$
  $$\text{resultado} \gets \text{rotarDerecha}(y)$$

#### 4. Doble Rotación Derecha-Izquierda (Caso Derecha-Izquierda, RL)
- **Condición de activación:** $\text{BF}(y) < -1$ y $(\text{clave} < y.\text{derecho}.\text{clave} \lor \text{BF}(y.\text{derecho}) > 0)$.
- **Operación:**
  $$y.\text{derecho} \gets \text{rotarDerecha}(y.\text{derecho})$$
  $$\text{resultado} \gets \text{rotarIzquierda}(y)$$

### Invariantes en Inserción y Eliminación

1. **Invariante de Orden Simétrico BST:**
   $$\forall u \in T, \quad k_L \in \text{claves}(u.\text{izq}) \implies k_L < u.\text{clave} \quad \land \quad k_R \in \text{claves}(u.\text{der}) \implies u.\text{clave} < k_R$$
2. **Invariante de Factor de Balance:**
   Al finalizar cada método `insertarRec` o `eliminarRec`, el desenrollado recursivo recalcula la altura de abajo hacia arriba y ejecuta `balancear(nodo)`. Por consiguiente:
   $$\forall u \in T, \quad |\text{BF}(u)| \le 1$$
3. **Sucesor In-Order en Eliminación:**
   Cuando se elimina un nodo con dos subárboles no nulos, se extrae el sucesor simétrico inmediato ($\min(u.\text{derecho})$), manteniendo el orden BST sin crear fragmentaciones estructurales.

### Complejidad Asintótica

| Operación | Complejidad Temporal | Justificación Mecánica |
| :--- | :--- | :--- |
| **Búsqueda (`buscar`)** | $\mathcal{O}(\log N)$ | Recorrido descendente acotado por la altura $h \le 1.44 \log_2 N$. |
| **Inserción (`insertar`)** | $\mathcal{O}(\log N)$ | Búsqueda $O(\log N)$ + a lo sumo 2 rotaciones $O(1)$. |
| **Eliminación (`eliminar`)** | $\mathcal{O}(\log N)$ | Búsqueda $O(\log N)$ + a lo sumo $O(\log N)$ rotaciones hacia la raíz. |
| **Recorrido In-Order** | $\mathcal{O}(N)$ | Visita cada nodo exactamente una vez, produciendo una secuencia estrictamente ordenada. |
| **Espacio de Almacenamiento** | $\mathcal{O}(N)$ | $N$ nodos con punteros `izquierdo`, `derecho` y campo entero `altura`. |

---

## 4. Montón Mínimo Binario (`MontonMinimo.java`)

### Estructura en Arreglo Plano e Indexación

El montículo mínimo es un árbol binario completo modelado en un arreglo lineal contiguo `Object[] datos`. Para cualquier nodo ubicado en la posición $i$ ($0 \le i < \text{tamanio}$):

$$\text{padre}(i) = \left\lfloor \frac{i - 1}{2} \right\rfloor \quad (i > 0)$$
$$\text{hijo\_izquierdo}(i) = 2i + 1$$
$$\text{hijo\_derecho}(i) = 2i + 2$$

### Invariantes Estructural y de Orden Parcial

#### Invariante Estructural (Complete Binary Tree Invariant)
El árbol está completamente lleno en todos sus niveles $0, 1, \dots, h-1$, y el último nivel $h$ se llena estrictamente de izquierda a derecha sin posiciones vacías intermedias. Se preserva al insertar siempre en la posición contigua `datos[tamanio]` y al extraer reemplazando la raíz con `datos[tamanio - 1]`.

#### Invariante de Orden Min-Heap (Heap-Order Invariant)
$$\forall i \in \{1, \dots, \text{tamanio} - 1\}, \quad \text{datos}\left[\left\lfloor \frac{i - 1}{2} \right\rfloor\right] \le \text{datos}[i]$$

**Corolario directo:**
$$\text{datos}[0] = \min_{0 \le k < \text{tamanio}} \text{datos}[k]$$
La raíz siempre contiene el elemento mínimo absoluto de la colección, permitiendo una consulta en tiempo $\mathcal{O}(1)$ (`verMinimo`).

### Dinámica de Percolación (Sift-Up / Sift-Down)

#### Percolación Ascendente (`subirHastaRaiz`)
- Se invoca tras colocar el nuevo elemento al final del arreglo (`datos[tamanio]`).
- Mientras $pos > 0$ y $\text{datos}[pos] < \text{datos}[\text{padre}(pos)]$, se realiza un intercambio.
- Número de comparaciones e intercambios acotado por la profundidad del árbol:
  $$\text{Intercambios} \le \lfloor \log_2(\text{tamanio}) \rfloor \implies \mathcal{O}(\log N)$$

#### Percolación Descendente (`bajarHastaHoja`)
- Se invoca tras colocar el último elemento en la raíz (`datos[0]`) y decrementar el tamaño.
- En cada paso, se evalúan los hijos izquierdo y derecho:
  $$\text{menor} = \arg\min \{\text{datos}[pos], \text{datos}[2pos + 1], \text{datos}[2pos + 2]\}$$
- Si $\text{menor} \neq pos$, se intercambian y se continúa el descenso; de lo contrario, el invariante ha quedado restaurado.
- Número de pasos acotado por la altura del árbol:
  $$\text{Comparaciones} \le 2 \lfloor \log_2(\text{tamanio}) \rfloor \implies \mathcal{O}(\log N)$$

### Análisis Amortizado de Capacidad

El arreglo `datos` se inicializa con capacidad $\mathcal{C}_0 = 16$. Cuando $\text{tamanio} = \mathcal{C}$, se duplica la capacidad mediante `redimensionar()`:
$$\mathcal{C}_{new} = 2 \times \mathcal{C}_{old}$$
Por el método contable o de potencial:
- Cada inserción ordinaria aporta 1 unidad de coste real y 2 créditos virtuales.
- Al momento de duplicar desde tamaño $K$ hasta $2K$, los $K/2$ elementos insertados desde la última duplicación han acumulado $2 \times (K/2) = K$ créditos, pagando exactamente la copia de los $K$ elementos en el nuevo arreglo.
- **Coste amortizado por inserción:** $\mathcal{O}(1)$ amortizado ($\mathcal{O}(\log N)$ peor caso al percolar).

---

## 5. Auditoría y Estado de la Suite de Pruebas Unitarias

### Diagnóstico de Cobertura Inicial

Durante la fase de inspección quirúrgica del repositorio, se identificaron las siguientes brechas respecto a los requerimientos de auditoría:
1. **Brecha en `ArbolAVLTest`:**
   - La suite original sólo incluía pruebas para rotaciones simples LL (`testAutoBalanceoIzquierdaIzquierda`) y RR (`testAutoBalanceoDerechaDerecha`).
   - Las rotaciones dobles **LR (Izquierda-Derecha)** y **RL (Derecha-Izquierda)** no contaban con cobertura unitaria explícita.
   - Las pruebas no comprobaban las propiedades estructurales observables (clave de la raíz, altura efectiva y verificación booleana de balance $|\text{BF}| \le 1$).
2. **Brecha en `MontonMinimoTest`:**
   - Se validaba el orden de extracción secuencial, pero no se verificaba el cumplimiento del invariante de montículo $\text{padre} \le \text{hijo}$ en cada nivel del arreglo interno tras cada operación.
   - No existían pruebas para valores negativos ni duplicados concurrentes.

### Ampliación de Casos de Prueba

Se incorporaron métodos de verificación formal en las clases productivas y se expandió la suite de pruebas:
- En `ArbolAVL`:
  - `int getAltura()`
  - `K getClaveRaiz()`
  - `int getFactorBalanceRaiz()`
  - `boolean estaBalanceado()`: validación recursiva integral de $|\text{BF}(u)| \le 1$ en todo el árbol.
- En `MontonMinimo`:
  - `boolean cumpleInvarianteMinHeap()`: verificación algorítmica $\forall i > 0, \text{datos}[\lfloor (i - 1) / 2 \rfloor] \le \text{datos}[i]$.
- En `ArbolAVLTest`:
  - `testAutoBalanceoIzquierdaDerecha`: Valida Caso LR (rotación doble izquierda-derecha con inserción 30 -> 10 -> 20; la raíz balanceada es 20 con altura 2).
  - `testAutoBalanceoDerechaIzquierda`: Valida Caso RL (rotación doble derecha-izquierda con inserción 10 -> 30 -> 20; la raíz balanceada es 20 con altura 2).
  - `testInvarianteBalanceMasivo`: Inserción secuencial de 100 elementos verificando $h \le 10$ y `estaBalanceado() == true`.
- En `MontonMinimoTest`:
  - `testInvariantePropiedadMinHeap`: Verificación continua del invariante interno en inserciones y extracciones sucesivas.
  - `testDuplicadosYNegativos`: Comprobación estricta de orden con números negativos $(-20, -5)$, cero y claves duplicadas.

### Matriz de Verificación JUnit 5

| Clase de Prueba | Test Case | Comportamiento / Invariante Validado | Estado |
| :--- | :--- | :--- | :--- |
| `ArbolAVLTest` | `testInicializacionVacia` | Estado inicial vacío, tamaño 0 y raíz nula | **PASÓ** |
| `ArbolAVLTest` | `testInsercionYBusqueda` | Inserción básica de nodos y recuperación $O(\log N)$ | **PASÓ** |
| `ArbolAVLTest` | `testActualizacionClaveDuplicada` | Idempotencia en tamaño ante clave duplicada | **PASÓ** |
| `ArbolAVLTest` | `testAutoBalanceoIzquierdaIzquierda` | **Rotación Simple LL:** Raíz = 20, Altura = 2, $|\text{BF}| \le 1$ | **PASÓ** |
| `ArbolAVLTest` | `testAutoBalanceoDerechaDerecha` | **Rotación Simple RR:** Raíz = 20, Altura = 2, $|\text{BF}| \le 1$ | **PASÓ** |
| `ArbolAVLTest` | `testAutoBalanceoIzquierdaDerecha` | **Rotación Doble LR:** Raíz = 20, Altura = 2, $|\text{BF}| \le 1$ | **PASÓ** |
| `ArbolAVLTest` | `testAutoBalanceoDerechaIzquierda` | **Rotación Doble RL:** Raíz = 20, Altura = 2, $|\text{BF}| \le 1$ | **PASÓ** |
| `ArbolAVLTest` | `testInvarianteBalanceMasivo` | 100 inserciones: Cota de altura $h \le 1.44 \log_2(102) \le 10$ | **PASÓ** |
| `ArbolAVLTest` | `testEliminacion` | Eliminación con supresión de nodo y reajuste de tamaño | **PASÓ** |
| `ArbolAVLTest` | `testEliminacionInexistente` | Retorno `false` ante clave no registrada | **PASÓ** |
| `ArbolAVLTest` | `testClaveNula` | Contrato defensivo: `IllegalArgumentException` ante `null` | **PASÓ** |
| `MontonMinimoTest` | `testInicializacionVacia` | Heap inicial vacío con capacidad por defecto 16 | **PASÓ** |
| `MontonMinimoTest` | `testOrdenDeExtraccion` | Extracción monótona ascendente $O(\log N)$ | **PASÓ** |
| `MontonMinimoTest` | `testRedimensionamientoDinamico` | 50 elementos: Duplicación geométrica sin pérdida de datos | **PASÓ** |
| `MontonMinimoTest` | `testExcepcionesEnVacio` | `NoSuchElementException` en `extraerMinimo` y `verMinimo` | **PASÓ** |
| `MontonMinimoTest` | `testInsercionNula` | Contrato defensivo: `IllegalArgumentException` ante `null` | **PASÓ** |
| `MontonMinimoTest` | `testVaciar` | Reinicio de tamaño y restablecimiento de capacidad base | **PASÓ** |
| `MontonMinimoTest` | `testInvariantePropiedadMinHeap` | $\forall i > 0, \text{datos}[\text{padre}(i)] \le \text{datos}[i]$ en cada paso | **PASÓ** |
| `MontonMinimoTest` | `testDuplicadosYNegativos` | Ordenamiento canónico con valores negativos y duplicados | **PASÓ** |

### Resumen de Ejecución Maven Surefire
```
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running com.trafico.estructuras.ArbolAVLTest
[INFO] Tests run: 11, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.501 s -- in com.trafico.estructuras.ArbolAVLTest
[INFO] Running com.trafico.estructuras.MontonMinimoTest
[INFO] Tests run: 8, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.066 s -- in com.trafico.estructuras.MontonMinimoTest
[INFO] 
[INFO] Results:
[INFO] 
[INFO] Tests run: 19, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
```

---

## 6. Conclusiones y Dictamen de Auditoría

1. **Dijkstra:** La implementación en `RutaMasCorta.java` es algorítmicamente correcta, respeta la propiedad de no-negatividad de aristas y alcanza la complejidad óptima $\mathcal{O}((|V| + |E|) \log |V|)$ mediante el uso del `MontonMinimo`.
2. **Árbol AVL:** Cumple estrictamente con la invariante de balance $|\text{BF}(u)| \le 1$ y cota de altura $h < 1.44 \log_2(N + 2)$. Se demostraron y probaron exitosamente los 4 casos de rotación: **LL**, **RR**, **LR** y **RL**.
3. **Montón Mínimo:** La propiedad de orden $\text{padre} \le \text{hijo}$ se preserva de manera determinista tanto en percolación ascendente (`subirHastaRaiz`) como descendente (`bajarHastaHoja`), soportando duplicación geométrica amortizada $O(1)$.
4. **Calidad de Código y Suites:** Se elevó la cobertura de pruebas de 14 a 19 tests, garantizando una validación completa y rigurosa sin advertencias ni regresiones.
