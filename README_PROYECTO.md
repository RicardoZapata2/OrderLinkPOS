# Documentación del Proyecto OrderLink POS

## Descripción General
OrderLink POS es un sistema de punto de venta para cafetería universitaria, desarrollado en JavaFX y SQLite. Permite la gestión de inventario, ventas, reportes y pedidos, con perfiles diferenciados para administrador y cajero.

## Estructura del Proyecto
- **src/main/java/com/orderlink/pos/controller/**: Controladores JavaFX (Login, Admin, Cajero)
- **src/main/java/com/orderlink/pos/model/**: Modelos de datos (Producto, Venta, Pedido)
- **src/main/java/com/orderlink/pos/db/**: Acceso y gestión de base de datos
- **src/main/resources/fxml/**: Vistas FXML (Login, AdminView, CashierView)
- **src/main/resources/css/**: Estilos visuales (theme.css)

## Guía de Uso
### Acceso
- Usuario administrador: `administrador` / `1234`
- Usuario cajero: `cajero` / `1234`

### Flujo de Usuario
- **Administrador:** CRUD de productos, reportes, métricas, cierre de sesión.
- **Cajero:** Punto de venta, carrito, deshacer/rehacer, cobro, cierre de sesión.

## Explicación de Código
- Todos los controladores y vistas incluyen comentarios detallados para facilitar el estudio y la explicación en presentaciones.
- El código está organizado en métodos claros y documentados.

## Estética y Usabilidad
- Interfaz moderna, responsiva y amigable.
- Login y vistas principales distribuidas y adaptables a ventana y pantalla completa.
- Alertas visuales y mensajes claros para el usuario.

## Mejoras y Extensiones
- Se recomienda agregar más reportes, métricas y gestión de pedidos en futuras versiones.
- El sistema es fácilmente extensible gracias a su estructura modular y comentada.

---

Para dudas técnicas, consulta los comentarios en el código fuente o contacta al desarrollador.
