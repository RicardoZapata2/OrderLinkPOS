package com.orderlink.pos.db;

import java.sql.*;

/**
 * DatabaseManager se encarga de la conexión y la inicialización de la base de datos SQLite.
 */
public class DatabaseManager {
    private static final String DB_URL = "jdbc:sqlite:cafeteria.db";
    private static Connection connection;

    /**
     * Obtiene la conexión a la base de datos.
     * @return Connection
     * @throws SQLException si ocurre un error de conexión
     */
    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(DB_URL);
        }
        return connection;
    }

    /**
     * Inicializa la base de datos creando las tablas necesarias si no existen.
     */
    public static void initializeDatabase() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            // Tabla de usuarios
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS users (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT UNIQUE NOT NULL,
                    password TEXT NOT NULL,
                    role TEXT CHECK(role IN ('administrador','cajero')) NOT NULL
                );
            """);
            // Tabla de productos
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS products (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL,
                    description TEXT,
                    price REAL NOT NULL,
                    stock INTEGER NOT NULL,
                    low_stock_threshold INTEGER NOT NULL
                );
            """);
            // Tabla de ventas
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS sales (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id INTEGER NOT NULL,
                    total_amount REAL NOT NULL,
                    sale_date TEXT NOT NULL,
                    FOREIGN KEY(user_id) REFERENCES users(id)
                );
            """);
            // Tabla de items de venta
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS sale_items (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    sale_id INTEGER NOT NULL,
                    product_id INTEGER NOT NULL,
                    quantity INTEGER NOT NULL,
                    price_per_unit REAL NOT NULL,
                    FOREIGN KEY(sale_id) REFERENCES sales(id),
                    FOREIGN KEY(product_id) REFERENCES products(id)
                );
            """);
            // Tabla de pedidos
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS pedidos (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    fecha TEXT NOT NULL,
                    estado TEXT NOT NULL
                );
            """);
            // Tabla de productos por pedido
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS productos_pedido (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    pedido_id INTEGER NOT NULL,
                    producto_id INTEGER NOT NULL,
                    cantidad INTEGER NOT NULL,
                    FOREIGN KEY(pedido_id) REFERENCES pedidos(id),
                    FOREIGN KEY(producto_id) REFERENCES products(id)
                );
            """);
            // Usuarios por defecto (según la guía y la rúbrica)
            stmt.executeUpdate("""
                INSERT OR IGNORE INTO users (username, password, role) VALUES
                ('administrador', '1234', 'administrador'),
                ('cajero', '1234', 'cajero');
            """);
        } catch (SQLException e) {
            System.err.println("Error inicializando la base de datos: " + e.getMessage());
        }
    }

    // CRUD de productos

    // CRUD de productos
    public static void agregarProducto(String nombre, String descripcion, double precio, int cantidad, int umbral) throws SQLException {
        String sql = "INSERT INTO products (name, description, price, stock, low_stock_threshold) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, nombre);
            stmt.setString(2, descripcion);
            stmt.setDouble(3, precio);
            stmt.setInt(4, cantidad);
            stmt.setInt(5, umbral);
            stmt.executeUpdate();
        }
    }

    public static void actualizarProducto(int id, String nombre, String descripcion, double precio, int cantidad, int umbral) throws SQLException {
        String sql = "UPDATE products SET name=?, description=?, price=?, stock=?, low_stock_threshold=? WHERE id=?";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, nombre);
            stmt.setString(2, descripcion);
            stmt.setDouble(3, precio);
            stmt.setInt(4, cantidad);
            stmt.setInt(5, umbral);
            stmt.setInt(6, id);
            stmt.executeUpdate();
        }
    }

    public static void eliminarProducto(int id) throws SQLException {
        String sql = "DELETE FROM products WHERE id=?";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    public static java.util.List<com.orderlink.pos.model.Producto> obtenerProductos() throws SQLException {
        java.util.List<com.orderlink.pos.model.Producto> lista = new java.util.ArrayList<>();
        String sql = "SELECT * FROM products";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                lista.add(new com.orderlink.pos.model.Producto(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("description"),
                    rs.getDouble("price"),
                    rs.getInt("stock"),
                    rs.getInt("low_stock_threshold")
                ));
            }
        }
        return lista;
    }
}
