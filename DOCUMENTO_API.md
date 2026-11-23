Documento de la API · Versión 2

Proyecto: TORRESCOMS Configurador de PC
Autor: salvatore915
Rama: Proyecto-V2


1. API utilizada

DummyJSON, apartado Products. Es una API pública de productos de prueba.

Dirección base: https://dummyjson.com/
Documentación oficial: https://dummyjson.com/docs/products
Clave o registro: no hacen falta. Ningún ejemplo de la documentación oficial usa claves ni cabeceras de autenticación.
Formato: JSON sobre HTTPS, con peticiones GET.
Imágenes: son URLs de cdn.dummyjson.com y las cargo con Glide.


2. Endpoints que uso

GET /products/category/laptops
Lo uso para obtener la lista de portátiles.

GET /products/category/mobile-accessories
Lo uso para obtener la lista de accesorios.

GET /products/search?q={texto}
Lo uso para el buscador.

GET /products/{id}
Lo uso para mostrar la ficha de un producto.

Las listas responden con estos campos: products, total, skip y limit.


3. Qué datos ofrece

Hoy el catálogo que consulto tiene 19 productos: 5 portátiles (Apple, Asus, Huawei, Lenovo y Dell) y 14 accesorios (auriculares, cargadores, soportes...).

No incluye componentes sueltos como CPU, GPU, RAM o discos.


4. Por qué la elijo

Es gratuita, no necesita clave y devuelve JSON con imágenes. Esto permite usar Retrofit, Room y Glide.

Tiene lista, búsqueda, filtro por categoría y detalle, que son las pantallas que pide el proyecto.

Se relaciona con mi app: el usuario puede consultar equipos y accesorios de tecnología y añadirlos a su lista de componentes.

Limitación: como son datos de prueba, no son componentes reales de un PC. Los productos que se añaden a la lista local entran con la categoría "Otro", salvo que su título contenga SSD, RAM o CPU.


5. Datos que muestro en la app

id: se usa como clave en la caché de Room.
title: se ve en la lista y en el detalle.
brand y category: se ven en la lista y en el detalle.
price: se ve en la lista y en el detalle. La API no indica la moneda, así que la app lo muestra con "€".
rating y stock: se ven en la lista y en el detalle.
description: se ve en el detalle.
thumbnail: es la imagen que se ve en la lista y en el detalle.
discountPercentage: se guarda en Room.