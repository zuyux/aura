# Solicitud de Propuesta (RFP)

# Plataforma de Alertas y Reportes de Seguridad Comunitaria

## 1. Resumen General

La Comunidad busca propuestas para el diseño, desarrollo, despliegue y mantenimiento de una plataforma de seguridad comunitaria orientada prioritariamente a dispositivos móviles (mobile-first). Esta plataforma permitirá a los residentes reportar incidentes, recibir alertas cercanas, coordinarse con contactos de confianza y mejorar los esfuerzos locales de prevención, protegiendo al mismo tiempo la privacidad de los usuarios.

La solución propuesta debe ser compatible con dispositivos Android, funcionar en condiciones de baja conectividad y proporcionar un flujo de trabajo seguro y consciente de la privacidad para el reporte de incidentes, la verificación de alertas y la coordinación de emergencias.

## 2. Antecedentes

Actualmente, la Comunidad depende de canales informales como grupos de WhatsApp, llamadas telefónicas, publicaciones en redes sociales y coordinación manual para comunicar los incidentes de seguridad. Estos canales están fragmentados, son difíciles de verificar y, a menudo, exponen información personal sensible.

La Comunidad requiere un sistema estructurado, confiable y que preserve la privacidad para mejorar la concienciación local, reducir la desinformación y apoyar una coordinación más segura entre los residentes.

## 3. Objetivos del Proyecto

El proveedor seleccionado deberá entregar una solución que permita a la Comunidad:

* Reportar incidentes de seguridad rápidamente desde un dispositivo móvil.
* Recibir alertas basadas en la ubicación sobre incidentes cercanos.
* Verificar o marcar reportes como falsos, resueltos o aún activos.
* Apoyar la coordinación de emergencias a través de contactos de confianza.
* Preservar la privacidad del usuario por defecto.
* Funcionar parcialmente sin conexión o bajo conectividad débil.
* Generar registros de incidentes estructurados para su posterior revisión o exportación.
* Proporcionar una capa administrativa para la moderación y la gestión comunitaria.

## 4. Alcance del Trabajo

La propuesta debe incluir el desarrollo y la entrega de los siguientes módulos principales:

### 4.1 Aplicación Móvil Android

La aplicación móvil se desarrollará en Kotlin y debe incluir:

* Pantalla de inicio con acceso a emergencias.
* Flujo de reporte de incidentes.
* Mapa y lista de alertas cercanas.
* Detalle de la alerta y acciones de verificación.
* Módulo Red Guardián / contactos de confianza.
* Borradores de reportes fuera de línea.
* Perfil de usuario y configuración de privacidad.

### 4.2 Reporte de Incidentes

La plataforma debe permitir a los usuarios enviar reportes que incluyan:

* Tipo de incidente.
* Nivel de gravedad.
* Ubicación aproximada.
* Fecha y hora.
* Descripción opcional.
* Evidencia opcional (como foto, video o audio).
* Opción de reporte anónimo.
* Configuración de visibilidad.

Las categorías iniciales de incidentes pueden incluir:

* Robo
* Intento de robo
* Actividad sospechosa
* Violencia
* Acoso
* Accidente
* Zona peligrosa
* Otro

### 4.3 Alertas y Verificación Comunitaria

La plataforma debe proporcionar:

* Flujo de alertas cercanas.
* Visualización basada en mapas.
* Etiquetas de estado de las alertas.
* Confirmación comunitaria.
* Marcado de reportes falsos.
* Estado de resuelto.
* Expiración de alertas desactualizadas.
* Filtrado por categoría y gravedad.

Los reportes se marcarán como no verificados hasta que sean validados por la comunidad, los moderadores o el personal autorizado.

### 4.4 Red Guardián / Coordinación de Emergencias

La solución debe incluir un módulo privado de coordinación de emergencias donde los usuarios puedan:

* Añadir contactos de confianza.
* Iniciar una sesión de seguridad.
* Compartir la ubicación con contactos de confianza.
* Marcarse a sí mismos como seguros.
* Llamar a un contacto o número de emergencia.
* Utilizar el sistema de respaldo por SMS cuando sea compatible.
* Finalizar la sesión manualmente.

Este módulo debe ser privado por defecto.

### 4.5 Soporte Fuera de Línea y de Baja Conectividad

La solución debe soportar:

* Creación local de borradores de incidentes.
* Almacenamiento local de evidencias.
* Sincronización en cola.
* Alertas en caché.
* Contactos de emergencia fuera de línea.
* Respaldo por SMS para mensajes de emergencia.
* Lógica de reintento cuando vuelva la conectividad.

### 4.6 Privacidad y Seguridad

La plataforma debe seguir los principios de privacidad desde el diseño, incluyendo:

* Modo anónimo por defecto siempre que sea posible.
* Ubicación aproximada para reportes públicos.
* Evidencia privada por defecto.
* No exponer públicamente la identidad del usuario.
* No exponer públicamente direcciones exactas.
* Eliminación de metadatos sensibles de los archivos multimedia.
* Almacenamiento local seguro.
* Comunicación cifrada con los servicios de backend.
* Prohibición de venta o intercambio comercial de datos de ubicación personal.

### 4.7 Panel Administrativo

Opcional para el MVP, pero las propuestas pueden incluir un panel de administración para:

* Revisar reportes.
* Moderar contenido sensible.
* Marcar reportes como verificados, falsos, resueltos o desestimados.
* Ver tendencias agregadas de incidentes.
* Exportar reportes estructurados.
* Gestionar zonas comunitarias.
* Gestionar moderadores de confianza.

## 5. Requisitos Técnicos

El stack técnico preferido es:

* Android Kotlin
* Jetpack Compose
* Room Database
* WorkManager
* DataStore
* Hilt
* Kotlin Coroutines / Flow
* MapLibre o Google Maps
* Supabase, Firebase o backend personalizado
* PostgreSQL + PostGIS para consultas geoespaciales
* Almacenamiento de objetos seguro para evidencias multimedia

El proveedor puede proponer alternativas, pero debe justificar las decisiones técnicas.

## 6. Requisitos del Modelo de Datos

El sistema debe admitir, como mínimo, las siguientes entidades:

* UserProfile (Perfil de Usuario)
* DeviceIdentity (Identidad del Dispositivo)
* IncidentReport (Reporte de Incidente)
* IncidentEvidence (Evidencia de Incidente)
* Alert (Alerta)
* ReportVerification (Verificación de Reporte)
* GuardianContact (Contacto Guardián)
* SafetySession (Sesión de Seguridad)
* SafetySessionUpdate (Actualización de Sesión de Seguridad)
* SyncQueueItem (Elemento de la Cola de Sincronización)

## 7. Entregables

El proveedor seleccionado deberá entregar:

* Aplicación Android MVP.
* Servicio de Backend/API.
* Implementación de la base de datos local.
* Implementación de la cola de sincronización.
* Flujo de trabajo básico para el reporte de incidentes.
* Flujo de trabajo para alertas cercanas.
* Flujo de trabajo para la Red Guardián.
* Documentación técnica.
* Documentación de despliegue.
* Repositorio de código fuente.
* Documentación de pruebas.
* Notas de privacidad y seguridad.
* Guía de incorporación (onboarding) para el usuario.

## 8. Criterios de Aceptación del MVP

El MVP será aceptado si:

* Los usuarios pueden crear reportes de incidentes.
* Los reportes se guardan localmente cuando se está fuera de línea.
* Los reportes se pueden sincronizar cuando vuelve la conectividad.
* Los usuarios pueden ver alertas cercanas.
* Los usuarios pueden abrir los detalles de una alerta.
* Los usuarios pueden verificar o marcar reportes como falsos/resueltos.
* Los usuarios pueden crear contactos de confianza.
* Los usuarios pueden iniciar y finalizar una sesión de la Red Guardián.
* Se aplican las reglas de privacidad de ubicación.
* La evidencia es privada por defecto.
* La aplicación se ejecuta en los dispositivos Android compatibles.
* El código fuente está documentado y se puede compilar.

## 9. Cronograma

Los proveedores deben proponer un cronograma para el MVP.

Fases sugeridas:

### Fase 1: Descubrimiento y Definición del Producto

Duración: 1–2 semanas

* Validación de requisitos.
* Flujos de usuario.
* Arquitectura técnica.
* Modelo de datos.
* Wireframes de la interfaz de usuario (UI).

### Fase 2: Desarrollo del MVP

Duración: 6–10 semanas

* Desarrollo de la aplicación Android.
* Configuración del backend.
* Base de datos local.
* Cola de sincronización.
* Módulo central de reportes.
* Alertas.
* Red Guardián.

### Fase 3: Pruebas y Piloto

Duración: 2–4 semanas

* Control de calidad (QA) interno.
* Piloto comunitario.
* Corrección de errores (bugs).
* Revisión de privacidad.
* Preparación para el despliegue.

### Fase 4: Lanzamiento y Soporte

Duración: continuo

* Monitoreo.
* Soporte.
* Mantenimiento.
* Iteración.

## 10. Requisitos de la Propuesta

Cada propuesta debe incluir:

* Perfil de la empresa/equipo.
* Experiencia relevante.
* Arquitectura técnica propuesta.
* Metodología de desarrollo.
* Cronograma.
* Presupuesto.
* Plan de mantenimiento.
* Enfoque de privacidad y seguridad.
* Política de código abierto (open-source).
* Riesgos y suposiciones.
* Términos de soporte post-lanzamiento.

## 11. Criterios de Evaluación

Las propuestas se evaluarán con base en:

| Criterio | Peso |
| --- | --- |
| Capacidad técnica | 25% |
| Enfoque de privacidad y seguridad | 20% |
| Comprensión del producto | 20% |
| Costo y cronograma | 15% |
| Experiencia relevante | 10% |
| Mantenibilidad y documentación | 10% |

## 12. Presupuesto

Los proveedores deben presentar un presupuesto detallado que incluya:

* Diseño de producto.
* Desarrollo Android.
* Desarrollo de backend.
* Configuración de infraestructura.
* Pruebas.
* Despliegue.
* Documentación.
* Mantenimiento.
* Panel administrativo opcional.
* Generación opcional de reportes mediante Modelos de Lenguaje Grande (LLM).

La Comunidad podrá considerar propuestas de precio fijo o pagos basados en hitos.

## 13. Propiedad y Licenciamiento

La Comunidad prefiere que el código fuente se entregue bajo una licencia de código abierto, como MIT o Apache 2.0, a menos que se negocie lo contrario.

La Comunidad conservará el acceso a:

* Código fuente.
* Documentación.
* Credenciales de despliegue.
* Esquema de la base de datos.
* Activos de diseño.
* Instrucciones de compilación (build).

## 14. Requisitos Éticos

La solución no debe:

* Fomentar el vigilantismo (justicia por mano propia).
* Publicar acusaciones contra personas identificables.
* Exponer públicamente ubicaciones exactas de los usuarios.
* Vender datos de los usuarios.
* Utilizar reconocimiento facial.
* Mostrar evidencias sensibles sin moderación.
* Reemplazar los canales oficiales de emergencia.

La plataforma debe apoyar la prevención, la documentación y la coordinación, no el castigo público ni la vigilancia.

## 15. Características Futuras Opcionales

Los proveedores pueden describir opcionalmente el soporte futuro para:

* Panel administrativo.
* Borradores de denuncias generados por LLM.
* Exportación de reportes en formato PDF/JSON.
* Notificaciones push.
* Sistema de reputación comunitaria.
* Integraciones municipales.
* Mapas de calor de seguridad.
* Reportes anónimos de extorsión/negocios.
* Bóveda de evidencias cifrada.
* Despliegues multi-comunitarios.