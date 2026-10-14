package repository;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import model.BaseEntity;
import util.AppConfig;

/**
 * Repository dung chung doc/ghi CSV - CRUD cho moi thuc the (tong quat hoa tu UserRepository/
 * StadiumRepository/SeatRepository cua Nhat).
 *
 * <ul>
 *   <li>Create : {@link #insert(BaseEntity)} - cap id tu dong (vd U001, TK0001)</li>
 *   <li>Read   : {@link #findAll()}, {@link #findById(String)}</li>
 *   <li>Update : {@link #update(BaseEntity)}</li>
 *   <li>Delete : {@link #delete(String)}</li>
 * </ul>
 *
 * Moi file co mot khoa rieng dung chung cho moi instance, nen cac thao tac doc-sua-ghi la nguyen tu.
 * Khi ghi, du lieu duoc ghi ra file tam roi moi thay the file that, tranh mat du lieu neu bi dung giua chung.
 */
public abstract class CsvRepository<T extends BaseEntity> {

    private static final Map<String, Object> FILE_LOCKS = new ConcurrentHashMap<>();

    private final String filePath;
    private final String header;
    private final String idPrefix;
    private final int idWidth;
    private final Object lock;

    public CsvRepository(String fileName, String header, String idPrefix, int idWidth) {
        this.filePath = AppConfig.DATA_DIR + File.separator + fileName;
        this.header = header;
        this.idPrefix = idPrefix;
        this.idWidth = idWidth;
        Object newLock = new Object();
        Object existing = FILE_LOCKS.putIfAbsent(this.filePath, newLock);
        this.lock = existing == null ? newLock : existing;
    }

    /** Chuyen mot dong CSV thanh doi tuong (moi lop con tu dinh nghia). */
    public abstract T parse(String line);

    /** Khoa cua file - lop con dung khi can doc-sua-ghi nhieu buoc nhu mot thao tac. */
    public Object getLock() {
        return this.lock;
    }

    public String getFilePath() {
        return this.filePath;
    }

    // ============================ READ ============================

    public List<T> findAll() {
        synchronized (this.lock) {
            List<T> list = new ArrayList<>();
            Path path = Paths.get(this.filePath);
            if (!Files.exists(path)) {
                return list;
            }
            try (BufferedReader br = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
                String line = br.readLine(); // bo qua dong tieu de
                int lineNumber = 1;
                while ((line = br.readLine()) != null) {
                    lineNumber++;
                    if (line.trim().isEmpty()) {
                        continue;
                    }
                    try {
                        T entity = this.parse(line);
                        if (entity != null) {
                            list.add(entity);
                        } else {
                            System.err.println("[CSV] Thieu cot o dong " + lineNumber + " cua " + this.filePath);
                        }
                    } catch (RuntimeException e) {
                        System.err.println("[CSV] Bo qua dong " + lineNumber + " loi trong " + this.filePath + ": " + line);
                    }
                }
            } catch (IOException e) {
                System.err.println("[CSV] Khong doc duoc " + this.filePath + ": " + e.getMessage());
            }
            return list;
        }
    }

    public T findById(String id) {
        if (id == null) {
            return null;
        }
        for (T entity : this.findAll()) {
            if (entity.getId().equalsIgnoreCase(id.trim())) {
                return entity;
            }
        }
        return null;
    }

    public boolean existsById(String id) {
        return this.findById(id) != null;
    }

    public boolean exists() {
        return new File(this.filePath).exists();
    }

    // ============================ CREATE ============================

    /** Them moi; neu chua co id thi cap id tu dong (trong cung khoa de khong trung id). */
    public T insert(T entity) {
        synchronized (this.lock) {
            List<T> list = this.findAll();
            if (entity.getId() == null || entity.getId().isEmpty()) {
                entity.setId(this.nextId(list));
            } else {
                for (T existing : list) {
                    if (existing.getId().equalsIgnoreCase(entity.getId())) {
                        throw new IllegalArgumentException("Duplicate id: " + entity.getId());
                    }
                }
            }
            list.add(entity);
            if (!this.saveAll(list)) {
                throw new IllegalStateException("Cannot save " + this.filePath);
            }
            return entity;
        }
    }

    // ============================ UPDATE ============================

    public boolean update(T entity) {
        synchronized (this.lock) {
            List<T> list = this.findAll();
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).getId().equalsIgnoreCase(entity.getId())) {
                    list.set(i, entity);
                    return this.saveAll(list);
                }
            }
            return false;
        }
    }

    // ============================ DELETE ============================

    public boolean delete(String id) {
        if (id == null) {
            return false;
        }
        synchronized (this.lock) {
            List<T> list = this.findAll();
            boolean removed = list.removeIf(entity -> entity.getId().equalsIgnoreCase(id.trim()));
            return removed && this.saveAll(list);
        }
    }

    // ============================ FILE ============================

    /** Ghi toan bo danh sach: ghi file tam roi thay the file that. */
    public boolean saveAll(List<T> list) {
        synchronized (this.lock) {
            Path target = Paths.get(this.filePath);
            Path tmp = Paths.get(this.filePath + ".tmp");
            try {
                Path parent = target.toAbsolutePath().getParent();
                if (parent != null) {
                    Files.createDirectories(parent);
                }
                try (BufferedWriter bw = Files.newBufferedWriter(tmp, StandardCharsets.UTF_8)) {
                    bw.write(this.header);
                    bw.newLine();
                    for (T entity : list) {
                        bw.write(entity.toCsvLine());
                        bw.newLine();
                    }
                }
                try {
                    Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
                } catch (AtomicMoveNotSupportedException e) {
                    Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING);
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
            if (!this.exists()) {
                this.saveAll(new ArrayList<>());
            }
        }
    }

    /** Id ke tiep = so lon nhat hien co + 1 (khong dung lai id da xoa). */
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
