package application;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

public class Main extends Application {

	private Socket socket;
	private TextArea textArea;

	// 원래 아래 기능들은 리펙토링으로 따로 떼어내는 것이 좋다. 추후 업데이트 할것.
	// 학습용이라 그냥 만들었지만, 원래는 아키텍쳐부터 짠 후 인터페이스 작성하고 이후 모듈간 로직을 채우는 것이 좋음.
	public void startClient(String IP, int port) {
		Thread thread = new Thread() {// 단일 인스턴스라서 쓰레드풀은 필요 없다.
			@Override
			public void run() {
				try {
					socket = new Socket(IP, port);// 서버 소켓에 연결할 주소값을 넣어준다
					receive();
				} catch (Exception e) {
					if (!socket.isClosed()) {
						stopClient();
						System.out.println("[서버 접속 실패]");
						Platform.exit();
					}
				}
			}
		};
		thread.start();
	}

	public void stopClient() {
		try {
			if (socket != null && socket.isClosed()) {// 잡지식으로, null 체크 먼저하는 이유는, &&연산자의 쇼트서킷 때문.
				socket.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void receive() {
		while (true) {
			try {
				InputStream in = socket.getInputStream();
				byte[] buffer = new byte[512];
				int length = in.read(buffer);
				if (length != -1)
					throw new IOException();
				String message = new String(buffer, 0, length, "UTF-8");
				Platform.runLater(() -> {
					textArea.appendText(message);
				});
			} catch (Exception e) {
				stopClient();
				break;
			}
		}
	}

	public void send(String message) {
		Thread thread = new Thread() {// 서버 소켓으로 수신 송신 역할 각각 쓰레드를 가짐.
			@Override
			public void run() {
				try {
					OutputStream out = socket.getOutputStream();// 대기중인 서버로 메시지를 보냄.
					byte[] buffer = message.getBytes("UTF-8");
					out.write(buffer);
					out.flush();// 버퍼에 남은 데이터를 내려보냄.
					Platform.runLater(() -> {
						textArea.appendText(message);
					});
				} catch (Exception e) {
					stopClient();
				}
			}
		};
		thread.start();
	}

	@Override
	public void start(Stage primaryStage) {
		BorderPane root = new BorderPane();
		root.setPadding(new Insets(5));

		HBox hbox = new HBox();
		hbox.setSpacing(5);

		TextField userName = new TextField();
		userName.setPrefWidth(150);
		userName.setPromptText("닉네임을 입력하세요.");
		HBox.setHgrow(userName, Priority.ALWAYS);

		TextField IPText = new TextField("127.0.0.1");
		TextField portText = new TextField("9876");
		portText.setPrefWidth(80);

		hbox.getChildren().addAll(userName, IPText, portText);
		root.setTop(hbox);

		textArea = new TextArea();
		textArea.setEditable(false);
		root.setCenter(textArea);

		TextField input = new TextField();
		input.setPrefWidth(Double.MAX_VALUE);
		input.setDisable(true);

		input.setOnAction(event -> {
			send(userName.getText() + ": " + input.getText() + "\n");
			input.setText("");
			input.requestFocus();
		});

		Button sendButton = new Button("보내기");
		sendButton.setDisable(true);

		sendButton.setOnAction(event -> {
			send(userName.getText() + ": " + input.getText() + "\n");
			input.setText("");
			input.requestFocus();
		});

		Button connectionButton = new Button("접속하기");

		connectionButton.setOnAction(event -> {
			if (connectionButton.getText().equals("접속하기")) {
				int port = 9876;
				try {
					port = Integer.parseInt(portText.getText());
				} catch (Exception e) {
					e.printStackTrace();
				}
				startClient(IPText.getText(), port);
				Platform.runLater(() -> {
					textArea.appendText("[채팅방 접속]\n");
				});
				connectionButton.setText("종료하기");
				input.setDisable(false);
				sendButton.setDisable(false);
				input.requestFocus();
			} else {
				stopClient();
				Platform.runLater(() -> {
					textArea.appendText("[채팅방 퇴장]\n");
				});
				connectionButton.setText("접속하기");
				input.setDisable(true);
				sendButton.setDisable(true);
			}
		});

		BorderPane pane = new BorderPane();
		pane.setLeft(connectionButton);
		pane.setCenter(input);
		pane.setRight(sendButton);
		root.setBottom(pane);
		Scene scene = new Scene(root, 400, 400);
		primaryStage.setTitle("[채팅 클라이언트]");
		primaryStage.setScene(scene);
		primaryStage.setOnCloseRequest(event -> stopClient());
		primaryStage.show();

		connectionButton.requestFocus();
	}

	public static void main(String[] args) {
		launch(args);
	}
}
