package view;

import java.util.List;
import model.Match;

/** Hien thi tran dau (Khoa: MatchView, Khanh: SellerView.showMatches). */
public class MatchView extends BaseView {

    public void displayMatches(List<Match> matches) {
        printSection("DANH SACH TRAN DAU");
        if (matches.isEmpty()) {
            System.out.println("Khong co tran dau nao.");
            return;
        }
        System.out.println(String.format("%-5s | %-26s | %-34s | %-16s | %-22s | %s",
                "ID", "Ten tran", "Doi dau", "Thoi gian", "San", "Trang thai"));
        for (Match match : matches) {
            System.out.println(match);
        }
    }

    public void displayMatch(Match match) {
        printSection("THONG TIN TRAN DAU");
        System.out.println("Ma tran     : " + match.getId());
        System.out.println("Ten tran    : " + match.getName());
        System.out.println("Giai dau    : " + match.getCompetition());
        System.out.println("Doi nha     : " + (match.getHomeTeam() == null ? match.getHomeTeamId() : match.getHomeTeam()));
        System.out.println("Doi khach   : " + (match.getAwayTeam() == null ? match.getAwayTeamId() : match.getAwayTeam()));
        System.out.println("Ngay        : " + match.getMatchDate());
        System.out.println("Gio         : " + match.getStartTime());
        if (match.getStadium() != null) {
            System.out.println("San         : " + match.getStadium().getName());
            System.out.println("Dia chi     : " + match.getStadium().getAddress() + ", " + match.getStadium().getCity());
        }
        System.out.println("Trang thai  : " + match.getStatus() + (match.isOnSale() ? " (dang mo ban ve)" : ""));
    }
}
