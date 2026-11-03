package controller;

import exception.BusinessException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import model.Match;
import model.Seat;
import model.Section;
import model.Stadium;
import model.Team;
import model.Ticket;
import model.User;
import model.enums.MatchStatus;
import model.enums.TicketStatus;
import repository.BookingRepository;
import repository.MatchRepository;
import repository.SeatRepository;
import repository.SectionRepository;
import repository.StadiumRepository;
import repository.TeamRepository;
import repository.TicketRepository;
import service.AuditService;
import util.AppConfig;

/**
 * Quan ly tran dau cua Seller (Khanh: SellerController): them, sua, xoa, mo/dong ban.
 * Seller chi thao tac tren tran do minh tao.
 */
public class MatchManagementController {

    private final MatchRepository matchRepo = new MatchRepository();
    private final TicketRepository ticketRepo = new TicketRepository();
    private final TeamRepository teamRepo = new TeamRepository();
    private final StadiumRepository stadiumRepo = new StadiumRepository();
    private final SectionRepository sectionRepo = new SectionRepository();
    private final SeatRepository seatRepo = new SeatRepository();
    private final BookingRepository bookingRepo = new BookingRepository();
    private final MatchController matchController = new MatchController();
    private final AuditService auditService = new AuditService();

    /** Khanh: createMatch. */
    public Match createMatch(User seller, String name, String competition, String homeTeamId, String awayTeamId,
                             String stadiumId, LocalDate date, LocalTime time) {
        if (homeTeamId.equalsIgnoreCase(awayTeamId)) {
            throw new BusinessException("Doi nha va doi khach phai khac nhau.");
        }
        if (this.teamRepo.findById(homeTeamId) == null || this.teamRepo.findById(awayTeamId) == null) {
            throw new BusinessException("Ma doi bong khong ton tai.");
        }
        if (this.stadiumRepo.findById(stadiumId) == null) {
            throw new BusinessException("Ma san van dong khong ton tai.");
        }
        this.requireFuture(date, time);
        this.requireNoScheduleConflict(null, stadiumId, homeTeamId, awayTeamId, date, time);
        Match match = new Match(null, name, competition, homeTeamId.toUpperCase(), awayTeamId.toUpperCase(),
                stadiumId.toUpperCase(), seller.getId(), date, time);
        this.matchRepo.insert(match);
        this.auditService.log(seller.getUsername(), "CREATE_MATCH " + match.getId(), AuditService.SUCCESS);
        return this.matchController.enrich(match);
    }

    /** Khanh: updateMatch. */
    public Match updateMatch(User seller, String matchId, String name, String competition, LocalDate date, LocalTime time) {
        Match match = this.requireOwnMatch(seller, matchId);
        if (match.hasStarted()) {
            throw new BusinessException("Tran dau da dien ra, khong the sua.");
        }
        if (name != null && !name.trim().isEmpty()) {
            match.setName(name.trim());
        }
        if (competition != null && !competition.trim().isEmpty()) {
            match.setCompetition(competition.trim());
        }
        if (date != null || time != null) {
            LocalDate newDate = date != null ? date : match.getMatchDate();
            LocalTime newTime = time != null ? time : match.getStartTime();
            this.requireFuture(newDate, newTime);
            this.requireNoScheduleConflict(match.getId(), match.getStadiumId(), match.getHomeTeamId(), match.getAwayTeamId(),
                    newDate, newTime);
            match.setMatchDate(newDate);
            match.setStartTime(newTime);
        }
        this.matchRepo.update(match);
        this.auditService.log(seller.getUsername(), "UPDATE_MATCH " + matchId, AuditService.SUCCESS);
        return this.matchController.enrich(match);
    }

    /**
     * Khanh: deleteMatch - chi xoa khi tran chua phat sinh giao dich nao.
     * Neu da co booking (ke ca da huy/het han) thi phai giu lai de doi soat lich su -> chi dong ban.
     */
    public void deleteMatch(User seller, String matchId) {
        this.requireOwnMatch(seller, matchId);
        List<Ticket> tickets = this.ticketRepo.findByMatchId(matchId);
        for (Ticket ticket : tickets) {
            if (ticket.getStatus() == TicketStatus.HOLD || ticket.getStatus() == TicketStatus.SOLD) {
                throw new BusinessException("Tran da co ve dang giu/da ban, chi co the dong ban ve.");
            }
        }
        if (!this.bookingRepo.findByMatchId(matchId).isEmpty()) {
            throw new BusinessException("Tran da phat sinh booking (lich su giao dich), chi co the dong ban ve.");
        }
        for (Ticket ticket : tickets) {
            this.ticketRepo.delete(ticket.getId());
        }
        this.matchRepo.delete(matchId);
        this.auditService.log(seller.getUsername(), "DELETE_MATCH " + matchId, AuditService.SUCCESS);
    }

    /** Khanh: getMatches - cac tran cua Seller. */
    public List<Match> getMatches(User seller) {
        List<Match> result = new ArrayList<>();
        for (Match match : this.matchRepo.findBySellerId(seller.getId())) {
            result.add(this.matchController.enrich(match));
        }
        return result;
    }

    /** Khanh: openSale. */
    public Match openSale(User seller, String matchId) {
        Match match = this.requireOwnMatch(seller, matchId);
        boolean hasAvailable = false;
        for (Ticket ticket : this.ticketRepo.findByMatchId(matchId)) {
            if (ticket.isAvailable()) {
                hasAvailable = true;
                break;
            }
        }
        if (!hasAvailable) {
            throw new BusinessException("Tran chua co ve AVAILABLE nao. Hay tao ve truoc khi mo ban.");
        }
        match.openSale();
        this.matchRepo.update(match);
        this.auditService.log(seller.getUsername(), "OPEN_SALE " + matchId, AuditService.SUCCESS);
        return match;
    }

    /** Khanh: closeSale. */
    public Match closeSale(User seller, String matchId) {
        Match match = this.requireOwnMatch(seller, matchId);
        match.closeSale();
        this.matchRepo.update(match);
        this.auditService.log(seller.getUsername(), "CLOSE_SALE " + matchId, AuditService.SUCCESS);
        return match;
    }

    public List<Team> getAllTeams() {
        return this.teamRepo.findAll();
    }

    public List<Stadium> getAllStadiums() {
        return this.stadiumRepo.findAll();
    }

    public List<Section> getSectionsOfMatch(User seller, String matchId) {
        Match match = this.requireOwnMatch(seller, matchId);
        return this.sectionRepo.findByStadiumId(match.getStadiumId());
    }

    public List<Seat> getSeatsOfSection(String sectionId) {
        return this.seatRepo.findBySectionId(sectionId);
    }

    public Match requireOwnMatch(User seller, String matchId) {
        Match match = this.matchRepo.findById(matchId);
        if (match == null || !match.getSellerId().equalsIgnoreCase(seller.getId())) {
            throw new BusinessException("Khong tim thay tran " + matchId + " trong danh sach tran cua ban.");
        }
        return match;
    }

    /**
     * Chong trung lich: mot san chi to chuc mot tran trong khoang MATCH_SLOT_HOURS gio,
     * va mot doi khong da hai tran trong cung mot ngay. Bo qua tran da huy va chinh tran dang sua.
     */
    private void requireNoScheduleConflict(String excludeMatchId, String stadiumId, String homeTeamId,
                                           String awayTeamId, LocalDate date, LocalTime time) {
        LocalDateTime start = LocalDateTime.of(date, time);
        for (Match other : this.matchRepo.findAll()) {
            if (other.getId().equalsIgnoreCase(excludeMatchId) || other.getStatus() == MatchStatus.CANCELLED) {
                continue;
            }
            long minutesApart = Math.abs(Duration.between(other.getStartDateTime(), start).toMinutes());
            if (other.getStadiumId().equalsIgnoreCase(stadiumId) && minutesApart < AppConfig.MATCH_SLOT_HOURS * 60L) {
                throw new BusinessException("San da co tran " + other.getId() + " luc " + other.getMatchDate() + " "
                        + other.getStartTime() + " (moi tran can cach nhau " + AppConfig.MATCH_SLOT_HOURS + " gio).");
            }
            boolean sameTeam = other.getHomeTeamId().equalsIgnoreCase(homeTeamId) || other.getHomeTeamId().equalsIgnoreCase(awayTeamId)
                    || other.getAwayTeamId().equalsIgnoreCase(homeTeamId) || other.getAwayTeamId().equalsIgnoreCase(awayTeamId);
            if (sameTeam && other.getMatchDate().equals(date)) {
                throw new BusinessException("Mot doi bong da co tran " + other.getId() + " trong ngay " + date + ".");
            }
        }
    }

    private void requireFuture(LocalDate date, LocalTime time) {
        if (!LocalDateTime.of(date, time).isAfter(LocalDateTime.now())) {
            throw new BusinessException("Thoi gian thi dau phai o tuong lai.");
        }
    }
}
