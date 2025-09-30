package com.orderlink.pos.controller;

import com.orderlink.pos.db.DatabaseManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Controlador para la pantalla de inicio de sesión.
 */
public class LoginController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Label errorLabel;

    /**
     * Inicializa el controlador de la pantalla de login.
     * Configura los eventos para el botón y la tecla Enter.
     */
    @FXML
    private void initialize() {
        // Permite iniciar sesión con el botón
        loginButton.setOnAction(this::handleLogin);
        // Permite iniciar sesión presionando Enter en el campo de contraseña
        passwordField.setOnAction(this::handleLogin);
        // Permite iniciar sesión presionando Enter en el campo de usuario
        usernameField.setOnAction(this::handleLogin);
    }

    /**
     * Maneja el proceso de inicio de sesión.
     * Valida los campos y redirige según el rol.
     * @param event Evento de acción (botón o Enter)
     */
    private void handleLogin(ActionEvent event) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        // Validación de campos vacíos
        if (username.isBlank() || password.isBlank()) {
            errorLabel.setText("Por favor, complete todos los campos.");
            return;
        }

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT role FROM users WHERE username = ? AND password = ?")) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String role = rs.getString("role");
                // Oculta la ventana de login (no la cierra)
                Stage stage = (Stage) loginButton.getScene().getWindow();
                stage.hide();

                try {
                    // Carga la vista correspondiente según el rol
                    String fxmlPath = "/fxml/" + ("administrador".equals(role) ? "AdminView.fxml" : "CashierView.fxml");
                    FXMLLoader loader = new FXMLLoader(LoginController.class.getResource(fxmlPath));
                    loader.setClassLoader(LoginController.class.getClassLoader());
                    loader.setControllerFactory(param -> {
                        try {
                            return param.getDeclaredConstructor().newInstance();
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    });
                    Scene scene = new Scene(loader.load());
                    scene.getStylesheets().add(getClass().getResource("/css/theme.css").toExternalForm());
                    Stage newStage = new Stage();
                    newStage.setScene(scene);
                    newStage.setTitle("OrderLink POS - " + role.substring(0, 1).toUpperCase() + role.substring(1));
                    newStage.setMinWidth(900); // Mejor distribución visual
                    newStage.setMinHeight(600);
                    newStage.setMaximized(true); // Inicia en pantalla completa
                    newStage.show();
                } catch (Exception ex) {
                    // Mostrar alerta visual si falla la carga de la vista
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error al cargar la vista principal");
                    alert.setHeaderText("No se pudo cargar la interfaz principal");
                    alert.setContentText("Detalle: " + ex.getMessage());
                    alert.showAndWait();
                    // Vuelve a mostrar el login
                    stage.show();
                }
            } else {
                errorLabel.setText("Usuario o contraseña incorrectos. Intente nuevamente.");
            }
        } catch (Exception e) {
            errorLabel.setText("Error de conexión a la base de datos.");
        }
    }
    // Puedes agregar aquí métodos auxiliares para validaciones o logs si lo requieres
}
