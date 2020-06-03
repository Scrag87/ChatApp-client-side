import java.io.File;
import java.util.List;

public class User {
private static MessagesUtlil messagesUtlil;
  private static User INSTANCE;
  private String nickname;
  private String username;
  private String password;

  private User() {

  }

  public static synchronized User getINSTANCE() {
    if (INSTANCE == null) {
      INSTANCE = new User();
    }
    return INSTANCE;
  }

public List<String> getMessages(){
    File file = new File("./history_" + username + ".txt");
    return messagesUtlil.deserializeAndLoadMessages(file);
}

  public String getNickname() {
    return nickname;
  }

  public void setNickname(String nickname) {
    this.nickname = nickname;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }
}
