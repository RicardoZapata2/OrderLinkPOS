package com.orderlink.pos.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.scene.Scene;
import javafx.stage.Stage;
import com.orderlink.pos.model.Producto;
import com.orderlink.pos.db.DatabaseManager;
import java.util.Optional;
import java.util.List;

/**
 * Controlador para la vista de Administrador.
 * Permite gestionar el inventario, reportes y cierre de sesión.
 * Cada método está documentado para facilitar el estudio y explicación.
 */
public class AdminController {
    // Etiquetas de métricas y botones principales
    @FXML private Label ventasLabel;
    @FXML private Label transaccionesLabel;
    @FXML private Label productoLabel;
    @FXML private Button agregarBtn;
    @FXML private TableView<Producto> inventarioTable;
    @FXML private Button reporteVentasBtn;
    @FXML private Button reporteInventarioBtn;
    @FXML private Button cierreBtn;

    /**
     * Inicializa la vista de administrador.
     * Configura la tabla, carga productos y eventos de botones.
     */
    @FXML
    private void initialize() {
        setupInventarioTable(); // Configura columnas de la tabla
        cargarProductos();      // Carga productos desde la base de datos
        agregarBtn.setOnAction(e -> mostrarDialogoProducto(null)); // Botón agregar
        // Doble clic para editar producto
        inventarioTable.setRowFactory(tv -> {
            TableRow<Producto> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    mostrarDialogoProducto(row.getItem());
                }
            });
            return row;
        });
        inventarioTable.setContextMenu(crearMenuContextual()); // Menú contextual
    }

    /**
     * Configura las columnas de la tabla de inventario.
     */
    private void setupInventarioTable() {
        inventarioTable.getColumns().clear();
        TableColumn<Producto, String> nombreCol = new TableColumn<>("Nombre");
        nombreCol.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        TableColumn<Producto, String> descCol = new TableColumn<>("Descripción");
        descCol.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        TableColumn<Producto, Double> precioCol = new TableColumn<>("Precio");
        precioCol.setCellValueFactory(new PropertyValueFactory<>("precio"));
        TableColumn<Producto, Integer> cantidadCol = new TableColumn<>("Stock");
        cantidadCol.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        TableColumn<Producto, Integer> umbralCol = new TableColumn<>("Umbral");
        umbralCol.setCellValueFactory(new PropertyValueFactory<>("umbral"));
        inventarioTable.getColumns().addAll(nombreCol, descCol, precioCol, cantidadCol, umbralCol);
    }

    /**
     * Carga los productos desde la base de datos y los muestra en la tabla.
     * Resalta en rojo los productos con stock bajo.
     */
    private void cargarProductos() {
        try {
            List<Producto> productos = DatabaseManager.obtenerProductos();
            inventarioTable.getItems().setAll(productos);
            // Alerta visual de stock bajo
            inventarioTable.setRowFactory(tv -> new TableRow<>() {
                @Override
                protected void updateItem(Producto item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item == null || empty) {
                        setStyle("");
                    } else if (item.getCantidad() <= item.getUmbral()) {
                        setStyle("-fx-background-color: #ffcccc;");
                    } else {
                        setStyle("");
                    }
                }
            });
        } catch (Exception e) {
            mostrarAlerta("Error", "No se pudo cargar el inventario.", Alert.AlertType.ERROR);
        }
    }

    /**
     * Crea el menú contextual para editar o eliminar productos.
     */
    private ContextMenu crearMenuContextual() {
        ContextMenu menu = new ContextMenu();
        MenuItem editar = new MenuItem("Editar");
        MenuItem eliminar = new MenuItem("Eliminar");
        editar.setOnAction(e -> {
            Producto p = inventarioTable.getSelectionModel().getSelectedItem();
            if (p != null) mostrarDialogoProducto(p);
        });
        eliminar.setOnAction(e -> {
            Producto p = inventarioTable.getSelectionModel().getSelectedItem();
            if (p != null) eliminarProducto(p);
        });
        menu.getItems().addAll(editar, eliminar);
        return menu;
    }

    /**
     * Muestra un diálogo para agregar o editar un producto.
     * @param producto Producto a editar, o null para agregar
     */
    private void mostrarDialogoProducto(Producto producto) {
        Dialog<Producto> dialog = new Dialog<>();
        dialog.setTitle(producto == null ? "Agregar Producto" : "Editar Producto");
        dialog.setHeaderText(null);
        Label nombreL = new Label("Nombre:");
        TextField nombreF = new TextField(producto != null ? producto.getNombre() : "");
        Label descL = new Label("Descripción:");
        TextField descF = new TextField(producto != null ? producto.getDescripcion() : "");
        Label precioL = new Label("Precio:");
        TextField precioF = new TextField(producto != null ? String.valueOf(producto.getPrecio()) : "");
        Label cantidadL = new Label("Cantidad:");
        TextField cantidadF = new TextField(producto != null ? String.valueOf(producto.getCantidad()) : "");
        Label umbralL = new Label("Umbral:");
        TextField umbralF = new TextField(producto != null ? String.valueOf(producto.getUmbral()) : "");
        VBox vbox = new VBox(8, nombreL, nombreF, descL, descF, precioL, precioF, cantidadL, cantidadF, umbralL, umbralF);
        dialog.getDialogPane().setContent(vbox);
        ButtonType okBtn = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okBtn, ButtonType.CANCEL);
        dialog.setResultConverter(btn -> {
            if (btn == okBtn) {
                try {
                    String nombre = nombreF.getText().trim();
                    String desc = descF.getText().trim();
                    double precio = Double.parseDouble(precioF.getText().trim());
                    int cantidad = Integer.parseInt(cantidadF.getText().trim());
                    int umbral = Integer.parseInt(umbralF.getText().trim());
                    if (nombre.isEmpty() || precio < 0 || cantidad < 0 || umbral < 0) throw new Exception();
                    return new Producto(producto != null ? producto.getId() : 0, nombre, desc, precio, cantidad, umbral);
                } catch (Exception ex) {
                    mostrarAlerta("Datos inválidos", "Verifica los campos.", Alert.AlertType.ERROR);
                }
            }
            return null;
        });
        Optional<Producto> result = dialog.showAndWait();
        result.ifPresent(p -> {
            try {
                if (producto == null) {
                    DatabaseManager.agregarProducto(p.getNombre(), p.getDescripcion(), p.getPrecio(), p.getCantidad(), p.getUmbral());
                    mostrarAlerta("Éxito", "Producto agregado.", Alert.AlertType.INFORMATION);
                } else {
                    DatabaseManager.actualizarProducto(p.getId(), p.getNombre(), p.getDescripcion(), p.getPrecio(), p.getCantidad(), p.getUmbral());
                    mostrarAlerta("Éxito", "Producto actualizado.", Alert.AlertType.INFORMATION);
                }
                cargarProductos();
            } catch (Exception ex) {
                mostrarAlerta("Error", "No se pudo guardar el producto.", Alert.AlertType.ERROR);
            }
        });
    }

    /**
     * Elimina un producto seleccionado tras confirmación.
     * @param producto Producto a eliminar
     */
    private void eliminarProducto(Producto producto) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "¿Eliminar producto?", ButtonType.YES, ButtonType.NO);
        alert.setHeaderText(null);
        Optional<ButtonType> res = alert.showAndWait();
        if (res.isPresent() && res.get() == ButtonType.YES) {
            try {
                DatabaseManager.eliminarProducto(producto.getId());
                mostrarAlerta("Éxito", "Producto eliminado.", Alert.AlertType.INFORMATION);
                cargarProductos();
            } catch (Exception e) {
                mostrarAlerta("Error", "No se pudo eliminar el producto.", Alert.AlertType.ERROR);
            }
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
