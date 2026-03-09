package chatfx;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ChatApp extends Application {
	
	private static final String SERVER_ADDRESS = "127.0.01"; // or the server's IP address
	private static final int PORT = 59001;
	private Thread socketThread;
	private volatile boolean running = true;
	private TextArea textArea;
	Socket socket;
    PrintWriter writer;

	@Override
	public void start(Stage primaryStage) throws Exception {
		
		primaryStage.setTitle("Java Chat App");
		Group root = new Group();
		
		textArea = new TextArea();
		textArea.setEditable(false);
		
		TextField textField = new TextField();
		Button button = new Button("Enter");
		Runnable action = () -> {
			System.out.println(textField.getText());
            if(textField.getText()!= null && textField.getText().isEmpty() == false){
                //textArea.appendText("\n" + textField.getText());
                String message = textField.getText();
                if(!message.isEmpty()) {
                	sendMessage(message);
                	textField.clear();
                }
            }
		};
		button.setOnAction((event) -> action.run());
		textField.setOnKeyPressed(event -> {
        	if(event.getCode() == KeyCode.ENTER) {
        		action.run();
        	}
        });
		
		VBox vbox = new VBox(10);
		
		BorderPane center = new BorderPane(textArea);
		HBox bottom = new HBox(textField,button);
		vbox.getChildren().addAll(center,bottom);
		Scene scene = new Scene(vbox,400,300);
		primaryStage.setScene(scene);
		primaryStage.show();
		
		startSocketReader(SERVER_ADDRESS, PORT);
		
		
	}
	
	private void startSocketReader(String host, int port) {
        socketThread = new Thread(() -> {
        	
        	try (Socket socket = new Socket(host, port);
                 BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
        		writer = new PrintWriter(socket.getOutputStream(), true);
                String line;
                while (running && (line = reader.readLine()) != null) {
                    String finalLine = line;
                    // Update UI safely
                    Platform.runLater(() -> {
                        textArea.appendText(finalLine + "\n");
                    });
                }

            } catch (IOException e) {
                Platform.runLater(() -> {
                    textArea.appendText("[Error] " + e.getMessage() + "\n");
                });
            }
        });

        socketThread.setDaemon(true); // Allow JVM to exit if only this thread is running
        socketThread.start();
    }
	
	private void sendMessage(String message) {
        if (writer != null) {
            writer.println(message);
        } else {
            System.err.println("Not connected to server.");
        }
    }
	
	public static void main(String[] args) {
		launch(args);
	}
	
	
	
}
