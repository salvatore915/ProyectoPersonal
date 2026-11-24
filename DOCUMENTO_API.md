# DOCUMENTO DE LA API - VERSIÓN 2

Proyecto: TORRECOMS - Configurador de PC  
Autor: Salvador Gómez  
Rama Git: Proyecto-V2  

## 1. Identificación y Acceso a la API

- Nombre de la API: DummyJSON (Catálogo de Productos).
- Dirección base (URL): https://dummyjson.com/
- Documentación oficial: https://dummyjson.com/docs/products
- Tipo de acceso: API pública y gratuita. No requiere clave de acceso (API Key), registros ni cabeceras de autorización.
- Formato de datos: Respuestas en formato JSON estándar mediante protocolo HTTPS con peticiones HTTP/REST (GET).

## 2. Justificación de la Elección

La elección de DummyJSON responde directamente a los objetivos de la aplicación TORRECOMS:

- Alineación temática: Ofrece datos reales y estructurados de productos tecnológicos (portátiles y accesorios informáticos como ratones, teclados, soportes y periféricos).
- Utilidad real: Permite al usuario consultar un catálogo de hardware en línea y seleccionar productos para incorporarlos a su lista local de componentes.
- Calidad de los datos: Proporciona imágenes remotas de alta resolución, descripciones técnicas, precios, valoraciones y control de stock, lo que permite aprovechar librerías modernas como Glide.

## 3. Endpoints Utilizados en la Aplicación

La aplicación consume los siguientes puntos de acceso:

- Consulta de Portátiles:  
  GET /products/category/laptops  
  Obtiene la lista de ordenadores portátiles disponibles.

- Consulta de Accesorios:  
  GET /products/category/mobile-accessories  
  Obtiene periféricos y complementos para PC y dispositivos.

- Buscador en Tiempo Real:  
  GET /products/search?q={texto}  
  Permite buscar productos en el servidor según el término introducido por el usuario.

- Detalle del Producto:  
  GET /products/{id}  
  Recupera la información completa de un producto específico mediante su identificador numérico.

## 4. Datos Extraídos y Uso en la App

Cada producto recibido de la API se procesa con los siguientes campos:

- id (Int): Identificador único y clave primaria en la base de datos local Room.
- title (String): Nombre del producto mostrado en el listado y en la pantalla de detalle.
- price (Double): Precio del artículo, presentado con el formato en euros (€).
- category (String): Categoría del producto utilizada para filtrado y chips de navegación.
- brand (String): Marca del fabricante (ejemplo: Apple, Asus, Lenovo, Huawei).
- description (String): Especificaciones técnicas del producto mostradas en la ficha de detalle.
- thumbnail (String): Enlace directo a la imagen remota, descargada y visualizada con Glide.
- rating (Double): Puntuación del producto mostrada mediante estrellas.
- stock (Int): Unidades disponibles en el almacén virtual.
- discountPercentage (Double): Porcentaje de descuento registrado en la base de datos local.

## 5. Integración y Servicio Adicional de Traducción

- Traducción automática: Dado que las descripciones de DummyJSON se encuentran en inglés, en la pantalla de detalle se realiza una petición automática al servicio de traducción de Google para mostrar el texto en español de manera transparente para el usuario.
- Respaldo de seguridad: Si el servicio de traducción no responde o no hay cobertura, la app mantiene el texto original en inglés garantizando que la aplicación nunca se bloquee.
