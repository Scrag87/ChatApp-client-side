import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import net.jcip.annotations.GuardedBy;

public class Controller implements Initializable {
  private static volatile boolean isReadThreadRunning;

  //  @GuardedBy("this")
  //  private static Vector<String> messages = new Vector<>();
  //
  //  @GuardedBy("this")
  //  private static Vector<String> tmpMessages = new Vector<>();
  //
  //  @GuardedBy("this")
  //  private static Vector<String> clientList = new Vector<>();
  //

  @GuardedBy("this")
  private static ArrayList<String> messages = new ArrayList<>();

  @GuardedBy("this")
  private static ArrayList<String> tmpMessages = new ArrayList<>();

  @GuardedBy("this")
  private static ArrayList<String> clientList = new ArrayList<>();

  private static Connection connection = Connection.getInstance();
  private static DataInputStream inputStream;
  private static DataOutputStream outputStream;

  @FXML private MenuItem menuConnect;
  @FXML private TextField messageInput;
  @FXML private Button sendMessageButton;

  @FXML private Label labelStatus;

  private final Object listChatViewLock = new Object();

  @GuardedBy("listChatViewLock")
  public ListView chatMemberListview;

  private final Object listUserViewLock = new Object();

  @GuardedBy("listUserViewLock")
  public ListView listView;

  public static EventHandler<WindowEvent> getCloseEventHandler() {
    return closeEventHandler;
  }

  private static EventHandler<WindowEvent> closeEventHandler =
      event -> {
        System.out.println("DO SMTH ON EXIT");
        System.exit(1);
      };

  public void send1Message(ActionEvent actionEvent) {
    sendMessage();
    messageInput.requestFocus();
  }

  private void sendMessage() {
    String message = messageInput.getText();
    if (Connection.getInstance().isConnected()) {
      if (message.equals("")) {
        return;
      }

      if (message.equals("/end")) {
        isReadThreadRunning = false;
        sendMessageToServer(message);
        return;
      }
      addToChatBoxListView(message);
      sendMessageToServer(message);
    } else {
      System.out.println("Connection CloseD");
    }

    messageInput.clear();
  }

  public static boolean initialize(String ipAddress, int portNumber) {
    if (connection.connect(ipAddress, portNumber)) {
      outputStream = connection.getOutputStream();
      inputStream = connection.getInputStream();

      //      Service<Void> service = new Service<Void>() {
      //        @Override
      //        protected Task<Void> createTask() {
      //          return new Task<Void>() {
      //            @Override
      //            protected Void call() throws Exception {
      //              // Долгий код
      //              return null;
      //            }
      //          };
      //        }
      //      };
      //      service.start();
      return true;
    } else {
      return false;
    }
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    Global.setParentController(this);
    labelStatus.setText("Disconnected");
    sendMessageButton.setStyle(
        "-fx-focus-color: transparent;"
            + " -fx-background-insets: 0, -0, 0, 0;"
            + "-fx-faint-focus-color: transparent;");

    messageInput.setStyle("-fx-focus-color: transparent;" + "-fx-faint-focus-color: transparent;");

    listView.setStyle(
        "-fx-focus-color: transparent;"
            + "-fx-faint-focus-color: transparent;"
            + " -fx-background-insets: 0, -0, 0, 0;");
  }

  public void sendMessage(KeyEvent keyEvent) {
    if (keyEvent.getCode() == KeyCode.ENTER) {
      sendMessage();
    }
  }

  public Controller getController() {
    return this;
  }

  public synchronized void runReadMsgTread() {
    System.out.println("runReadMsgTread()-Controller");
    isReadThreadRunning = true;

    Thread readMessage =
        new Thread(
            () -> {
              //  Connection connection = Connection.getInstance();
              while (isReadThreadRunning) {
                String msg = "";
                try {
                  // read the message sent to this client
                  msg = inputStream.readUTF();

                } catch (IOException e) {
                  System.out.println(" Wrong Command");
                  isReadThreadRunning = false;
                  System.out.println(connection);
                  e.printStackTrace();
                }

                if (msg.equals("/end")) {
                  continue;
                }
                if (msg.startsWith("<@#> ") && msg.contains(" Busy")) {
                  tmpMessages.add(msg.split(" ")[1]);
                  Platform.runLater(
                      () -> {
                        clearMessages();
                        messages.add("Name" + tmpMessages + " is taken. Choose another one");
                        listView
                            .getItems()
                            .addAll("Name " + tmpMessages + " is taken. Choose another one");
                        tmpMessages.clear();
                      });
                  continue;
                }
                if (msg.equals("<@#> Connection closed")) {
                  Platform.runLater(
                      () -> {
                        listView.getItems().clear();
                        listView.getItems().addAll("Connection closed");
                        labelStatus.setText("Disconnected");
                        chatMemberListview.getItems().clear();
                        Connection.getInstance().setConnected(false);
                      });
                  break;
                }
                if (msg.startsWith("<@#>/u ")) {
                  ArrayList<String> stringArrayList = new ArrayList<>();
                  stringArrayList.addAll(Arrays.asList(msg.split(" ")));
                  stringArrayList.remove(0);
                  clientList.clear();
                  Platform.runLater(() -> listOfMemberAddToMemberListView(stringArrayList));
                  continue;
                }
                if (msg.startsWith("<@#> ")) {
                  String clientName = getClientName(msg);
                  if (msg.startsWith("<@#> ") && msg.contains("successfully registered!")) {
                    // sendMessageToServer("<@#>/u");

                    Platform.runLater(() -> labelStatus.setText("Connected"));
                    msg = clientName + " you have been successfully registered!";
                  }
                  if (msg.startsWith("<@#> ") && msg.contains("join us")) {
                    Platform.runLater(() -> chatMemberAddToListView(clientName));
                    msg = clientName + " join us";
                  }
                  if (msg.startsWith("<@#> ") && msg.contains("left us")) {
                    Platform.runLater(() -> chatMemberRemoveFromListView(clientName));
                    msg = clientName + " left us";
                  }
                }

                addToMessages(msg);
                System.out.println(msg);
                System.out.println(messages);

                // Update the text of label here
                Platform.runLater(
                    () -> {
                      listView.getItems().clear();
                      listView.getItems().addAll(messages);
                      chatMemberListview.getItems().clear();
                      chatMemberListview.getItems().addAll(clientList);
                    });
              }
            });
    readMessage.start();
  }

  private synchronized void chatMemberAddToListView(String clientName) {
    clientList.add(clientName);
    chatMemberListview.getItems().clear();
    chatMemberListview.getItems().addAll(clientList);
  }

  private synchronized void listOfMemberAddToMemberListView(List<String> clientName) {
    clientList.clear();
    clientList.addAll(clientName);
    chatMemberListview.getItems().clear();
    chatMemberListview.getItems().addAll(clientList);
  }

  private synchronized void chatMemberRemoveFromListView(String clientName) {
    clientList.remove(clientName);
    chatMemberListview.getItems().clear();
    chatMemberListview.getItems().addAll(clientList);
  }

  public synchronized void addToChatBoxListView(String string) {
    addToMessages(string);
    listView.getItems().addAll(string);
  }

  public synchronized void addToMessages(String string) {
    messages.add(string);
  }

  public synchronized void clearMessages() {
    messages.clear();
    listView.getItems().clear();
    messageInput.clear();
  }

  public void sendMessageToServer(String msg) {
    try {
      // write on the output stream
      outputStream.writeUTF(msg);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void menuConnectAction(ActionEvent actionEvent) throws IOException {
    Stage connectWindow = new Stage();
    connectWindow.setTitle("Connect Stage");
    Parent rootConnect = FXMLLoader.load(getClass().getResource("ConnectWindow.fxml"));
    connectWindow.setScene(new Scene(rootConnect, 400, 300));
    connectWindow.setMinHeight(300);
    connectWindow.setMinWidth(400);

    // Specifies the modality for new window.
    connectWindow.initModality(Modality.APPLICATION_MODAL);
    connectWindow.show();
    connectWindow.toFront();
  }

  public void menuCloseAction(ActionEvent actionEvent) {
    System.exit(1);
  }

  private String getClientName(String input) {
    return tokenize(input)[1];
  }

  private String[] tokenize(String received) { // /command recipient message
    String[] clientMessage = {"", "", ""};
    String clientName = "";
    StringTokenizer st = new StringTokenizer(received, " ");
    String command = st.nextToken();
    String recipient = st.nextToken();
    StringBuilder str = new StringBuilder();
    while (st.hasMoreTokens()) {
      str.append(st.nextToken()).append(" ");
    }

    clientMessage[0] = command;
    clientMessage[1] = recipient;
    clientMessage[2] = str.toString();
    System.out.println("tokens " + Arrays.toString(clientMessage));
    return clientMessage;
  }

  public void setLabelStatusText(String text) {
    labelStatus.setText(text);
  }
}
