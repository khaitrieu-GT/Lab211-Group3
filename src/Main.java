import java.util.NoSuchElementException;
import service.DataSeeder;
import view.MainView;

/**
 * Diem khoi chay he thong ban ve san van dong (LAB211 - Group 3).
 * Chay tu thu muc goc du an de doc/ghi du lieu trong thu muc data/.
 */
public class Main {
    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("  KHOI DONG HE THONG BAN VE SAN VAN DONG (LAB211)");
        System.out.println("==================================================");

        new DataSeeder().seedIfMissing();
        try {
            new MainView().start();
        } catch (NoSuchElementException e) {
            System.out.println();
            System.out.println("Ket thuc du lieu dau vao.");
        }

        System.out.println("\nCAM ON BAN DA SU DUNG!");
    }
}
