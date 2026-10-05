package repository;

import model.Seat;
import model.SeatStatus;
import java.io.*;
import java.util.*;

public class SeatRepository {
    private final String filePath = "data/seats.csv";

    public SeatRepository() {}

    public List findAll() {
        List list = new ArrayList();
        File file = new File(this.filePath);
        if (!file.exists()) return list;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty() || line.startsWith("id")) continue;
                Seat seat = Seat.fromCsvLine(line);
                if (seat != null) list.add(seat);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean saveAll(List list) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(this.filePath))) {
            bw.write("id,sectionId,rowNumber,seatNumber,status,version");
            bw.newLine();
            for (Seat s : list) {
                bw.write(s.toCsvLine());
                bw.newLine();
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public synchronized boolean updateStatus(String seatId, SeatStatus newStatus) {
        List seats = this.findAll();
        boolean updated = false;
        for (Seat s : seats) {
            if (s.getId().equalsIgnoreCase(seatId)) {
                s.setStatus(newStatus);
                s.setVersion(s.getVersion() + 1);
                updated = true;
                break;
            }
        }
        return updated && this.saveAll(seats);
    }
}