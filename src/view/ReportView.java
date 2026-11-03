package view;

import dto.SalesReport;
import util.MoneyUtil;

/** Hien thi bao cao doanh thu - dung chung cho Admin (toan he thong) va Seller (tran cua minh). */
public class ReportView extends BaseView {

    public void displayReport(SalesReport report) {
        this.printSection("BAO CAO DOANH THU");
        System.out.println(String.format("%-5s | %-26s | %8s | %6s | %8s | %18s", "Tran", "Ten", "Tong ve", "Da ban",
                "Dang giu", "Doanh thu"));
        for (SalesReport.Line line : report.getLines()) {
            System.out.println(String.format("%-5s | %-26s | %8d | %6d | %8d | %18s", line.getMatchId(),
                    line.getMatchName(), line.getTotalTickets(), line.getSoldTickets(), line.getHeldTickets(),
                    MoneyUtil.format(line.getRevenue())));
        }
        System.out.println("Tong ve da ban : " + report.getTotalSold());
        System.out.println("Tong doanh thu : " + MoneyUtil.format(report.getTotalRevenue()));
        System.out.println("Da hoan tien   : " + MoneyUtil.format(report.getTotalRefunded()));
    }
}
