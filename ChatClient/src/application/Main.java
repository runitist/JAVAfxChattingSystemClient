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
	
	
	
	//���� �Ʒ� ��ɵ��� �����丵���� ���� ����� ���� ����. ���� ������Ʈ �Ұ�.
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
						System.out.println("[���� ���� ����]");
						Platform.exit();
					}
				}
			}
		};
		thread.start();
	}
	
	public void stopClient() {
		try {
			if(socket != null && socket.isClosed()) {//����������, null üũ �����ϴ� ������, &&�������� ��Ʈ��Ŷ ����.
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
		Thread thread = new Thread() {//���� �������� ���� �۽� ���� ���� �����带 ����.
			@Override
			public void run() {
				
				try {
					OutputStream out = socket.getOutputStream();//������� ������ �޽����� ����.
					byte[] buffer = message.getBytes("UTF-8");
					out.write(buffer);
					out.flush();//���ۿ� ���� �����͸� ��������.
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
