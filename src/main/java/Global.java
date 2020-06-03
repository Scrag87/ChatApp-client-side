public class Global {

  private static Controller parentController;

  private Global() {
  }

  public static synchronized Controller getParentController() {
    if (parentController == null){
      return new Controller();
    }
    return parentController;
  }

  public static synchronized void setParentController(Controller parentController) {
    Global.parentController = parentController;
  }
}