package controller;

import exception.BusinessException;
import java.util.List;
import model.Team;
import model.User;
import repository.MatchRepository;
import repository.TeamRepository;
import service.AuditService;
import util.ValidationUtil;

/**
 * Quan ly doi bong cua Admin.
 */
public class TeamController {

    private final TeamRepository teamRepo = new TeamRepository();
    private final MatchRepository matchRepo = new MatchRepository();
    private final AuditService auditService = new AuditService();
    private final User admin;

    public TeamController(User admin) {
        this.admin = admin;
    }

    public List<Team> getAllTeams() {
        return this.teamRepo.findAll();
    }

    public Team createTeam(String name, String logo, String country) {
        if (ValidationUtil.isBlank(name)) {
            throw new BusinessException("Ten doi bong khong duoc trong.");
        }
        for (Team existing : this.teamRepo.findAll()) {
            if (existing.getName().equalsIgnoreCase(name.trim())) {
                throw new BusinessException("Doi bong " + name.trim() + " da ton tai (" + existing.getId() + ").");
            }
        }
        Team team = new Team(null, name.trim(), logo, country);
        this.teamRepo.insert(team);
        this.auditService.log(this.admin.getUsername(), "CREATE_TEAM " + team.getId(), AuditService.SUCCESS);
        return team;
    }

    public Team updateTeam(String teamId, String name, String logo, String country) {
        Team team = this.requireTeam(teamId);
        if (!ValidationUtil.isBlank(name)) {
            for (Team existing : this.teamRepo.findAll()) {
                if (!existing.getId().equalsIgnoreCase(team.getId()) && existing.getName().equalsIgnoreCase(name.trim())) {
                    throw new BusinessException("Ten doi bong da ton tai (" + existing.getId() + ").");
                }
            }
            team.setName(name.trim());
        }
        if (!ValidationUtil.isBlank(logo)) {
            team.setLogo(logo.trim());
        }
        if (!ValidationUtil.isBlank(country)) {
            team.setCountry(country.trim());
        }
        this.teamRepo.update(team);
        this.auditService.log(this.admin.getUsername(), "UPDATE_TEAM " + teamId, AuditService.SUCCESS);
        return team;
    }

    /** Xoa doi bong khi doi chua tham gia tran dau nao. */
    public void deleteTeam(String teamId) {
        Team team = this.requireTeam(teamId);
        if (!this.matchRepo.findByTeamId(team.getId()).isEmpty()) {
            throw new BusinessException("Doi bong dang co tran dau, khong the xoa.");
        }
        this.teamRepo.delete(team.getId());
        this.auditService.log(this.admin.getUsername(), "DELETE_TEAM " + teamId, AuditService.SUCCESS);
    }

    private Team requireTeam(String teamId) {
        Team team = this.teamRepo.findById(teamId);
        if (team == null) {
            throw new BusinessException("Khong tim thay doi bong " + teamId);
        }
        return team;
    }
}
