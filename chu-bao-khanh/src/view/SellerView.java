package view;

import java.util.List;
import model.ETicket;

/**
 * Giao dien Seller (Khanh: SellerView): chi hien thi menu va ket qua.
 * Luong nghiep vu do controller.SellerController dieu khien.
 */
public class SellerView extends BaseView {

    /** Khanh: showMenu. */
    public int showMenu(String title) {
        this.printHeader(title);
        System.out.println("--- Tran dau (CRUD) ---");
        System.out.println("1. Them tran dau            2. Xem tran cua toi        3. Sua tran dau");
        System.out.println("4. Xoa tran dau             5. Mo ban ve               6. Dong ban ve");
        System.out.println("--- Ve mo ban (CRUD) ---");
        System.out.println("7. Tao ve cho mot ghe       8. Tao ve ca khu vuc       9. Xem ve cua tran");
        System.out.println("10. Sua gia/loai ve         11. Dung/mo lai ban mot ve 18. Xoa ve");
        System.out.println("--- Ban hang ---");
        System.out.println("12. Ban ve tai quay         13. Soat ve (check-in QR)  14. Bao cao doanh thu");
        System.out.println("15. Lich su ban             16. Thong bao              17. Ho so ca nhan");
        System.out.println("0. Dang xuat");
        return this.inputChoice();
    }

    /** 1 = dung ban, 2 = mo ban lai. */
    public int askToggleSaleAction() {
        return this.inputIntInRange("1. Dung ban (stop sale)  2. Mo ban lai (put on sale): ", 1, 2);
    }

    public void displaySoldTickets(List<ETicket> tickets) {
        this.printSuccess("Ban ve thanh cong (tien mat).");
        for (ETicket ticket : tickets) {
            System.out.println("  " + ticket);
        }
    }
}
