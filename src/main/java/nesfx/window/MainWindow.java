package nesfx.window;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import nesfx.console.Nes;
import nesfx.input.ButtonAssignment;

public class MainWindow extends Application {

	private long lastTime;
	private DisplayCanvas canvas;

	private Nes nes;

	private String romPath = "rom/nestest.nes";

	@Override
	public void start(final Stage primaryStage) {
		VBox root = new VBox();

		canvas = new DisplayCanvas();
		root.getChildren().add(canvas);

		Scene scene = new Scene(root);
		scene.setOnKeyPressed(new KeyPressedHandler());
		scene.setOnKeyReleased(new KeyReleasedHandler());

		primaryStage.setTitle("NesFX");
		primaryStage.setScene(scene);
		primaryStage.setResizable(false);
		primaryStage.sizeToScene();
		primaryStage.centerOnScreen();
		primaryStage.show();

		nes = new Nes();
		if (!nes.init(romPath)) {
			Platform.exit();
			System.exit(1);
		}

		lastTime = System.nanoTime();
		new GameLoop().start();
	}

	public class KeyPressedHandler implements EventHandler<KeyEvent> {
		@Override
		public void handle(final KeyEvent event) {
			Integer button = ButtonAssignment.map.get(event.getCode());
			if (button != null) {
				nes.setKeyPressed(0, button);
			}
		}
	}

	public class KeyReleasedHandler implements EventHandler<KeyEvent> {
		@Override
		public void handle(final KeyEvent event) {
			Integer button = ButtonAssignment.map.get(event.getCode());
			if (button != null) {
				nes.setKeyReleased(0, button);
			}
		}
	}

	public class GameLoop extends AnimationTimer {

		@Override
		public void handle(final long currentTime) {
			long delta = currentTime - lastTime;
			lastTime = currentTime;

			nes.runCycles(delta);

			if (nes.isRenderingEnabled()) {
				canvas.draw(nes.getDisplayBuffer());
			}
		}
	}

	public static void main(final String[] args) {
		launch(args);
	}
}
