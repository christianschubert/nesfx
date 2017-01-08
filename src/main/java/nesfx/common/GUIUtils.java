package nesfx.common;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class GUIUtils {
  public static final void showWarningDialog(final String message) {
    Alert alert = new Alert(AlertType.WARNING);
    alert.setTitle("Warning");
    alert.setHeaderText(null);
    alert.setContentText(message);
    alert.showAndWait();
  }
}
