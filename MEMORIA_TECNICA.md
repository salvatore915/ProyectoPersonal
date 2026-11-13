# Memoria Técnica · Versión 2: Tu App Conectada a Internet

**Proyecto:** TORRECOMS - Configurador de PC  
**Rama:** `Proyecto-V2`  
**Autor:** salvatore915  

---

## 1. Integración de la API (Retrofit + Gson + Corrutinas)
Para comunicar la aplicación con Internet se empleó la arquitectura estándar moderna de Android:
- **Permisos en `AndroidManifest.xml`:** Se incorporaron los permisos `<uses-permission android:name="android.permission.INTERNET" />` y `<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />`.
- **Cliente HTTP y Serialización:** Se configuró `Retrofit` (v2.11.0) junto con `GsonConverterFactory` en el singleton `RetrofitClient`.
- **Interfaz del Servicio (`DummyJsonApi`):** Define los métodos HTTP REST (`@GET`) mediante funciones suspendidas (`suspend fun`), garantizando que ninguna llamada de red bloquee el hilo principal (UI thread).
- **Procesamiento Asíncrono:** Todas las consultas de red se despachan en el hilo de E/S (`Dispatchers.IO`) dentro del repositorio.

---

## 2. Almacenamiento Local en Room y Persistencia de Caché
Para cumplir con el requisito de persistencia de datos remotos:
- **Entidad `ProductoOnline` (`@Entity(tableName = "productos_online")`):** Contiene la información consultada de la API (`id`, `title`, `description`, `price`, `category`, `brand`, `thumbnail`, `rating`, `stock`, `discountPercentage`).
- **DAO `ProductoOnlineDao`:** Proporciona operaciones optimizadas para:
  - Inserción masiva con estrategia de reemplazo (`OnConflictStrategy.REPLACE`).
  - Obtención completa de la caché ordenada (`obtenerTodos()`).
  - Consulta de detalle por identificador (`obtenerPorId(id)`).
  - Búsqueda textual mediante SQL `LIKE` sobre título, descripción o marca.
- **Evolución de la Base de Datos (`AppDatabase`):** Se actualizó a la versión 3, integrando las entidades `Componente` (datos locales de la V1) y `ProductoOnline` (datos de Internet en caché).

---

## 3. Funcionamiento del Modo Sin Conexión (Offline First)
La app garantiza una experiencia de usuario sólida ante fallos o ausencias de conectividad:
1. **Flujo de Petición en el Repositorio (`CatalogoRepository`):**
   - Cuando el usuario accede al catálogo o realiza una búsqueda, el repositorio intenta consultar los datos más recientes de la API.
   - Si la petición tiene éxito, almacena de forma transparente la respuesta en Room y emite los datos con la bandera `esDesdeCache = false`.
   - Si se produce una excepción de red (`UnknownHostException`, `SocketTimeoutException`, etc.), el repositorio captura el fallo y consulta automáticamente la base de datos local Room.
   - Si Room contiene datos previamente cacheados, los retorna con `esDesdeCache = true`.
2. **Experiencia en la Interfaz (UI):**
   - Si se muestran datos de la caché, la interfaz activa de forma visible el banner amarillo:  
     `⚠️ Sin conexión a Internet. Mostrando datos guardados en Room.`
   - La aplicación **nunca sufre un cierre inesperado (crash)** por falta de red.
   - Si el dispositivo carece de conexión y tampoco existen datos en Room, se muestra una pantalla amigable con un botón **"Reintentar"**.
   - Para las imágenes remotas, **Glide** aprovecha su propia caché de disco y memoria para seguir mostrándolas sin conexión.

---

## 4. Arquitectura del Proyecto (MVVM + Repository)
El código se reorganizó siguiendo los principios de arquitectura recomendados por Google:
- **Capa de Datos:**
  - Red: `network/DummyJsonApi.kt`, `network/RetrofitClient.kt`.
  - Local: `ProductoOnline.kt`, `ProductoOnlineDao.kt`, `AppDatabase.kt`.
- **Capa de Repositorio (`repository/CatalogoRepository.kt`):**
  - Es la única fuente de la verdad para el catálogo.
  - Abstrae la toma de decisiones entre red y almacenamiento local.
- **Capa de Lógica de Presentación (`viewmodel/CatalogoViewModel.kt`):**
  - Utiliza `StateFlow<CatalogoUiState>` para emitir estados reactivos (`Loading`, `Success`, `Error`).
  - Gestiona el ciclo de vida con `viewModelScope` evitando pérdidas de memoria.
- **Capa de Vista (UI):**
  - `CatalogoActivity`: Muestra la lista, filtros de categoría y barra de búsqueda.
  - `DetalleProductoActivity`: Muestra la ficha técnica detallada del hardware con imagen en alta definición.
  - `CatalogoAdapter`: Adaptador de `RecyclerView` con soporte para Glide.

---

## 5. Mejora Propia Implementada y Justificación
Se diseñó e integró la funcionalidad: **"Añadir producto del catálogo online directamente a mis componentes locales de TORRECOMS"**.

### Justificación:
En la versión 1 de TORRECOMS, el usuario debía escribir manualmente cada componente y su precio para armar su torre. Con esta mejora:
- El usuario navega por el catálogo online real y abre la ficha de cualquier producto (ej. un SSD, Laptop o memoria).
- Al pulsar el botón **"➕ Añadir a mis componentes (TORRECOMS)"**, la app mapea automáticamente el producto online a un `Componente` local con su precio exacto y lo inserta en la tabla `componentes` de Room.
- Al regresar a la pantalla principal o al historial, el nuevo componente ya figura en el **recuento total de piezas**, el **precio acumulado en euros** y la lista de la torre.
- Además, se incorporó una **barra de búsqueda instantánea** y **filtros rápidos por categoría** ("Todos", "Laptops", "Accesorios") para encontrar rápidamente cualquier hardware.

---

## 6. Mantenimiento de Funcionalidades Anteriores
Se conservaron intactas todas las funcionalidades desarrolladas en la primera versión:
- Formulario de entrada con validación de precios y nombres.
- Persistencia de preferencias del usuario (`SharedPreferences`).
- Registro fotográfico con **Cámara** y **Galería** (`Fotos.kt`).
- Geolocalización con **GPS** (`FusedLocationProviderClient`).
- **Historial de componentes** con borrado reactivo y cálculo de totales en Room.
- Reproducción de efectos de audio contextuales según el precio en `SecondActivity`.
