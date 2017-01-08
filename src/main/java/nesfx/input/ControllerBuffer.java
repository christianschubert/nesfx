package nesfx.input;

import nesfx.common.Constants;

public class ControllerBuffer {

  private boolean[][] controllers = new boolean[Constants.CONTROLLER_COUNT][Constants.BUTTON_COUNT];
  private int[] nextButton = new int[Constants.CONTROLLER_COUNT];

  public void reset() {
    for (int i = 0; i < controllers.length; i++) {
      for (int j = 0; j < controllers[i].length; j++) {
        controllers[i][j] = false;
      }
    }

    for (int i = 0; i < nextButton.length; i++) {
      nextButton[i] = 0;
    }
  }

  public boolean isKeyPressed(final int controller, final int button) {
    return controllers[controller][button];
  }

  public boolean isNextKeyPressed(final int controller) {
    int next = nextButton[controller]++;
    if (nextButton[controller] >= Constants.BUTTON_COUNT) {
      nextButton[controller] = 0;
    }
    return controllers[controller][next];
  }

  public void setKeyPressed(final int controller, final int button) {
    controllers[controller][button] = true;
  }

  public void setKeyReleased(final int controller, final int button) {
    controllers[controller][button] = false;
  }

  public void save(final ControllerBuffer controllerBuffer) {
    controllers = controllerBuffer.getControllers();
  }

  public boolean[][] getControllers() {
    return controllers;
  }
}