import view.AdminView;

public class Main {
    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("  KHOI DONG HE THONG BAN VE SAN VAN DONG (LAB211)");
        System.out.println("==================================================");

        AdminView adminView = new AdminView();
        adminView.displayMenu();

        System.out.println("\nCAM ON BAN DA SU DUNG!");
    }
}