Aclaraciones sobre resolucion del problema
---
* Se utilizo el idioma ingles ya que me parecio mas facil nombrar variables y comentar

* Se realizo a modo de _API REST_ pensando en que la utilizacion de este sistema podia darse desde la _WEB_

* A modo de documentacion se encuentran en los comentarios al estilo _JAVADOCS_ arriba de cada metod

* Se realizaron test unitarios solo del service

* No se mockeo la cola de prioridad para poder probar mejor la funcionalidad

* Se creo un test con la finalidad de poder simular de manera mas real el compartamiento que podria llegar a tener la entrada de llamadas

* Se decidio como default (por configuracion) rechazar toda llamada posterior a tener todos los hilos ocupados con otras llamadas

* Se decidio no tener mas empleados que hilos para atender llamadas ya que no iba a cambiar el funcionamiento general del programa

Cosas a mejorar
---
Por cuestion de tiempos algunas cosas podrian haberse visto mejor

* Crear una UI simple donde se puede ver con intervalos de 1 minuto como va evolucionando el estado del callcenter

* Tener compartamientos mas configurable en cuanto a tiempos de espera para ser atendido (en caso de no ser rechazado de inmediato)

* Manejar mejor el error en caso de no tener mas lugar en el _Executor_

* Crear test sobre el resource para poder ver el manejo de errores

* Crear test e2e donde se pueda ver la intereaccion con la API (eleccion cucumber)
