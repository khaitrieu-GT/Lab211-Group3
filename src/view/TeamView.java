package view;

/**
 * Giao dien quan ly doi bong.
 */
public class TeamView extends BaseView {

    public int showTeamMenu() {
        this.printHeader("QUAN LY DOI BONG (CRUD)");
        System.out.println("1. Xem danh sach doi bong");
        System.out.println("2. Them doi bong");
        System.out.println("3. Sua doi bong");
        System.out.println("4. Xoa doi bong");
        System.out.println("0. Quay lai");
        return this.inputChoice();
    }
}
