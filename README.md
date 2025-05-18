# InfoTracker
Propuesta de valor: Sirve para relacionar noticias y vídeos de youtube sobre un tema concreto, permitiendo ampliar la información del usuario sobre ese tema
Justificación de las APIS: Me resulta útil combinar estas dos APIS ya que muchas veces se publican vídeos en youtube interesantes sobre algunas noticias, lo cual nos permite ampliar la información, ya que muchas veces en la tele no sale tanta información al haber bastantes noticias en un programa.
Estructura del datamart: Utiliza arrays para guardar eventos de tipo NewsEvent y VideoEvent, sirve para añadir eventos, y devolver los eventos de noticias y vídeos
Sólo se ejecuta el módulo business-unit que incluye una interfaz que engloba los demás módulos, al ejecutarse se abrirá una interfaz en la que se podrá buscar noticias y vídeos sobre algún tema y ver el historial de lo buscado
En eventstore se almacena el historial de lo buscado, tiene varias carpetas con el nombre de cada tema buscado y dentro de ella las carpetas news y events y cada una dentro un archivo .events con el nombre de la fecha
