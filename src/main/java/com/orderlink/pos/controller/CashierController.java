package com.orderlink.pos.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import com.orderlink.pos.model.Producto;
import com.orderlink.pos.db.DatabaseManager;
import java.util.*;

/**
 * Controlador para la vista de Cajero.
 * Permite gestionar el punto de venta, carrito, deshacer/rehacer y cobro.
 * Cada método está documentado para facilitar el estudio y explicación.
 */
public class CashierController {
    // Componentes principales de la vista de cajero
    @FXML private ListView<Producto> productosList;
    @FXML private TableView<CarritoItem> carritoTable;
    @FXML private Button undoBtn;
    @FXML private Button redoBtn;
    @FXML private Label totalLabel;
    @FXML private Button pagarBtn;

    // Pilas para deshacer y rehacer acciones en el carrito
    private final Stack<List<CarritoItem>> undoStack = new Stack<>();
    private final Stack<List<CarritoItem>> redoStack = new Stack<>();

    /**
     * Clase interna que representa un ítem en el carrito de compras.
     */
    public static class CarritoItem {
        private final Producto producto;
        private int cantidad;
        public CarritoItem(Producto producto, int cantidad) {
            this.producto = producto;
            this.cantidad = cantidad;
        }
        public String getNombre() { return producto.getNombre(); }
        public double getPrecio() { return producto.getPrecio(); }
        public int getCantidad() { return cantidad; }
        public double getSubtotal() { return cantidad * producto.getPrecio(); }
        public void setCantidad(int cantidad) { this.cantidad = cantidad; }
        public Producto getProducto() { return producto; }
    }

    /**
     * Inicializa la vista de cajero.
     * Configura la tabla, carga productos y eventos de botones.
     */
    @FXML
    private void initialize() {
        setupCarritoTable(); // Configura columnas de la tabla del carrito
        cargarProductos();   // Carga productos desde la base de datos
        // Doble clic para agregar producto al carrito
        productosList.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                Producto p = productosList.getSelectionModel().getSelectedItem();
                if (p != null) agregarAlCarrito(p);
            }
        });
        undoBtn.setOnAction(e -> undo());
        redoBtn.setOnAction(e -> redo());
        pagarBtn.setOnAction(e -> pagar());
        actualizarTotal();
    }

    /**
     * Configura las columnas de la tabla del carrito y los eventos de edición.
     */
    private void setupCarritoTable() {
        carritoTable.getColumns().clear();
        TableColumn<CarritoItem, String> nombreCol = new TableColumn<>("Producto");
        nombreCol.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        TableColumn<CarritoItem, Integer> cantidadCol = new TableColumn<>("Cantidad");
        cantidadCol.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        TableColumn<CarritoItem, Double> precioCol = new TableColumn<>("Precio");
        precioCol.setCellValueFactory(new PropertyValueFactory<>("precio"));
        TableColumn<CarritoItem, Double> subtotalCol = new TableColumn<>("Subtotal");
        subtotalCol.setCellValueFactory(new PropertyValueFactory<>("subtotal"));
        carritoTable.getColumns().addAll(nombreCol, cantidadCol, precioCol, subtotalCol);
        carritoTable.setRowFactory(tv -> {
            TableRow<CarritoItem> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    editarCantidad(row.getItem());
                }
            });
            return row;
        });
        carritoTable.setContextMenu(crearMenuContextualCarrito());
    }

    /**
     * Carga los productos desde la base de datos y los muestra en la lista.
     */
    private void cargarProductos() {
        try {
            List<Producto> productos = DatabaseManager.obtenerProductos();
            productosList.getItems().setAll(productos);
        } catch (Exception e) {
            mostrarAlerta("Error", "No se pudo cargar productos.", Alert.AlertType.ERROR);
        }
    }

    /**
     * Agrega un producto al carrito o incrementa su cantidad si ya existe.
     */
    private void agregarAlCarrito(Producto producto) {
        List<CarritoItem> carrito = new ArrayList<>(carritoTable.getItems());
        Optional<CarritoItem> existente = carrito.stream().filter(ci -> ci.getProducto().getId() == producto.getId()).findFirst();
        if (existente.isPresent()) {
            existente.get().setCantidad(existente.get().getCantidad() + 1);
        } else {
            carrito.add(new CarritoItem(producto, 1));
        }
        pushUndo();
        carritoTable.getItems().setAll(carrito);
        actualizarTotal();
    }

    /**
     * Permite editar la cantidad de un producto en el carrito.
     */
    private void editarCantidad(CarritoItem item) {
        TextInputDialog dialog = new TextInputDialog(String.valueOf(item.getCantidad()));
        dialog.setTitle("Editar cantidad");
        dialog.setHeaderText(null);
        dialog.setContentText("Nueva cantidad:");
        Optional<String> res = dialog.showAndWait();
        res.ifPresent(val -> {
            try {
                int nueva = Integer.parseInt(val);
                if (nueva > 0) {
                    item.setCantidad(nueva);
                    pushUndo();
                    carritoTable.refresh();
                    actualizarTotal();
                }
            } catch (Exception ignored) {}
        });
    }

    /**
     * Crea el menú contextual para eliminar productos del carrito.
     */
    private ContextMenu crearMenuContextualCarrito() {
        ContextMenu menu = new ContextMenu();
        MenuItem eliminar = new MenuItem("Eliminar");
        eliminar.setOnAction(e -> {
            CarritoItem item = carritoTable.getSelectionModel().getSelectedItem();
            if (item != null) {
                carritoTable.getItems().remove(item);
                pushUndo();
                actualizarTotal();
            }
        });
        menu.getItems().addAll(eliminar);
        return menu;
    }

    /**
     * Guarda el estado actual del carrito para deshacer.
     */
    private void pushUndo() {
        undoStack.push(clonarCarrito());
        redoStack.clear();
    }

    /**
     * Deshace la última acción en el carrito.
     */
    private void undo() {
        if (!undoStack.isEmpty()) {
            redoStack.push(clonarCarrito());
            List<CarritoItem> prev = undoStack.pop();
            carritoTable.getItems().setAll(prev);
            actualizarTotal();
        }
    }

    /**
     * Rehace la última acción deshecha en el carrito.
     */
    private void redo() {
        if (!redoStack.isEmpty()) {
            undoStack.push(clonarCarrito());
            List<CarritoItem> next = redoStack.pop();
            carritoTable.getItems().setAll(next);
            actualizarTotal();
        }
    }

    /**
     * Clona el carrito actual para las pilas de deshacer/rehacer.
     */
    private List<CarritoItem> clonarCarrito() {
        List<CarritoItem> copia = new ArrayList<>();
        for (CarritoItem ci : carritoTable.getItems()) {
            copia.add(new CarritoItem(ci.getProducto(), ci.getCantidad()));
        }
        return copia;
    }

    /**
     * Actualiza el total mostrado en la vista.
     */
    private void actualizarTotal() {
        double total = carritoTable.getItems().stream().mapToDouble(CarritoItem::getSubtotal).sum();
        totalLabel.setText("Total: $" + String.format("%.2f", total));
    }

    /**
     * Realiza el proceso de cobro y limpia el carrito.
     */
    private void pagar() {
        double total = carritoTable.getItems().stream().mapToDouble(CarritoItem::getSubtotal).sum();
        if (total == 0) {
            mostrarAlerta("Carrito vacío", "Agrega productos antes de pagar.", Alert.AlertType.WARNING);
            return;
        }
        ChoiceDialog<String> dialog = new ChoiceDialog<>("Efectivo", "Efectivo", "Tarjeta");
        dialog.setTitle("Método de pago");
        dialog.setHeaderText(null);
        dialog.setContentText("Selecciona método de pago:");
        Optional<String> res = dialog.showAndWait();
        if (res.isPresent()) {
            mostrarAlerta("Venta realizada", "Total: $" + String.format("%.2f", total) + "\nPago: " + res.get(), Alert.AlertType.INFORMATION);
            carritoTable.getItems().clear();
            undoStack.clear();
            redoStack.clear();
            actualizarTotal();
        }
    }

    /**
     * Muestra una alerta informativa, de error o confirmación.
     */
    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
