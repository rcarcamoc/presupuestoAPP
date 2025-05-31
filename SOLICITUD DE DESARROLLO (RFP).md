
# SOLICITUD DE DESARROLLO (RFP)
# APLICACIÓN MÓVIL DE FINANZAS PERSONALES "PERSONALBUDGET"

**Fecha de emisión:** 31 de mayo de 2025  

---

## ÍNDICE

1. [Introducción y visión general](#1-introducción-y-visión-general)
2. [Objetivos del proyecto](#2-objetivos-del-proyecto)
3. [Alcance y entregables](#3-alcance-y-entregables)
4. [Requisitos funcionales](#4-requisitos-funcionales)
5. [Requisitos no funcionales](#5-requisitos-no-funcionales)
6. [Arquitectura técnica recomendada](#6-arquitectura-técnica-recomendada)
7. [Roadmap y cronograma sugerido](#7-roadmap-y-cronograma-sugerido)
8. [Criterios de éxito y aceptación](#8-criterios-de-éxito-y-aceptación)
9. [Proceso de postulación](#9-proceso-de-postulación)
10. [Anexos](#10-anexos)

---

## 1. INTRODUCCIÓN Y VISIÓN GENERAL

### 1.1 Contexto

En la actualidad, muchas personas reciben notificaciones de sus gastos bancarios por correo electrónico, pero carecen de herramientas eficientes para consolidar, categorizar y analizar esta información financiera. Esto dificulta el seguimiento de presupuestos y la toma de decisiones financieras informadas.

### 1.2 Visión del producto

PersonalBudget busca ser una aplicación Android innovadora que automatice la captura de gastos desde notificaciones bancarias recibidas en Gmail, permitiendo a los usuarios gestionar sus finanzas personales con mínimo esfuerzo manual. La aplicación ofrecerá visualizaciones claras, presupuestos por categoría, respaldos seguros y opciones de compartición selectiva de información financiera.

### 1.3 Público objetivo

- Personas que reciben notificaciones de gastos bancarios por correo electrónico
- Usuarios interesados en gestionar sus finanzas personales de manera eficiente
- Parejas o familias que desean compartir y gestionar gastos conjuntos
- Individuos que valoran la automatización y simplicidad en herramientas financieras

---

## 2. OBJETIVOS DEL PROYECTO

### 2.1 Objetivo principal

Desarrollar una aplicación Android de finanzas personales que automatice la captura de gastos desde notificaciones bancarias en Gmail, ofrezca gestión de presupuestos, visualizaciones claras y opciones de compartición segura.

### 2.2 Objetivos específicos

1. **Automatización:** Minimizar la entrada manual de datos mediante la ingesta automática desde Gmail
2. **Visualización:** Proporcionar insights claros sobre patrones de gasto y estado de presupuestos
3. **Seguridad:** Garantizar la privacidad y protección de datos financieros sensibles
4. **Colaboración:** Permitir la compartición selectiva de información financiera entre usuarios
5. **Respaldo:** Asegurar que los datos estén respaldados y disponibles en múltiples dispositivos

### 2.3 Indicadores clave de rendimiento (KPIs)

- Reducción del tiempo de registro de gastos (comparado con métodos manuales)
- Precisión en la extracción de datos desde correos electrónicos (>95%)
- Satisfacción del usuario con la interfaz y funcionalidades (>4.5/5 en pruebas de usabilidad)
- Tiempo de respuesta de la aplicación (<1 segundo para operaciones comunes)

---

## 3. ALCANCE Y ENTREGABLES

### 3.1 Alcance del proyecto

#### Incluido en el alcance:
- Aplicación Android nativa desarrollada en Kotlin con Jetpack Compose
- Integración con Gmail API para ingesta automática de gastos
- Integración con Google Drive para respaldos y compartición
- Sistema de autenticación mediante Google Sign-In
- Funcionalidades de gestión de presupuestos y categorización
- Visualizaciones e informes financieros
- Documentación técnica y de usuario

#### Fuera del alcance:
- Versión para iOS o web (consideradas para fases futuras)
- Integración directa con APIs bancarias (considerada para fases futuras)
- Funcionalidades de inversión o gestión de activos
- Soporte para múltiples idiomas en la versión inicial

### 3.2 Entregables esperados

1. **Código fuente completo:**
   - Repositorio Git con historial de commits
   - Estructura organizada según Clean Architecture
   - Documentación inline del código

2. **Aplicación funcional:**
   - APK firmado para distribución
   - Bundle para Google Play Store

3. **Documentación:**
   - Documentación de arquitectura
   - Manual de usuario
   - Guía de instalación y configuración
   - Documentación de APIs y componentes

4. **Materiales adicionales:**
   - Diseños UI/UX (Figma o similar)
   - Plan de pruebas y resultados
   - Estrategia de lanzamiento recomendada

---

## 4. REQUISITOS FUNCIONALES

Los requisitos se clasifican como:
- **Mandatorio (M):** Debe ser implementado en la versión inicial
- **Deseable (D):** Altamente valorado pero podría implementarse en fases posteriores
- **Futuro (F):** Considerado para versiones futuras

### 4.1 Autenticación y permisos

- **(M)** Implementación de Google Sign-In para autenticación de usuarios
- **(M)** Solicitud y gestión de permisos para Gmail API (scope de lectura)
- **(M)** Solicitud y gestión de permisos para Drive API (scope de archivos)
- **(M)** Soporte para selección de cuenta de Gmail alternativa para ingesta (diferente a la cuenta principal)
- **(D)** Opción de autenticación biométrica para acceso a la aplicación
- **(F)** Soporte para múltiples perfiles de usuario en el mismo dispositivo

### 4.2 Ingesta de gastos desde Gmail

- **(M)** Capacidad para listar correos con etiqueta o query específica del banco
- **(M)** Parser HTML para extraer datos de gastos (monto, fecha, hora, comercio, cuotas, últimos dígitos)
- **(M)** Programación de tarea periódica (WorkManager) para consulta automática cada hora
- **(M)** Almacenamiento de gastos extraídos en base de datos local
- **(D)** Soporte para múltiples formatos de correos bancarios
- **(D)** Notificaciones sobre nuevos gastos detectados
- **(F)** Capacidad de aprendizaje para reconocer nuevos formatos de correos

### 4.3 Gestión manual de gastos

- **(M)** Pantalla para agregar gastos manualmente con todos los campos relevantes
- **(M)** Listado de gastos con opciones de filtrado y ordenamiento
- **(M)** Funcionalidad de edición y eliminación de gastos
- **(M)** Asignación de categorías a gastos
- **(D)** Capacidad para adjuntar imágenes de recibos
- **(D)** Reconocimiento de texto en imágenes de recibos (OCR)
- **(F)** Escaneo de códigos QR de facturas electrónicas

### 4.4 Gestión de presupuestos

- **(M)** Definición de presupuestos por categoría
- **(M)** Seguimiento de gastos vs presupuesto
- **(M)** Alertas visuales al superar presupuestos
- **(D)** Presupuestos recurrentes (mensuales, quincenales, etc.)
- **(D)** Ajuste automático de presupuestos basado en historial
- **(F)** Presupuestos para ocasiones especiales (vacaciones, eventos)

### 4.5 Reportes y visualizaciones

- **(M)** Gráficos de gastos por categoría (barras, torta)
- **(M)** Comparativa visual de gasto vs presupuesto
- **(M)** Listado filtrable de gastos recientes
- **(M)** Resumen consolidado de situación financiera
- **(D)** Insights automáticos sobre patrones de gasto
- **(D)** Tendencias de gasto a lo largo del tiempo
- **(F)** Predicciones de gastos futuros basadas en patrones

### 4.6 Respaldos y sincronización

- **(M)** Exportación de base de datos a formato JSON
- **(M)** Respaldo automático diario en Google Drive
- **(M)** Opciones manuales para "Respaldar ahora" y "Restaurar"
- **(D)** Historial de respaldos con posibilidad de restaurar versiones anteriores
- **(D)** Sincronización entre múltiples dispositivos del mismo usuario
- **(F)** Exportación a formatos adicionales (CSV, Excel)

### 4.7 Compartición de datos

- **(M)** Capacidad para invitar a otros usuarios por correo
- **(M)** Roles configurables (lectura, escritura)
- **(M)** Sincronización de datos mediante Google Drive
- **(D)** Panel de control para gestionar usuarios compartidos
- **(D)** Notificaciones sobre cambios realizados por otros usuarios
- **(F)** Comentarios y notas en gastos compartidos

---

## 5. REQUISITOS NO FUNCIONALES

### 5.1 Rendimiento

- **(M)** Tiempo de inicio de la aplicación < 3 segundos en dispositivos de gama media
- **(M)** Tiempo de respuesta para operaciones comunes < 1 segundo
- **(M)** Consumo de memoria RAM < 200MB en uso normal
- **(D)** Optimización para dispositivos de gama baja
- **(D)** Carga eficiente de listas largas mediante paginación

### 5.2 Seguridad

- **(M)** Almacenamiento seguro de tokens OAuth
- **(M)** Cifrado de datos sensibles en la base de datos local
- **(M)** Validación de entradas de usuario
- **(D)** Opción de bloqueo de aplicación con PIN o biometría
- **(D)** Detección y prevención de capturas de pantalla en información sensible

### 5.3 Usabilidad

- **(M)** Interfaz intuitiva siguiendo Material Design 3
- **(M)** Soporte para temas claro y oscuro
- **(M)** Feedback claro sobre acciones y estados
- **(D)** Tutoriales interactivos para nuevos usuarios
- **(D)** Accesibilidad para usuarios con discapacidades visuales

### 5.4 Compatibilidad

- **(M)** Compatibilidad con Android 7.0 (API 24) y superior
- **(M)** Adaptación a diferentes tamaños de pantalla (teléfonos y tablets)
- **(M)** Soporte para orientación vertical y horizontal
- **(D)** Optimización para dispositivos plegables
- **(F)** Soporte para Wear OS (versión simplificada)

### 5.5 Mantenibilidad

- **(M)** Arquitectura modular siguiendo Clean Architecture
- **(M)** Documentación de código y componentes
- **(M)** Tests unitarios para lógica de negocio crítica
- **(D)** Tests de integración para flujos principales
- **(D)** CI/CD para automatización de pruebas y despliegue

### 5.6 Escalabilidad

- **(M)** Diseño que permita añadir nuevos tipos de fuentes de datos
- **(M)** Arquitectura extensible para nuevas funcionalidades
- **(D)** Capacidad para manejar grandes volúmenes de datos históricos
- **(F)** Preparación para expansión multiplataforma

---

## 6. ARQUITECTURA TÉCNICA RECOMENDADA

Se recomienda seguir una arquitectura moderna y escalable, pero el equipo de desarrollo puede proponer alternativas justificadas.

### 6.1 Stack tecnológico recomendado

- **Lenguaje:** Kotlin
- **UI:** Jetpack Compose
- **Base de datos local:** Room (SQLite)
- **Sincronización y respaldo:** Google Drive API (v3)
- **Autenticación:** Google Sign-In
- **Inyección de dependencias:** Hilt o Koin
- **Programación asíncrona:** Coroutines y Flow
- **Navegación:** Jetpack Navigation
- **Gráficos:** MPAndroidChart o similar compatible con Compose

### 6.2 Arquitectura propuesta

Se recomienda seguir **Clean Architecture** con las siguientes capas:

- **Data layer:**
  - Room para persistencia local
  - GmailDataSource para interacción con Gmail API
  - DriveDataSource para interacción con Drive API
  - Implementaciones concretas de repositorios

- **Domain layer:**
  - Entidades de negocio
  - Interfaces de repositorios
  - Casos de uso (UseCases)

- **Presentation layer:**
  - ViewModels para gestión de estado
  - Compose Screens para UI
  - Navegación y componentes visuales

### 6.3 Patrones recomendados

- MVVM para la capa de presentación
- Repository Pattern para acceso a datos
- Use Case Pattern para lógica de negocio
- Dependency Injection para desacoplamiento
- Observer Pattern (Flow/StateFlow) para datos reactivos

---

## 7. ROADMAP Y CRONOGRAMA SUGERIDO

El siguiente roadmap es una sugerencia y puede ser ajustado en la propuesta del equipo de desarrollo.

### Fase 1: Fundamentos (4-6 semanas)

- Configuración del proyecto y arquitectura base
- Implementación de autenticación con Google Sign-In
- Desarrollo de modelos de datos y base de datos local
- Creación de UI básica con navegación principal
- Implementación de pantallas para ingreso manual de gastos

### Fase 2: Funcionalidad core (6-8 semanas)

- Integración con Gmail API para ingesta de correos
- Desarrollo de parser HTML para extracción de datos
- Implementación de WorkManager para sincronización periódica
- Desarrollo de gestión de presupuestos
- Creación de visualizaciones básicas

### Fase 3: Características avanzadas (6-8 semanas)

- Integración con Google Drive para respaldos
- Implementación de compartición de datos
- Desarrollo de reportes avanzados e insights
- Mejoras en la experiencia de usuario
- Optimización de rendimiento

### Fase 4: Refinamiento y lanzamiento (4-6 semanas)

- Testing exhaustivo y corrección de bugs
- Optimización de rendimiento y consumo de recursos
- Refinamiento de UI/UX basado en feedback
- Preparación para lanzamiento (firma, configuración de Play Store)
- Documentación final

### Cronograma estimado

- **Duración total estimada:** 20-28 semanas (5-7 meses)
- **Hitos principales:**
  - Prototipo funcional: Semana 8
  - MVP con ingesta de Gmail: Semana 14
  - Beta con todas las funcionalidades core: Semana 20
  - Versión final lista para lanzamiento: Semana 24-28

---

## 8. CRITERIOS DE ÉXITO Y ACEPTACIÓN

### 8.1 Criterios funcionales

- La aplicación debe extraer correctamente datos de gastos desde correos de Gmail con una precisión >95%
- Todas las funcionalidades marcadas como Mandatorias (M) deben estar implementadas y funcionales
- La sincronización con Google Drive debe ser confiable y mantener la integridad de los datos
- La compartición de datos entre usuarios debe respetar los roles y permisos asignados

### 8.2 Criterios no funcionales

- La aplicación debe cumplir con los requisitos de rendimiento especificados
- La interfaz de usuario debe seguir las guías de Material Design 3
- El código debe seguir las mejores prácticas de desarrollo Android
- La aplicación debe pasar las pruebas de seguridad básicas

### 8.3 Proceso de aceptación

1. **Revisión de código:**
   - Revisión de arquitectura y patrones implementados
   - Verificación de estándares de codificación
   - Evaluación de tests y cobertura

2. **Testing funcional:**
   - Pruebas de todas las funcionalidades según requisitos
   - Verificación de flujos de usuario completos
   - Validación de integración con APIs externas

3. **Testing no funcional:**
   - Pruebas de rendimiento
   - Evaluación de seguridad
   - Pruebas de usabilidad

4. **Entrega final:**
   - Entrega de todos los entregables especificados
   - Demostración de la aplicación funcionando
   - Transferencia de conocimiento y documentación

---

## 9. PROCESO DE POSTULACIÓN

### 9.1 Contenido esperado de la propuesta

Las propuestas deben incluir:

1. **Enfoque técnico:**
   - Arquitectura propuesta (confirmar o sugerir alternativas a la recomendada)
   - Stack tecnológico detallado
   - Estrategia para abordar desafíos técnicos clave

2. **Plan de proyecto:**
   - Cronograma detallado con hitos
   - Metodología de desarrollo
   - Equipo propuesto y roles

3. **Presupuesto:**
   - Desglose de costos por fase
   - Modelo de facturación propuesto
   - Opciones para características deseables vs. mandatorias

4. **Experiencia relevante:**
   - Proyectos similares desarrollados
   - Experiencia con las tecnologías propuestas
   - Referencias de clientes anteriores

### 9.2 Criterios de evaluación

Las propuestas serán evaluadas según:

- Comprensión de los requisitos y objetivos del proyecto
- Solidez del enfoque técnico propuesto
- Realismo y detalle del plan de proyecto
- Experiencia demostrable en proyectos similares
- Relación calidad-precio de la propuesta

### 9.3 Plazos y proceso

- **Fecha límite para preguntas:** [Definir por cliente]
- **Fecha límite para propuestas:** [Definir por cliente]
- **Proceso de selección:**
  1. Evaluación inicial de propuestas
  2. Entrevistas con equipos preseleccionados
  3. Selección final y negociación de contrato

---

## 10. ANEXOS

### 10.1 Ejemplos de correos bancarios

Se proporcionarán ejemplos de correos electrónicos de notificaciones bancarias para que los equipos puedan evaluar la complejidad del parsing HTML requerido.

### 10.2 Mockups conceptuales

Se incluirán mockups básicos de las principales pantallas para ilustrar la visión del producto:
- Pantalla de inicio/dashboard
- Listado de gastos
- Detalle de gasto
- Gestión de presupuestos
- Visualizaciones/reportes

### 10.3 Glosario de términos

- **Ingesta:** Proceso de extracción automática de datos desde correos electrónicos
- **Categoría:** Clasificación de gastos (ej. Alimentación, Transporte, Ocio)
- **Presupuesto:** Límite de gasto establecido para una categoría en un período
- **Insight:** Información relevante derivada del análisis de datos financieros

---

*Última actualización: 31 de mayo de 2025*
