package view;

import java.util.List;
import model.enums.PaymentMethod;

/**
 * Giao dien Buyer (Khoa: MainView cua BuyerTicketMVC): chi hien thi menu va nhan du lieu nhap.
 * Luong mua ve do controller.BuyerController dieu khien.
 */
public class BuyerView extends BaseView {

    /** Khoa: MainView.showMenu. */
    public int showMenu(String title) {
        this.printHeader(title);
        System.out.println("1. Tim kiem tran dau");
        System.out.println("2. Xem thong tin tran dau");
        System.out.println("3. Xem so do san");
        System.out.println("4. Chon ghe & dat ve");
        System.out.println("5. Booking dang cho thanh toan");
        System.out.println("6. Lich su dat ve");
        System.out.println("7. Xem ve dien tu (E-Ticket)");
        System.out.println("8. Gui yeu cau huy ve");
        System.out.println("9. Trang thai yeu cau huy ve");
        System.out.println("10. Thong bao");
        System.out.println("11. Ho so ca nhan");
        System.out.println("0. Dang xuat");
        return this.inputChoice();
    }

    /** 1 = theo ten, 2 = theo ngay, 3 = tat ca. */
    public int askSearchType() {
        return this.inputIntInRange("1. Tim theo ten  2. Tim theo ngay  3. Tat ca tran\nLua chon: ", 1, 3);
    }

    public void showSeatSelectionHint(int maxTickets, int holdMinutes) {
        System.out.println("\nToi da " + maxTickets + " ve/booking. Ghe duoc giu " + holdMinutes + " phut.");
        System.out.println("Nhap ghe theo dang A1; neu cung ten hang o nhieu khu, nhap kem ma khu: SEC01-A1.");
    }

    /** Tra ve chuoi rong khi nguoi dung ket thuc chon ghe. */
    public String askSeat() {
        return this.inputString("Nhap ghe muon chon (vd A1), Enter de ket thuc: ");
    }

    public int showBookingActions(int extendMinutes) {
        System.out.println("\n1. Thanh toan");
        System.out.println("2. Gia han giu ghe them " + extendMinutes + " phut");
        System.out.println("3. Bo chon mot ghe");
        System.out.println("4. Huy booking");
        System.out.println("0. Quay lai (ghe van duoc giu den khi het han)");
        return this.inputChoice();
    }

    public PaymentMethod choosePaymentMethod(List<PaymentMethod> methods) {
        this.printSection("CHON PHUONG THUC THANH TOAN");
        for (int i = 0; i < methods.size(); i++) {
            System.out.println((i + 1) + ". " + methods.get(i));
        }
        return methods.get(this.inputIntInRange("Lua chon: ", 1, methods.size()) - 1);
    }

    /** Mo phong ket qua tra ve tu cong thanh toan. */
    public boolean askGatewayApproved() {
        return this.inputIntInRange("Mo phong ket qua cong thanh toan (1 = thanh cong, 2 = that bai): ", 1, 2) == 1;
    }
}
