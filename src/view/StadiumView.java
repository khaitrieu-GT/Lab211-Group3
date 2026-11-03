package view;

import java.util.List;
import model.Seat;
import model.Section;
import model.Stadium;

/**
 * Giao dien quan ly san, khu vuc, ghe (Nhat: AdminView.renderStadiumList).
 */
public class StadiumView extends BaseView {

    public int showStadiumMenu() {
        this.printHeader("QUAN LY SAN VAN DONG (CRUD)");
        System.out.println("1. Xem danh sach san van dong");
        System.out.println("2. Them san van dong moi");
        System.out.println("3. Xem khu vuc & ghe cua san");
        System.out.println("4. Them khu vuc");
        System.out.println("5. Sua khu vuc (ten, suc chua)");
        System.out.println("6. Mo / Dong khu vuc");
        System.out.println("7. Sinh hang ghe cho khu vuc");
        System.out.println("8. Quan ly trang thai ghe (Khoa/Mo bao tri)");
        System.out.println("9. Khoa / Mo khoa ghe");
        System.out.println("10. Sua thong tin san van dong");
        System.out.println("11. Xoa san van dong");
        System.out.println("12. Xoa khu vuc");
        System.out.println("13. Xoa ghe");
        System.out.println("0. Quay lai");
        return this.inputChoice();
    }

    public void displayStadiums(List<Stadium> stadiums) {
        this.printSection("DANH SACH SAN VAN DONG");
        this.printList(stadiums, "Chua co san van dong.");
    }

    public void displayStadiumLayout(Stadium stadium) {
        this.printSection(stadium.getName() + " - suc chua " + stadium.getCapacity()
                + ", da phan bo " + stadium.getAllocatedCapacity());
        for (Section section : stadium.getSections()) {
            System.out.println(section);
            for (Seat seat : section.getSeats()) {
                System.out.println("    " + seat);
            }
        }
    }
}
