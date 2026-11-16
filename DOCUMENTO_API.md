# Documento de la API · Versión 2

**Proyecto:** TORRESCOMS Configurador de PC
**Autor:** salvatore915
**Rama:** `Proyecto-V2`

## API utilizada

**DummyJSON · Products** (API pública de productos de prueba).

- Dirección base: `https://dummyjson.com/`
- Documentación oficial: <https://dummyjson.com/docs/products>
- Clave o registro: **no hacen falta**. Ningún ejemplo de la documentación oficial usa claves ni cabeceras de autenticación.
- Formato: JSON sobre HTTPS, con peticiones `GET`.
- Imágenes: URLs de `cdn.dummyjson.com`, que cargo con Glide.

## Endpoints que uso

| Petición | Para qué la uso |
| --- | --- |
| `GET /products/category/laptops` | Lista de portátiles |
| `GET /products/category/mobile-accessories` | Lista de accesorios |
| `GET /products/search?q={texto}` | Buscador |
| `GET /products/{id}` | Ficha de un producto |

Las listas responden con `products`, `total`, `skip` y `limit`.

## Qué datos ofrece

Hoy el catálogo que consulto tiene **19 productos**: 5 portátiles (Apple, Asus, Huawei, Lenovo y Dell) y 14 accesorios (auriculares, cargadores, soportes...). No incluye componentes sueltos como CPU, GPU, RAM o discos.

## Por qué la elijo

- Es gratuita, sin clave, y devuelve JSON con imágenes, lo que permite usar Retrofit, Room y Glide.
- Tiene lista, búsqueda, filtro por categoría y detalle, que son las pantallas que pide el proyecto.
- Se relaciona con mi app: el usuario puede consultar equipos y accesorios de tecnología y añadirlos a su lista de componentes.

**Limitación:** al ser datos de prueba, no son componentes reales de un PC. Los productos que se añaden a la lista local entran como categoría "Otro", salvo que su título contenga SSD, RAM o CPU.

## Datos que muestro en la app

| Campo | Dónde se ve |
| --- | --- |
| `id` | Clave en la caché de Room |
| `title` | Lista y detalle |
| `brand` y `category` | Lista y detalle |
| `price` | Lista y detalle (la API no indica moneda; la app lo muestra con "€") |
| `rating` y `stock` | Lista y detalle |
| `description` | Detalle |
| `thumbnail` | Imagen en lista y detalle |
| `discountPercentage` | Se guarda en Room |