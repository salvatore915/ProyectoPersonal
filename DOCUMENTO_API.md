# Propuesta y Documentación de la API · Versión 2

**Proyecto:** TORRECOMS - Configurador de PC  
**Alumno / Desarrollador:** salvatore915  
**Rama:** `Proyecto-V2`  

---

## 1. Nombre de la API
**DummyJSON Products API** (Módulo de Hardware, Laptops y Accesorios Tecnológicos).

## 2. Dirección Web y Endpoints
- **URL Base:** `https://dummyjson.com/`
- **Documentación oficial:** `https://dummyjson.com/docs/products`
- **Endpoints utilizados:**
  - `GET /products/category/laptops` — Obtiene la lista de ordenadores portátiles y estaciones de trabajo.
  - `GET /products/category/mobile-accessories` — Obtiene componentes y periféricos complementarios.
  - `GET /products/search?q={query}` — Búsqueda en tiempo real de productos/componentes por texto o marca.
  - `GET /products/{id}` — Consulta detallada del producto individual.

## 3. Características Técnicas
- **Autenticación:** Pública y 100% gratuita. **No requiere registro ni API Key** ni encabezados de autorización.
- **Formato:** Respuestas en formato **JSON** estructurado estándar (REST / HTTP GET).
- **Imágenes:** Servidas a través de CDN de alta velocidad (`https://cdn.dummyjson.com/...`) en formato WebP optimizado para dispositivos móviles, compatible con Glide.

## 4. Finalidad y Justificación de la Elección
La aplicación **TORRECOMS** fue concebida como un gestor y configurador de componentes de PC (CPUs, GPUs, RAM, Discos y Periféricos). 

La integración de la API DummyJSON encaja de forma natural con esta temática porque:
1. Proporciona datos reales y actualizados de ordenadores y tecnología (marcas reconocidas como Apple, Asus, Dell, Lenovo, HP).
2. Ofrece especificaciones técnicas concretas: título, descripción detallada, marca, categoría, precio oficial, stock disponible, valoración de usuarios (rating) y galerías fotográficas.
3. Permite al usuario consultar un catálogo de hardware en línea e **importar directamente componentes al presupuesto local de su ordenador**, integrando la tienda online con la base de datos local de la app.

## 5. Datos Concretos Utilizados en la App
| Campo JSON | Tipo de Dato | Uso en la Aplicación |
| :--- | :--- | :--- |
| `id` | `Int` | Clave primaria para indexación y caché local en Room. |
| `title` | `String` | Nombre comercial del producto mostrado en listas y detalle. |
| `description` | `String` | Ficha técnica y especificaciones detalladas en la vista de detalle. |
| `price` | `Double` | Precio en euros (€) para presupuestar la torre o equipo. |
| `category` | `String` | Categorización del hardware ("laptops", "accessories"). |
| `brand` | `String` | Fabricante del equipo (Apple, ASUS, Dell, etc.). |
| `thumbnail` | `String` | URL de la imagen principal cargada con **Glide** en listas y detalle. |
| `rating` | `Double` | Puntuación otorgada por la comunidad (estrellas ★). |
| `stock` | `Int` | Disponibilidad de inventario en almacén. |
| `discountPercentage` | `Double` | Descuento aplicable sobre el precio recomendado. |
