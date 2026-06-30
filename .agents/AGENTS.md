# Reglas del Espacio de Trabajo: Oona Backend

Estas reglas combinan las instrucciones propias del proyecto con pautas generales para reducir errores comunes al trabajar con agentes de codigo. Las reglas especificas de este repositorio tienen prioridad sobre las generales.

## 1. Gestion de ramas

- Al iniciar cualquier tarea o desarrollo de codigo nuevo, no trabajes directamente en la rama actual.
- Pregunta siempre primero al usuario si desea crear una nueva rama en base a `develop` actualizada.
- No reviertas cambios existentes que no hayas hecho, salvo que el usuario lo pida explicitamente.

## 2. Pensar antes de codificar

- No asumas requisitos ambiguos sin revisar el contexto del proyecto.
- Explica las suposiciones relevantes antes de implementar cuando puedan afectar el resultado.
- Si hay varias interpretaciones razonables, plantealas en vez de elegir silenciosamente.
- Si falta informacion critica, pregunta antes de tocar codigo.
- Si existe una solucion mas simple y suficiente, priorizala.

## 3. Simplicidad primero

- Implementa el codigo minimo que resuelve el pedido.
- No agregues funcionalidades no solicitadas.
- No crees abstracciones para codigo de un solo uso.
- No agregues configurabilidad, extensibilidad o capas extra si no son necesarias.
- Si una solucion empieza a crecer demasiado, revisala y simplificala antes de continuar.

## 4. Cambios quirurgicos

- Toca solo los archivos necesarios para cumplir la tarea.
- No refactorices codigo adyacente si no esta directamente relacionado con el pedido.
- Respeta el estilo, estructura y patrones existentes del proyecto.
- Si detectas codigo muerto o problemas no relacionados, mencionarlos es preferible a modificarlos sin solicitud.
- Elimina imports, variables o funciones que tus propios cambios hayan dejado sin uso.

## 5. Ejecucion orientada a verificacion

- Convierte cada tarea en un objetivo verificable.
- Para bugs, intenta reproducir el fallo antes de corregirlo cuando sea practico.
- Para cambios de comportamiento, agrega o ajusta tests cuando el riesgo lo justifique.
- Ejecuta las pruebas o comandos de validacion relevantes antes de dar el trabajo por terminado.
- Si no puedes validar algo, dilo claramente junto con la razon.

## 6. Criterio de cierre

Un cambio esta listo solo cuando:

- Cumple el pedido original.
- Mantiene las reglas locales del proyecto.
- Tiene el alcance mas pequeno razonable.
- Fue verificado con tests, build o una comprobacion manual apropiada.
- Los riesgos o advertencias restantes quedaron comunicados.
