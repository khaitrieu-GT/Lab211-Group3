package controller;

import exception.BusinessException;
import java.math.BigDecimal;
import java.util.List;
import model.Seat;
import model.Section;
import model.Stadium;
import model.User;
import model.enums.SectionStatus;
import repository.MatchRepository;
import repository.SeatRepository;
import repository.SectionRepository;
import repository.StadiumRepository;
import repository.TicketRepository;
import service.AuditService;
import util.ValidationUtil;

/**
 * Quan ly san van dong va khu vuc (Nhat: AdminController, Khanh: Section).
 */
public class StadiumController {

    private final StadiumRepository stadiumRepo = new StadiumRepository();
    private final SectionRepository sectionRepo = new SectionRepository();
    private final SeatRepository seatRepo = new SeatRepository();
    private final MatchRepository matchRepo = new MatchRepository();
    private final TicketRepository ticketRepo = new TicketRepository();
    private final AuditService auditService = new AuditService();
    private final User admin;

    public StadiumController(User admin) {
        this.admin = admin;
    }

    /** Nhat: getAllStadiums. */
    public List<Stadium> getAllStadiums() {
        return this.stadiumRepo.findAll();
    }

    /** Nhat: createStadium (id duoc cap tu dong). */
    public Stadium createStadium(String name, String address, String city, int capacity) {
        if (ValidationUtil.isBlank(name) || capacity <= 0) {
            throw new BusinessException("Ten san khong duoc trong va suc chua phai > 0.");
        }
        Stadium stadium = new Stadium(null, name.trim(), address, city, capacity);
        this.stadiumRepo.insert(stadium);
        this.auditService.log(this.admin.getUsername(), "CREATE_STADIUM " + stadium.getId(), AuditService.SUCCESS);
        return stadium;
    }

    /** San kem khu vuc va ghe. */
    public Stadium getStadiumLayout(String stadiumId) {
        Stadium stadium = this.requireStadium(stadiumId);
        for (Section section : this.sectionRepo.findByStadiumId(stadium.getId())) {
            section.setSeats(this.seatRepo.findBySectionId(section.getId()));
            stadium.addSection(section);
        }
        return stadium;
    }

    /** Sua thong tin san; suc chua moi khong duoc nho hon tong suc chua cac khu vuc da phan bo. */
    public Stadium updateStadium(String stadiumId, String name, String address, String city, int capacity) {
        Stadium stadium = this.getStadiumLayout(stadiumId);
        if (!ValidationUtil.isBlank(name)) {
            stadium.setName(name.trim());
        }
        if (!ValidationUtil.isBlank(address)) {
            stadium.setAddress(address.trim());
        }
        if (!ValidationUtil.isBlank(city)) {
            stadium.setCity(city.trim());
        }
        if (capacity < 0) {
            throw new BusinessException("Suc chua khong duoc am.");
        }
        if (capacity > 0) {
            if (capacity < stadium.getAllocatedCapacity()) {
                throw new BusinessException("Suc chua moi nho hon tong suc chua cac khu vuc ("
                        + stadium.getAllocatedCapacity() + ").");
            }
            stadium.setCapacity(capacity);
        }
        this.stadiumRepo.update(stadium);
        this.auditService.log(this.admin.getUsername(), "UPDATE_STADIUM " + stadiumId, AuditService.SUCCESS);
        return stadium;
    }

    /** Xoa san (kem khu vuc va ghe) khi chua co tran dau nao to chuc tai san. */
    public void deleteStadium(String stadiumId) {
        Stadium stadium = this.requireStadium(stadiumId);
        if (!this.matchRepo.findByStadiumId(stadium.getId()).isEmpty()) {
            throw new BusinessException("San dang duoc dung cho tran dau, khong the xoa.");
        }
        for (Section section : this.sectionRepo.findByStadiumId(stadium.getId())) {
            this.removeSectionWithSeats(section.getId());
        }
        this.stadiumRepo.delete(stadium.getId());
        this.auditService.log(this.admin.getUsername(), "DELETE_STADIUM " + stadiumId, AuditService.SUCCESS);
    }

    public List<Section> getSectionsByStadium(String stadiumId) {
        return this.sectionRepo.findByStadiumId(stadiumId);
    }

    public Section createSection(String stadiumId, String name, String type, int capacity, BigDecimal basePrice) {
        Stadium stadium = this.getStadiumLayout(stadiumId);
        if (capacity <= 0) {
            throw new BusinessException("Suc chua khu vuc phai > 0.");
        }
        if (stadium.getAllocatedCapacity() + capacity > stadium.getCapacity()) {
            throw new BusinessException("Tong suc chua cac khu vuc vuot suc chua san (" + stadium.getCapacity() + ").");
        }
        Section section = new Section(null, stadium.getId(), name, type.toUpperCase(), capacity, basePrice);
        this.sectionRepo.insert(section);
        this.auditService.log(this.admin.getUsername(), "CREATE_SECTION " + section.getId(), AuditService.SUCCESS);
        return section;
    }

    /** Khanh: Section.updateStatus - mo/dong khu vuc. */
    public Section updateSectionStatus(String sectionId, SectionStatus status) {
        Section section = this.requireSection(sectionId);
        section.updateStatus(status);
        this.sectionRepo.update(section);
        this.auditService.log(this.admin.getUsername(), "SECTION_STATUS " + sectionId + " -> " + status, AuditService.SUCCESS);
        return section;
    }

    /**
     * Khanh: Section.updateSection - doi ten va suc chua.
     * Suc chua moi: khong nho hon so ghe hien co va tong cac khu vuc khong vuot suc chua san.
     */
    public Section updateSection(String sectionId, String name, int capacity) {
        Section section = this.requireSection(sectionId);
        section.setSeats(this.seatRepo.findBySectionId(sectionId));
        Stadium stadium = this.getStadiumLayout(section.getStadiumId());
        int otherSections = stadium.getAllocatedCapacity() - section.getCapacity();
        if (capacity <= 0) {
            throw new BusinessException("Suc chua khu vuc phai > 0.");
        }
        if (otherSections + capacity > stadium.getCapacity()) {
            throw new BusinessException("Tong suc chua cac khu vuc vuot suc chua san (" + stadium.getCapacity() + ").");
        }
        try {
            section.updateSection(ValidationUtil.isBlank(name) ? section.getName() : name.trim(), capacity);
        } catch (IllegalArgumentException e) {
            throw new BusinessException("Suc chua moi nho hon so ghe hien co (" + section.getSeats().size() + ").");
        }
        this.sectionRepo.update(section);
        this.auditService.log(this.admin.getUsername(), "UPDATE_SECTION " + sectionId, AuditService.SUCCESS);
        return section;
    }

    /** Xoa khu vuc (kem ghe) khi chua co ve nao duoc tao cho khu vuc. */
    public void deleteSection(String sectionId) {
        Section section = this.requireSection(sectionId);
        if (!this.ticketRepo.findBySectionId(section.getId()).isEmpty()) {
            throw new BusinessException("Khu vuc da co ve mo ban, chi co the dong khu vuc.");
        }
        this.removeSectionWithSeats(section.getId());
        this.auditService.log(this.admin.getUsername(), "DELETE_SECTION " + sectionId, AuditService.SUCCESS);
    }

    private void removeSectionWithSeats(String sectionId) {
        for (Seat seat : this.seatRepo.findBySectionId(sectionId)) {
            this.seatRepo.delete(seat.getId());
        }
        this.sectionRepo.delete(sectionId);
    }

    private Stadium requireStadium(String stadiumId) {
        Stadium stadium = this.stadiumRepo.findById(stadiumId);
        if (stadium == null) {
            throw new BusinessException("Khong tim thay san " + stadiumId);
        }
        return stadium;
    }

    private Section requireSection(String sectionId) {
        Section section = this.sectionRepo.findById(sectionId);
        if (section == null) {
            throw new BusinessException("Khong tim thay khu vuc " + sectionId);
        }
        return section;
    }
}
