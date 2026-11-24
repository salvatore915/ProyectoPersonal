# MEMORIA TÉCNICA - VERSIÓN 2

Proyecto: TORRECOMS - Configurador de PC  
Autor: Salvador Gómez  
Rama Git: Proyecto-V2  

## 1. Conexión a Internet y Red

- Permisos configurados (AndroidManifest.xml):  
  Se han incluido los permisos INTERNET y ACCESS_NETWORK_STATE para permitir la comunicación HTTP/REST y verificar la conectividad del dispositivo.
- Librerías de comunicación:  
  - Retrofit 2.11.0: Gestión de llamadas a la API a través de la interfaz declarativa ApiService.
  - Gson Converter 2.11.0: Deserialización automática de las respuestas JSON a modelos de datos Kotlin (ProductoDto).
  - El cliente HTTP centralizado está implementado en RetrofitClient (network/ApiService.kt).
- Concurrencia con Corrutinas:  
  Todas las funciones de acceso a la red son de tipo suspend y se despachan en el hilo de entrada/salida (Dispatchers.IO), asegurando que la interfaz de usuario se mantenga siempre fluida sin congelarse.

## 2. Almacenamiento Local (Room) y Modo Sin Conexión

- Entidad ProductoOnline: Tabla productos_online donde se persisten todos los campos obtenidos de la API (id, title, description, price, category, brand, thumbnail, rating, stock, discountPercentage).
- Acceso a datos (ProductoOnlineDao): Implementa inserciones con estrategia OnConflictStrategy.REPLACE, búsquedas por texto y filtrado por categorías.
- Base de datos unificada (AppDatabase): Actualizada a la Versión 3, gestionando simultáneamente las entidades locales (Componente) y las entidades en caché (ProductoOnline).
- Estrategia Offline-First:  
  1. Con conexión: Se realiza la petición a DummyJSON. Al responder, los datos se guardan en Room y se actualiza la interfaz.
  2. Sin conexión (Caché): Si la llamada falla por falta de red o modo avión, el repositorio captura el fallo de forma segura (try/catch) y lee los productos almacenados en Room. La interfaz muestra un aviso informativo: Modo sin conexión: mostrando catálogo guardado en Room.
  3. Sin datos y sin red: Se muestra un estado de error controlado con opción a reintentar.
  4. Caché de imágenes: La librería Glide conserva en su memoria local las fotos ya descargadas para que continúen visibles sin acceso a Internet.

## 3. Arquitectura y Organización del Código

El proyecto implementa la arquitectura recomendada por Google (MVVM - Model-View-ViewModel):

- Capa de Vista (UI):  
  - CatalogoActivity: Muestra el catálogo de productos con barra de búsqueda, chips de categorías y un indicador de carga (ProgressBar).
  - DetalleProductoActivity: Presenta la ficha completa del producto seleccionado con su imagen, precio y descripción traducida.
  - CatalogoAdapter: Adaptador de RecyclerView optimizado para el reciclado de vistas e imágenes.
- Capa de ViewModel:  
  - CatalogoViewModel: Expone un StateFlow reactivo con los estados Loading, Success (incluyendo indicador offline) y Error.
- Capa de Repositorio:  
  - CatalogoRepository: Actúa como fuente única de verdad, decidiendo de manera transparente cuándo solicitar datos a la red (ApiService) y cuándo leer de la base de datos local (ProductoOnlineDao).

## 4. Mejora Propia (Hito 3)

Se han implementado y justificado tres mejoras estrechamente ligadas a la temática de la app (Configurador de presupuestos de PC):

1. Búsqueda y Filtrado en Tiempo Real:  
   Barra de búsqueda interactiva y filtros por chips (Todos, Portátiles, Accesorios), operativos tanto online como sobre la base de datos local sin conexión.
2. Integración de la Tienda Online en el Presupuesto Local:  
   En la pantalla de detalle, el botón Añadir a mi configuración de PC permite importar cualquier producto de la API directamente a la tabla componentes de TORRECOMS, sumándose automáticamente al total económico de la app.
3. Mapeo Inteligente de Categorías:  
   Al añadir un producto online, se analiza su título para clasificarlo automáticamente en las categorías locales de la app:
   - Términos como SSD o Disk: Categoría Disco.
   - Términos como RAM o Memory: Categoría RAM.
   - Términos como Processor o CPU: Categoría CPU.
   - Resto de artículos y portátiles: Categoría Otro.
4. Traducción Automática al Español:  
   Integración de traducción al vuelo para las descripciones en inglés procedentes de la API.

## 5. Mantenimiento de Funcionalidades de la Versión 1

Se han conservado sin alteraciones todas las funciones de la primera versión:
- Formulario de entrada con validación de datos.
- Almacenamiento del último componente con SharedPreferences.
- Captura de imagen mediante Cámara y selección desde Galería.
- Geolocalización GPS con registro de coordenadas (FusedLocationProviderClient).
- Historial de componentes con cálculo de presupuestos y borrado individual.
- Asignación visual de iconos por categoría (CPU, GPU, RAM, DISCO, OTRO).
- Efectos sonoros con MediaPlayer en función del precio asignado.
