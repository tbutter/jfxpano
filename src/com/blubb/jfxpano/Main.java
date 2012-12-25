package com.blubb.jfxpano;

import java.io.FileInputStream;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class Main extends Application {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		launch();
	}

	PanoView pv = null;
	ImageView iv = null;
	final static double inc = Math.PI/180.0;
	
	@Override
	public void start(Stage stage) throws Exception {
		pv = new PanoView(
				new FileInputStream(
						"/Users/thomasbutter/Dropbox/Camera Uploads/2012-11-29 16.08.21.jpg"),
				800, 600);
		BorderPane pane = new BorderPane();
		pane.setCenter(iv = new ImageView(pv.getImage()));
		Scene scene = new Scene(pane);
		stage.setWidth(800);
		stage.setHeight(600);
		stage.setScene(scene);
		scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
			
			@Override
			public void handle(KeyEvent ke) {
				if(ke.getCode() == KeyCode.LEFT) {
					pv.setHeading(pv.getHeading()-inc);
				}
				if(ke.getCode() == KeyCode.RIGHT) {
					pv.setHeading(pv.getHeading()+inc);
				}
				if(ke.getCode() == KeyCode.UP) {
					pv.setTilt(pv.getTilt()-inc);
				}
				if(ke.getCode() == KeyCode.DOWN) {
					pv.setTilt(pv.getTilt()+inc);
				}
			}
		});

		stage.show();
		stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			
			@Override
			public void handle(WindowEvent arg0) {
				System.exit(0);
			}
		});
	}
}
