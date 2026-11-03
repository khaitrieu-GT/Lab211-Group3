import controller.MainController;
import service.DataSeeder;

/**
 * Diem khoi chay he thong ban ve san van dong (LAB211 - Group 3).
 * Chuong trinh chay tu Controller: Main -> MainController.start() -> controller theo vai tro -> View.
 * Chay tu thu muc goc du an de doc/ghi du lieu trong thu muc data/.
 */
public class Main {
    public static void main(String[] args) {
        new DataSeeder().seedIfMissing();
        new MainController().start();
    }
}
