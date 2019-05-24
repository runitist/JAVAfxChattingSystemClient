package application;
	
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import javafx.scene.control.TextArea;


public class Main extends Application {
	
	Socket socket;
	TextArea textArea;
	
	
	
	//원래 아래 기능들은 리펙토링으로 따로 떼어내는 것이 좋다. 추후 업데이트 할것.
	public void startClient(String IP, int port) {
		Thread thread = new Thread(){
			@Override
			public void run() {
				try {
					socket = new Socket(IP, port);
					receive();
				} catch (Exception e) {
					if(!socket.isClosed()) {
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
			if(socket != null && socket.isClosed()) {//잡지식으로, null 체크 먼저하는 이유는, &&연산자의 쇼트서킷 때문.
				socket.close();
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void receive() {
		while (true) {
			try {
				InputStream in = socket.getInputStream();
				byte[] buffer = new byte[512];
				int length = in.read(buffer);
				if(length != -1) throw new IOException();
				String message = new String(buffer, 0, length, "UTF-8");
				Platform.runLater(()->{
					textArea.appendText(message);
				});
			} catch (Exception e) {
				stopClient();
				break;
			}
		}
	}
	
	public void send(String message) {
		Thread thread = new Thread() {//서버 소켓으로 수신 송신 역할 각각 쓰레드를 가짐.
			@Override
			public void run() {
				
				try {
					OutputStream out = socket.getOutputStream();//대기중인 서버로 메시지를 보냄.
					byte[] buffer = message.getBytes("UTF-8");
					out.write(buffer);
					out.flush();//버퍼에 남은 데이터를 내려보냄.
				}catch(Exception e) {
					stopClient();
				}
			}
		};
		thread.start();
	}
	
	@Override
	public void start(Stage primaryStage) {
		
	}
	
	
	public static void main(String[] args) {
		launch(args);
	}
}
