package repository;

import model.Stadium;
import java.io.*;
import java.util.*;

public class StadiumRepository {
    private final String filePath = "data/stadiums.csv";

    public StadiumRepository() {}

    public List findAll() {
        List list = new ArrayList();
        File file = new File(this.filePath);
        if (!file.exists()) return list;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty() || line.startsWith("id")) continue;
                Stadium s = Stadium.fromCsvLine(line);
                if (s != null) list.add(s);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean saveAll(List list) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(this.filePath))) {
            bw.write("id,name,address,capacity");
            bw.newLine();
            for (Object obj : list) {
                Stadium s = (Stadium) obj; // SỬA LỖI DÒNG 34: Ép kiểu tường minh cho JDK 8 & 17
                bw.write(s.toCsvLine());
                bw.newLine();
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean add(Stadium stadium) {
        List list = this.findAll();
        list.add(stadium);
        return this.saveAll(list);
    }
}