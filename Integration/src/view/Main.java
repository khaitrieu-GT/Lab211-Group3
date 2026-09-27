package view;
import controller.MainController;

public class Main {
    public static void main(String[] args) {
        System.out.println("==========================================");
        System.out.println(" CHAO MUNG DEN VOI STADIUM TICKET BOOKING ");
        System.out.println("==========================================");

        MainController controller = new MainController();

        boolean loginResult = controller.handleLogin("admin", "123456");
        if (loginResult) {
            controller.triggerNotification("U001", "Chuc mung ban da dang nhap thanh cong!");
        }
    }
}