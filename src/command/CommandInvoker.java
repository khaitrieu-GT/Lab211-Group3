package command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Thuc thi lenh va ghi lai lich su ten lenh da chay (phuc vu debug/nhat ky).
 */
public class CommandInvoker {

    private final List<String> history = new ArrayList<>();

    public void run(Command command) {
        command.execute();
        this.history.add(command.getClass().getSimpleName());
    }

    public void runAll(Command... commands) {
        for (Command command : commands) {
            run(command);
        }
    }

    public List<String> getHistory() {
        return Collections.unmodifiableList(this.history);
    }
}
