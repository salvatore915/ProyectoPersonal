Memoria técnica · Versión 2

Proyecto: TORRESCOMS Configurador de PC
Autor: salvatore915
Rama: Proyecto-V2

1. Conexión a Internet
   Permisos (AndroidManifest.xml): INTERNET y ACCESS_NETWORK_STATE.
   Retrofit 2.11.0 con Gson: la interfaz DummyJsonApi y el objeto RetrofitClient están en network/DummyJsonApi.kt.
   Corrutinas: todas las funciones de la API son suspend, y el repositorio trabaja con Dispatchers.IO, así que la red no bloquea el hilo principal.
2. Room y modo sin conexión
   Entidad ProductoOnline (tabla productos_online) con id, title, description, price, category, brand, thumbnail, rating, stock y discountPercentage.
   ProductoOnlineDao: inserta (reemplazando), consulta por id, por categoría y busca por texto en título, descripción y marca.
   AppDatabase: versión 3, con las entidades Componente y ProductoOnline.

Cómo funciona sin conexión (CatalogoRepository):

Se pide el catálogo a la API. Si responde, se guarda en Room y se muestra.
Si falla la red, se lee lo guardado en Room y se muestra con un aviso de "sin conexión".
Si tampoco hay datos guardados, se muestra un mensaje de error con el botón Reintentar.

La app no se cierra por falta de red. Glide usa su propia caché para las imágenes ya vistas.

3. Arquitectura
   CatalogoActivity ─► CatalogoViewModel ─► CatalogoRepository ─► DummyJsonApi (red)
   ├─► ProductoOnlineDao (Room)
   └─► ComponenteDao (Room)
   Vista: CatalogoActivity (lista, búsqueda y filtros), DetalleProductoActivity (ficha) y CatalogoAdapter (RecyclerView con Glide).
   ViewModel: CatalogoViewModel expone un StateFlow con los estados Loading, Success y Error.
   Repositorio: decide entre red y Room. DetalleProductoActivity consulta el repositorio directamente, sin ViewModel.
4. Mejora propia

Añadir un producto del catálogo a mis componentes. En el detalle, el botón "Añadir a mis componentes" crea un Componente con el título y el precio del producto y lo guarda en la tabla componentes, así aparece en la lista y en los totales de la V1.

La categoría local es "Otro", salvo que el título contenga SSD o Disk (Disco), RAM o Memory (RAM), o CPU o Processor (CPU).

Además hay búsqueda por texto y filtros por categoría (Todos, Laptops y Accesorios).

5. Funciones de la V1 que se mantienen

Formulario con validación, SharedPreferences, cámara y galería, GPS, historial con borrado y totales, y sonidos según el precio.