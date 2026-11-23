Memoria técnica · Versión 2

Proyecto: TORRECOMS Configurador de PC
Autor: salvatore915
Rama: Proyecto-V2


1. Conexión a Internet

Permisos (AndroidManifest.xml): INTERNET y ACCESS_NETWORK_STATE.

Uso Retrofit 2.11.0 con Gson. La interfaz ApiService y el objeto RetrofitClient están en network/ApiService.kt.

Uso corrutinas. Todas las funciones de la API son suspend y el repositorio trabaja con Dispatchers.IO. Así, la red nunca bloquea la pantalla principal.


2. Room y modo sin conexión

La entidad ProductoOnline (tabla productos_online) tiene estos campos: id, title, description, price, category, brand, thumbnail, rating, stock y discountPercentage.

El ProductoOnlineDao permite guardar productos (reemplazando los antiguos), buscar por id, buscar por categoría y buscar por texto en título, descripción y marca.

AppDatabase está en la versión 3 y contiene las entidades Componente y ProductoOnline.

Cómo funciona sin conexión (CatalogoRepository):

Primero, se pide el catálogo a la API. Si responde, se guarda en Room y se muestra.

Segundo, si falla la red, se leen los datos guardados en Room y se muestran con un aviso de "sin conexión".

Tercero, si tampoco hay datos guardados, se muestra un mensaje de error con el botón Reintentar.

La app no se cierra por falta de red. Glide guarda en su propia caché las imágenes que ya se han visto.


3. Arquitectura

El flujo de la app es este:

CatalogoActivity usa CatalogoViewModel.
CatalogoViewModel usa CatalogoRepository.
CatalogoRepository usa tres cosas: ApiService (la red), ProductoOnlineDao (Room) y ComponenteDao (Room).

Vista: CatalogoActivity muestra la lista, la búsqueda y los filtros. DetalleProductoActivity muestra la ficha del producto. CatalogoAdapter es el RecyclerView que usa Glide para las imágenes.

ViewModel: CatalogoViewModel ofrece un StateFlow con tres estados: Loading (cargando), Success (correcto) y Error.

Repositorio: decide si usar la red o Room. DetalleProductoActivity consulta el repositorio directamente, sin ViewModel.


4. Mejora propia

Añadir un producto del catálogo a mis componentes.

En la pantalla de detalle, el botón "Añadir a mis componentes" crea un Componente con el título y el precio del producto y lo guarda en la tabla componentes. Así aparece en la lista y en los totales de la V1.

La categoría local es "Otro", salvo que el título contenga estas palabras: si contiene SSD o Disk, la categoría es Disco. Si contiene RAM o Memory, la categoría es RAM. Si contiene CPU o Processor, la categoría es CPU.

Además hay búsqueda por texto y filtros por categoría: Todos, Laptops y Accesorios.


5. Funciones de la V1 que se mantienen

Formulario con validación, SharedPreferences, cámara y galería, GPS, historial con borrado y totales, y sonidos según el precio.