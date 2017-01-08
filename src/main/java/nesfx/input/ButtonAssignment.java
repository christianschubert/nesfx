package nesfx.input;

import java.util.HashMap;
import java.util.Map;

import javafx.scene.input.KeyCode;

public class ButtonAssignment {
  public static final Map<KeyCode, Integer> map;
  static {
    map = new HashMap<>();

    map.put(KeyCode.DOWN, Button.DOWN);
    map.put(KeyCode.UP, Button.UP);
    map.put(KeyCode.LEFT, Button.LEFT);
    map.put(KeyCode.RIGHT, Button.RIGHT);
    map.put(KeyCode.A, Button.A);
    map.put(KeyCode.S, Button.B);
    map.put(KeyCode.SPACE, Button.SELECT);
    map.put(KeyCode.ENTER, Button.START);
  }
}
