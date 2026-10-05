package repository;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import model.BaseEntity;
import util.AppConfig;

/**
 * Repository dung chung doc/ghi CSV (tong quat hoa tu UserRepository/StadiumRepository/SeatRepository cua Nhat).
 * Moi file co mot khoa rieng dung chung cho moi instance, nen cac thao tac doc-sua-ghi la nguyen tu.
 */
public abstract class CsvRepository<T extends BaseEntity> {

    private static final Map<String, Object> FILE_LOCKS = new ConcurrentHashMap<>();

    private final String filePath;
    private final String header;
    private final String idPrefix;
    private final int idWidth;
    protected final Object lock;

    protected CsvRepository(String fileName, String header, String idPrefix, int idWidth) {
        this.filePath = AppConfig.DATA_DIR + File.separator + fileName;
        this.header = header;
        this.idPrefix = idPrefix;
        this.idWidth = idWidth;
        Object newLock = new Object();
        Object existing = FILE_LOCKS.putIfAbsent(this.filePath, newLock);
        this.lock = existing == null ? newLock : existing;
    }

    /** Chuyen mot dong CSV thanh doi tuong. */
    protected abstract T parse(String line);

    public List<T> findAll() {
        synchronized (this.lock) {
            List<T> list = new ArrayList<>();
            File file = new File(this.filePath);
            if (!file.exists()) {
                return list;
            }
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
                String line = br.readLine(); // bo qua dong tieu de
                while ((line = br.readLine()) != null) {
                    if (line.trim().isEmpty()) {
                        continue;
                    }
                    try {
                        T entity = parse(line);
                        if (entity != null) {
                            list.add(entity);
                        }
                    } catch (RuntimeException e) {
                        System.err.println("[CSV] Bo qua dong loi trong " + this.filePath + ": " + line);
                    }
                }
            } catch (IOException e) {
                System.err.println("[CSV] Khong doc duoc " + this.filePath + ": " + e.getMessage());
            }
            return list;
        }
    }

    public boolean saveAll(List<T> list) {
        synchronized (this.lock) {
            File file = new File(this.filePath);
            File parent = file.getParentFile();
            if (parent != null && !parent.exists() && !parent.mkdirs()) {
                return false;
            }
            try (BufferedWriter bw = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
                bw.write(this.header);
                bw.newLine();
                for (T entity : list) {
                    bw.write(entity.toCsvLine());
                    bw.newLine();
                }
                return true;
            } catch (IOException e) {
                System.err.println("[CSV] Khong ghi duoc " + this.filePath + ": " + e.getMessage());
                return false;
            }
        }
    }

    /** Tao file chi co tieu de neu chua ton tai. */
    public void ensureFile() {
        synchronized (this.lock) {
            if (!new File(this.filePath).exists()) {
                saveAll(new ArrayList<>());
            }
        }
    }

    public boolean exists() {
        return new File(this.filePath).exists();
    }

    public T findById(String id) {
        if (id == null) {
            return null;
        }
        for (T entity : findAll()) {
            if (entity.getId().equalsIgnoreCase(id.trim())) {
                return entity;
            }
        }
        return null;
    }

    /** Them moi; neu chua co id thi cap id tu dong (trong cung khoa de khong trung id). */
    public T insert(T entity) {
        synchronized (this.lock) {
            List<T> list = findAll();
            if (entity.getId() == null || entity.getId().isEmpty()) {
                entity.setId(nextId(list));
            } else {
                for (T existing : list) {
                    if (existing.getId().equalsIgnoreCase(entity.getId())) {
                        throw new IllegalArgumentException("Duplicate id: " + entity.getId());
                    }
                }
            }
            list.add(entity);
            if (!saveAll(list)) {
                throw new IllegalStateException("Cannot save " + this.filePath);
            }
            return entity;
        }
    }

    /** Nhat: StadiumRepository.add. */
    public boolean add(T entity) {
        try {
            insert(entity);
            return true;
        } catch (RuntimeException e) {
            return false;
        }
    }

    public boolean update(T entity) {
        synchronized (this.lock) {
            List<T> list = findAll();
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).getId().equalsIgnoreCase(entity.getId())) {
                    list.set(i, entity);
                    return saveAll(list);
                }
            }
            return false;
        }
    }

    public boolean delete(String id) {
        synchronized (this.lock) {
            List<T> list = findAll();
            boolean removed = false;
            for (int i = list.size() - 1; i >= 0; i--) {
                if (list.get(i).getId().equalsIgnoreCase(id)) {
                    list.remove(i);
                    removed = true;
                }
            }
            return removed && saveAll(list);
        }
    }

    private String nextId(List<T> list) {
        int max = 0;
        for (T entity : list) {
            String id = entity.getId();
            if (id != null && id.startsWith(this.idPrefix)) {
                try {
                    max = Math.max(max, Integer.parseInt(id.substring(this.idPrefix.length())));
                } catch (NumberFormatException ignored) {
                    // id khong theo mau, bo qua
                }
            }
        }
        return this.idPrefix + String.format("%0" + this.idWidth + "d", max + 1);
    }
}
