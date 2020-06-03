import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class MessagesUtlil implements Serializable {

  private static CopyOnWriteArrayList<String> messages =
      new CopyOnWriteArrayList<>();


  private static void addMessage(String msg) {
    messages.add(msg);
  }

  private static void printMessages() {
    System.out.println(messages);
  }

  private static void clearMessages() {
    messages.clear();
  }


  public static void serializeAnsSaveMessages(File file,List<String> msg) {
    try (ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(file))) {
      os.writeObject(msg);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static List<String> deserializeAndLoadMessages(File file) {
    try (ObjectInputStream is = new ObjectInputStream(new FileInputStream(file))) {
      return (List<String>) is.readObject();
    } catch (IOException e) {
      e.printStackTrace();
      return  Collections.EMPTY_LIST;
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }
    return Collections.EMPTY_LIST;
  }

  @Override
  public String toString() {
    return "Messages{" +
        "messages=" + messages +
        '}';
  }

}
