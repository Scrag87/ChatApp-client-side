import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class AppChatRun extends Application {
Scene sceneConnect,sceneChat;
  @Override
  public void start(Stage primaryStage) throws Exception {
    Parent rootChat = FXMLLoader.load(getClass().getResource("chatApp.fxml"));
    Parent rootConnect = FXMLLoader.load(getClass().getResource("ConnectWindow.fxml"));
    primaryStage.setTitle("Chat app");
    primaryStage.setMinHeight(500);
    primaryStage.setMinWidth(600);
    sceneChat =new Scene(rootChat, 600, 500);
    sceneConnect =new Scene(rootConnect, 350, 300);
    primaryStage.setOnCloseRequest(Controller.getCloseEventHandler());
    primaryStage.setScene(sceneChat);
    primaryStage.show();

  }

  public static void main(String[] args) {
    launch(args);
  }
}
