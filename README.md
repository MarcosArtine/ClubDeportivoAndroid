<p align="center">
  <img src="https://img.shields.io/badge/Club%20Deportivo-App%20Android-C01C28?style=for-the-badge&logo=android&logoColor=white" />
  <img src="https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white" />
  <img src="https://img.shields.io/badge/SQLite-003B57?style=for-the-badge&logo=sqlite&logoColor=white" />
  <img src="https://img.shields.io/badge/Android%20Studio-3DDC84?style=for-the-badge&logo=androidstudio&logoColor=white" />
</p>

<p align="center">
  <img src="https://img.shields.io/badge/IFTS%20N°29-DAM%20Comisión%20C-C01C28?style=for-the-badge" />
  <img src="https://img.shields.io/badge/Grupo%204-Artine%20·%20Cardozo%20·%20Alcaraz%20·%20Uribio%20·%20Vargas-212121?style=for-the-badge" />
</p>

---

# 🏟️ Club Deportivo — Sistema de Gestión Android

> Migración del sistema de escritorio C# al entorno Android.  
> Permite gestionar socios, no socios, pagos de cuotas, actividades y listados  
> directamente desde un dispositivo móvil.

---

## 📑 Índice

- [📋 Descripción general](#-descripción-general)
- [👥 Equipo](#-equipo)
- [🏗️ Arquitectura](#️-arquitectura)
- [📁 Estructura de carpetas](#-estructura-de-carpetas)
- [🗄️ Base de datos](#️-base-de-datos)
- [📱 Módulos y pantallas](#-módulos-y-pantallas)
- [🧭 Flujo de navegación](#-flujo-de-navegación)
- [🔄 Mejoras incorporadas](#-mejoras-incorporadas)
- [📖 Historias de usuario](#-historias-de-usuario)

---

## 📋 Descripción general

| Campo | Detalle |
|---|---|
| 🏫 **Institución** | IFTS N°29 |
| 📚 **Materia** | Desarrollo de Aplicaciones para Dispositivos |
| 🎓 **Carrera** | Tecnicatura en Desarrollo de Software |
| 👨‍🏫 **Docente** | Kevin Axel del Bello |
| 🗂️ **Proyecto base** | Sistema Club Deportivo (C# — DSOO y MDS) |
| 🖥️ **Entorno** | Android Studio · Kotlin · SQLite |

El sistema original fue desarrollado en **C# con Windows Forms** y MySQL. Esta app lo migra al entorno Android manteniendo todos los procesos principales e incorporando cuatro mejoras detectadas durante el análisis.

---

## 👥 Grupo 4 — Integrantes

| Integrante          |
|---------------------|
| 👩‍💻 Marisol Alcaraz |
| 👨‍💻 Marcos Artine   |
| 👩‍💻 Anahí Cardozo   |
| 👨‍💻 Jonatan Uribio  |
| 👩‍💻 Nancy Vargas    |

---

## 🏗️ Arquitectura

El proyecto sigue el patrón **DAO (Data Access Object)**, el mismo que se usó en C# durante DSOO, adaptado al entorno Android con SQLite.

```
┌─────────────────────────────────────────────────────────┐
│                     ACTIVITY                            │
│      pantalla — maneja eventos del usuario              │
└──────────────────────┬──────────────────────────────────┘
                       │ usa
┌──────────────────────▼──────────────────────────────────┐
│                      DAO                                │
│   consulta, inserta y actualiza en la base de datos     │
└──────────────────────┬──────────────────────────────────┘
                       │ abre
┌──────────────────────▼──────────────────────────────────┐
│                    BDatos                               │
│        SQLiteOpenHelper — gestiona Club.db              │
└──────────────────────┬──────────────────────────────────┘
                       │ opera sobre
┌──────────────────────▼──────────────────────────────────┐
│               SQLite  ·  Club.db                        │
│          base de datos local del dispositivo            │
└─────────────────────────────────────────────────────────┘
```

### 🔁 Comparación C# → Kotlin

| C# / DSOO             | Android / Kotlin                   |
|-----------------------|------------------------------------|
| `class SocioDAO`      | `class SocioDAO(context: Context)` |
| `List<Socio>`         | `List<Socio>`                      |
| `SqlConnection`       | `SQLiteDatabase`                   |
| `SqlDataReader`       | `Cursor`                           |
| `bool`                | `Boolean`                          |
| `using (transaction)` | `beginTransaction()`               |
| `DataTemplate` XAML   | `RecyclerView + Adapter`           |
| `Form.Activated`      | `onResume()`                       |
| `Socio?` nullable     | `Socio?` nullable                  |
| `record` con `with`   | `data class` con `copy()`          |

---

## 📁 Estructura de carpetas

```
ClubDeportivoAndroid/
└── app/src/main/
    ├── AndroidManifest.xml
    ├── java/com/grupo4/clubdeportivo/
    │   │
    │   ├── 📂 adapters/
    │   │   ├── ActividadAdapter.kt        
    │   │   └── SocioAdapter.kt            
    │   │
    │   ├── 📂 database/
    │   │   ├── BDatos.kt                  
    │   │   ├── 📂 models/
    │   │   │   ├── Persona.kt             
    │   │   │   ├── Socio.kt               
    │   │   │   ├── NoSocio.kt             
    │   │   │   ├── Actividad.kt           
    │   │   │   ├── PagoCuota.kt           
    │   │   │   ├── PagoDiario.kt          
    │   │   │   ├── PagoRealizado.kt       
    │   │   │   └── Usuario.kt             
    │   │   └── 📂 dao/
    │   │       ├── SocioDAO.kt            
    │   │       ├── ActividadDAO.kt        
    │   │       ├── PagoCuotaDAO.kt        
    │   │       ├── PagoDiarioDAO.kt       
    │   │       └── UsuarioDAO.kt          
    │   │
    │   ├── MainActivity.kt                
    │   ├── LoginActivity.kt               
    │   ├── RegisterActivity.kt            
    │   ├── HomeActivity.kt                
    │   ├── SocioActivity.kt               
    │   ├── NuevoSocioActivity.kt         
    │   ├── DetalleSocioActivity.kt        
    │   ├── ActividadesActivity.kt         
    │   ├── NuevaActividadActivity.kt      
    │   ├── PagosActivity.kt               
    │   ├── ListadoActivity.kt             
    │   └── SeguridadUtils.kt             
    │
    └── res/
        ├── 📂 layout/
        │   ├── activity_main.xml
        │   ├── activity_login.xml
        │   ├── activity_register.xml
        │   ├── activity_home.xml
        │   ├── activity_socio.xml         
        │   ├── activity_nuevo_socio.xml
        │   ├── activity_detalle_socio.xml 
        │   ├── activity_actividades.xml
        │   ├── activity_nueva_actividad.xml
        │   ├── activity_pagos.xml         
        │   ├── activity_listado.xml
        │   ├── item_socio.xml             
        │   ├── item_actividad.xml
        │   └── item_pago.xml
        ├── 📂 drawable/
        │   ├── ic_socio.xml · ic_socioitem.xml
        │   ├── ic_actividad.xml · ic_calendario.xml
        │   ├── ic_flecha_atras.xml · ic_option.xml
        │   ├── ic_logo.xml · ic_logo_blanco.xml
        │   └── spinner_background.xml
        ├── 📂 menu/
        │   └── menu_opciones_socio.xml    
        └── 📂 values/
            ├── colors.xml   → red_app #C01C28
            ├── strings.xml
            └── themes.xml
```

---

## 🗄️ Base de datos

**Archivo:** `BDatos.kt` · **BD:** `Club.db` · **Versión:** `2`

### Diagrama de tablas

```
┌──────────────┐     ┌──────────────────────────────────┐
│   Usuario    │     │            Persona               │
│──────────────│     │──────────────────────────────────│
│ UsuarioId PK │     │ PersonaId  INTEGER PK AUTOINCR.  │
│ Email UNIQUE │     │ Nombre     TEXT NOT NULL         │
│ Contrasena   │     │ Apellido   TEXT NOT NULL         │
│ Nombre       │     │ TipoDni    TEXT                  │
└──────────────┘     │ NroDni     TEXT NOT NULL UNIQUE  │
                     │ FechaNac.  TEXT                  │
                     │ Telefono   TEXT                  │
                     │ Email      TEXT                  │
                     │ AptoFisico INTEGER DEFAULT 1     │
                     └──────────────┬───────────────────┘
                                    │ 1
                        ┌───────────┴───────────┐
                        │                       │
               ┌────────▼───────┐     ┌─────────▼──────┐
               │     Socio      │     │    NoSocio      │
               │────────────────│     │─────────────────│
               │ SocioId     FK │     │ NoSocioId    FK │
               │ FechaAltaSocio │     └────────┬────────┘
               │ NroCarnet      │              │
               │ EstadoSocio    │     ┌────────▼──────────────┐
               └───────┬────────┘     │      PagoDiario        │
                       │             │────────────────────────│
               ┌───────▼────────┐    │ PagoDiarioId PK        │
               │   PagoCuota    │    │ NoSocioId    FK        │
               │────────────────│    │ ActividadId  FK        │
               │ CuotaId     PK │    │ FechaPago              │
               │ SocioId     FK │    │ MontoPagado            │
               │ FechaVencim.   │    │ PagoRealizadoId FK     │
               │ EstadoCuota    │    └────────────────────────┘
               │ MontoCuota     │
               │ MedioPago      │    ┌────────────────────────┐
               │ MesCuota       │    │     PagoRealizado       │
               │ AnioCuota      │    │────────────────────────│
               │ PagoRealizadoId│    │ PagoRealizadoID PK     │
               └────────────────┘    │ FechaPago              │
                                     │ MontoTotal             │
               ┌────────────────┐    │ TipoConcepto           │
               │   Actividad    │    │ MedioPago              │
               │────────────────│    │ ReferenciaId           │
               │ ActividadId PK │    └────────────────────────┘
               │ NombreActividad│
               │ MontoActividad │
               │ URLImagen      │
               └────────────────┘
```

### 💡 Decisiones técnicas

|           Decisión          |                        Motivo                                            |
|-----------------------------|--------------------------------------------------------------------------|
| **Herencia Table Per Type** | `Persona` → `Socio` / `NoSocio` en tablas separadas, igual que en DSOO   |
| **Boolean → INTEGER**       | SQLite no tiene tipo booleano. `AptoFisico`: `1` = true, `0` = false     |
| **Baja lógica**             | `EstadoSocio = "Inactivo"` en vez de DELETE. Preserva historial de pagos |
| **Transacciones**           | Insertar un socio requiere 2 tablas. Si una falla, se revierte la otra |
| **`onResume()`**            | Recarga la lista al volver de cualquier pantalla automáticamente |

---

## 📱 Módulos y pantallas

### 🔐 Autenticación
| Pantalla | Activity | Descripción |
|---|---|---|
| Splash | `MainActivity` | Pantalla de entrada con logo |
| Login | `LoginActivity` | Email + contraseña con hash SHA-256 |
| Registro | `RegisterActivity` | Crear cuenta de empleado |

### 👤 Socios
| Pantalla | Activity | Descripción |
|---|---|---|
| Listado | `SocioActivity` | Lista con SearchView — busca en tiempo real |
| Registrar | `NuevoSocioActivity` | Formulario completo con calendario y apto físico |
| Detalle / Editar | `DetalleSocioActivity` | Precarga datos — guarda o da de baja |

### 💰 Pagos
| Pantalla | Activity | Descripción |
|---|---|---|
| Lista de pagos | `PagosActivity` | Historial ordenado por fecha |
| Nuevo pago | `PagosActivity` | Búsqueda por ID o nombre con lupa 🔍 |

### 🏃 Actividades
| Pantalla | Activity | Descripción |
|---|---|---|
| Listado | `ActividadesActivity` | Lista de actividades con monto |
| Nueva actividad | `NuevaActividadActivity` | Alta con imagen, nombre y monto |

### 📋 Listados
| Pantalla | Activity | Descripción |
|---|---|---|
| Morosos | `ListadoActivity` | Socios con cuota vencida en el día |

---

## 🧭 Flujo de navegación

```
MainActivity  (splash)
      │
      └──► LoginActivity
                │
                ├──► RegisterActivity
                │
                └──► HomeActivity  ──────────────────────────────┐
                          │                                       │
                          ├──► SocioActivity                     │
                          │         ├──► NuevoSocioActivity       │
                          │         │       └── RESULT_OK ───────►│ recarga lista
                          │         └──► DetalleSocioActivity     │
                          │                 └── RESULT_OK ───────►│ recarga lista
                          │                                       │
                          ├──► PagosActivity                      │
                          │    (lista ↔ formulario en la misma Activity)
                          │                                       │
                          ├──► ActividadesActivity               │
                          │         └──► NuevaActividadActivity   │
                          │                                       │
                          └──► ListadoActivity ◄─────────────────┘
```

### 🔍 Búsqueda en Pagos — 4 intentos en orden

```
Usuario escribe en el campo y toca 🔍
              │
              ▼
     ¿Es número?
      ├── SÍ → busca por ID en Socio
      │         └── ¿encontró? → completar formulario ✅
      │         busca por ID en NoSocio
      │         └── ¿encontró? → completar formulario ✅
      │
      └── NO → busca por nombre/apellido LIKE en Socio
                └── ¿encontró? → completar formulario ✅
                busca por nombre/apellido LIKE en NoSocio
                └── ¿encontró? → completar formulario ✅
                
              ¿ninguno encontró?
              └── Toast "No se encontró ningún cliente" ❌
```

---

## 🔄 Mejoras incorporadas

> Respecto al sistema original en C# con Windows Forms:

### ✅ Mejora 1 — Login con registro de usuario
Elimina los 4 pasos de configuración manual (servidor, puerto, usuario MySQL, clave MySQL). El login tiene solo **email y contraseña**. La conexión a la BD es transparente para el usuario. Las contraseñas se hashean con **SHA-256**.

### ✅ Mejora 2 — Gestión de actividades
El sistema original cargaba actividades por fuera del sistema. Ahora existe un módulo completo de **alta y modificación** de actividades con nombre, monto e imagen, directamente desde la app.

### ✅ Mejora 3 — Comprobante compartible
Se reemplaza la generación de PDF para impresión por una **pantalla de confirmación** con opción de compartir el comprobante de pago o el carnet digital por WhatsApp, email u otras apps del dispositivo.

### ✅ Mejora 4 — Pago de cuota en cuotas
El módulo de pagos incorpora planes de **3 o 6 cuotas** con tarjeta. El sistema calcula los vencimientos de cada cuota, registra el estado (Pagado / Pendiente) y genera el comprobante con el detalle del plan completo.

---

## 📖 Historias de usuario

### Módulo de autenticación

| ID | Escenario | Resultado esperado |
|---|---|---|
| **HU-1** Iniciar sesión | Credenciales correctas | Accede a la pantalla principal |
| **HU-1** Iniciar sesión | Credenciales incorrectas | Mensaje de error, no permite acceso |
| **HU-2** Registrar usuario | Datos completos, usuario nuevo | Crea usuario y habilita acceso |
| **HU-2** Registrar usuario | Nombre de usuario ya existente | Mensaje de error indicando duplicado |

### Módulo de socios

| ID | Escenario | Resultado esperado |
|---|---|---|
| **HU-3** Registrar No Socio | Datos completos, DNI nuevo, apto físico entregado | Registra el No Socio, muestra éxito |
| **HU-3** Registrar No Socio | DNI ya registrado | Mensaje de error de duplicado |
| **HU-3** Registrar No Socio | Sin marcar apto físico | Mensaje indicando que es obligatorio |
| **HU-4** Registrar Socio | Datos completos, DNI nuevo, apto físico | Registra el Socio, genera número de socio |
| **HU-4** Registrar Socio | DNI ya registrado | Mensaje de error de duplicado |
| **HU-4** Registrar Socio | Sin marcar apto físico | Mensaje indicando que es obligatorio |

### Módulo de pagos

| ID | Escenario | Resultado esperado |
|---|---|---|
| **HU-5** Pagar cuota | Socio existente, pago único | Registra pago, muestra comprobante con opción de compartir |
| **HU-5** Pagar cuota | Socio existente, plan 3 o 6 cuotas | Registra plan, calcula vencimientos, muestra comprobante con detalle |
| **HU-5** Pagar actividad | No Socio existente, pago diario | Registra pago diario, muestra comprobante con opción de compartir |
| **HU-5** Pagar | ID no existente | Mensaje de error, no corresponde a ningún registro |

### Módulo de carnet y listados

| ID | Escenario | Resultado esperado |
|---|---|---|
| **HU-6** Generar carnet | Socio sin carnet previo | Genera carnet con número asignado, opción de compartir |
| **HU-6** Generar carnet | Socio con carnet ya emitido | Informa que ya existe, ofrece reenviar |
| **HU-7** Emitir listado | Socios con cuota vencida en el día | Muestra el listado de morosos |
| **HU-7** Emitir listado | Sin socios con cuota vencida | Mensaje indicando que no hay deudores |

### Módulo de actividades

| ID | Escenario | Resultado esperado |
|---|---|---|
| **HU-8** Alta de actividad | Datos completos, actividad nueva | Registra la actividad y la muestra en el listado |
| **HU-8** Modificar actividad | Selecciona actividad existente, edita datos | Actualiza y muestra con los cambios aplicados |

---

## 🔗 Recursos del proyecto

| Recurso | Enlace |
|---|---|
| 🎨 **Prototipo Figma** | [Ver prototipo interactivo](https://www.figma.com/design/BeGSOyV6FsUTe5nxBxv2Z9/Club-Deportivo---Prototipo?node-id=10121-17676&t=mtiep1qsuXjOOvJK-1) |
| 📄 **Informe Etapas 1 y 2** | IFTS N°29 — DAM Comisión C |

---

<p align="center">
  <img src="https://img.shields.io/badge/Kotlin-7F52FF?style=flat-square&logo=kotlin&logoColor=white" />
  <img src="https://img.shields.io/badge/SQLite-003B57?style=flat-square&logo=sqlite&logoColor=white" />
  <img src="https://img.shields.io/badge/Android%20Studio-3DDC84?style=flat-square&logo=androidstudio&logoColor=white" />
  <img src="https://img.shields.io/badge/Material%20Design-757575?style=flat-square&logo=materialdesign&logoColor=white" />
  <img src="https://img.shields.io/badge/Figma-F24E1E?style=flat-square&logo=figma&logoColor=white" />
</p>

<p align="center">
  <sub>IFTS N°29 · DAM Comisión C · Grupo 4 · 2026</sub>
</p>
