# Ubicación privada y desplazamientos

## Datos y compatibilidad
Se añaden cuatro columnas anulables a client_profiles: reference_label,
reference_latitude, reference_longitude y reference_approximate.
La ciudad usada para filtrar eventos y las preferencias anteriores se conservan.
No se crea una ubicación del usuario a partir de semillas ni se publican sus coordenadas
en el catálogo o en el endpoint público del mapa.

En desarrollo, al reiniciar con el perfil dev, Hibernate ddl-auto=update
añade estas columnas. En despliegues con validate o cambios de esquema controlados,
aplicar previamente docs/client-reference-location.sql con el procedimiento del equipo.
Este archivo no se ejecuta automáticamente.

## API (rol USER, identidad obtenida de la autenticación)
- GET/PUT/DELETE /api/v1/client-profiles/me/reference-location.
- PUT recibe label (1–300 caracteres), latitude (-90 a 90), longitude
  (-180 a 180) y approximate (booleano obligatorio).
- POST /api/v1/client-profiles/me/event-travel recibe occurrenceIds
  (1 a 50 identificadores positivos). Solo se consultan sesiones públicas,
  futuras, presenciales o híbridas, con plazas y coordenadas válidas.
- Las respuestas privadas no deben almacenarse en cachés compartidas.
- occurrenceId se añade a las tarjetas del catálogo, recomendaciones y home
  para usar la sede de la misma sesión que se está mostrando.

## Distancias y tiempos
Sin configurar rutas, se calcula localmente la distancia en línea recta con
Haversine. El método es STRAIGHT_LINE y durationMinutes queda en null.
No se estima el tiempo a partir de una velocidad arbitraria.

Para tiempos en coche, el operador debe configurar OONA_TRAVEL_ROUTING_URL
con la URL base de un servidor OSRM propio o de un proveedor autorizado y
compatible con Table API (annotations=distance,duration).
La configuración general está vacía. En el perfil dev, el desarrollador autorizó explícitamente
router.project-osrm.org para pruebas locales el 28-10-2026. Ese perfil envía las coordenadas
al servidor de demostración. No activar dev en producción. Para producción se debe configurar
un proveedor aprobado por el equipo.

Ejemplo de servidor propio: OONA_TRAVEL_ROUTING_URL=http://servidor-interno:5000.
Reiniciar el backend tras configurar la variable.
Confirmar previamente el tratamiento de coordenadas del proveedor seleccionado.
No utilizar automáticamente un servidor público de demostración en producción.
La API debe devolver distancias, duraciones y puntos ajustados de origen/destino.

Los trayectos se consultan en lotes, con caché en memoria de cinco minutos,
límite de 1000 entradas, máximo una petición nueva por segundo y tiempos de espera
acotados. Una ruta fallida, nula, demasiado alejada del punto indicado o una
consulta limitada por frecuencia conserva solo la distancia en línea recta.
El frontend conserva la distancia y reintenta hasta dos veces (a los 2 y 5 segundos
tras cada resultado temporal). No reintenta automáticamente sesiones inválidas ni errores
de autenticación. En el detalle, “Reintentar trayecto” permite una nueva consulta manual
con pausa mínima de un segundo. Al cambiar de cuenta, eliminar el origen o retirar
la tarjeta se cancelan los intentos pendientes que ya no correspondan.
La duración indica estimación en coche sin tráfico en tiempo real.
Las sedes iniciales aproximadas y los orígenes marcados como aproximados se
identifican como orientativos. Confirmar la sede con el organizador.

## Revisión manual
1. Entrar como cliente en Preferencias y escribir la referencia.
2. Elegir el punto en el mapa existente (clic, centro del mapa o Mi ubicación).
   No se geocodifica automáticamente un texto ni se inventa su posición.
3. Guardar con “Guardar ubicación”; el resto del formulario conserva su guardado.
4. Abrir un evento físico y comprobar la sede a la izquierda y el desplazamiento
   a la derecha. En móvil se apilan. Las tarjetas de Explorar, Home y relacionados muestran
   una fila compacta con kilómetros y minutos/horas, sin dirección larga ni avisos repetidos.
5. Cambiar la referencia, confirmar otro punto y guardar. Revisar la nueva distancia.
6. Eliminar la ubicación y verificar el aviso con enlace a Preferencias en Inicio,
   Explorar y el detalle. Guardarla debe retirar el aviso.
7. Un evento online, una sesión privada/pasada/llena o sin coordenadas no debe
   mostrar métricas. Cambiar de cuenta no debe conservar información de la anterior.
8. Con servidor propio configurado, comprobar minutos/horas y caída del proveedor:
   la navegación debe continuar mostrando solo la distancia en línea recta.
9. “Cómo llegar”/“Ver ruta orientativa” abre Google Maps solo al pulsarlo;
   ese enlace comparte los puntos indicados con Google Maps.

Las pruebas automatizadas usan H2 y un servidor HTTP local simulado para las rutas;
no envían ubicaciones personales a servicios externos.


## Inicio desde IntelliJ para pruebas locales
Seleccionar el perfil Spring dev en la configuración de OonaBeApplication y reiniciar.
No hace falta una clave de API para el servidor de demostración de OSRM.
Si existe OONA_TRAVEL_ROUTING_URL en el entorno, su valor tiene prioridad.
El servidor de demostración se limita a pruebas locales razonables y no comerciales;
respeta una petición por segundo y no garantiza disponibilidad ni tráfico en tiempo real.
La aplicación mantiene consultas en lotes, caché y recuperación ante fallos.
Fuente: https://github.com/Project-OSRM/osrm-backend/wiki/Demo-server
