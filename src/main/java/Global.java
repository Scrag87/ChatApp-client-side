public class Global {

  private static Controller parentController;

  private Global() {
  }

  public static Controller getParentController() {
    if (parentController == null){
      return new Controller();
    }
    return parentController;
  }

  public static void setParentController(Controller parentController) {
    Global.parentController = parentController;
  }
}