package server.ui;

import common.User;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

public class ServerState {

    private static final ServerState instance = new ServerState();

    private final ObservableList<String> logs =
            FXCollections.observableArrayList();

    private final ObservableList<User> onlineUsers =
            FXCollections.observableArrayList();

    private final Set<String> onlineUsernames = new HashSet<>();

    private final IntegerProperty onlineCount =
            new SimpleIntegerProperty(0);

    private ServerState() {}

    public static ServerState getInstance() {
        return instance;
    }

    /* ================== GETTERS ================== */

    public ObservableList<String> getLogs() {
        return logs;
    }

    public ObservableList<User> getOnlineUsers() {
        return onlineUsers;
    }

    public IntegerProperty onlineCountProperty() {
        return onlineCount;
    }

    /* ================== SERVER EVENTS ================== */

    public void serverStarted(int port) {
        addLog("🚀 RMI Server started on port " + port);
    }

    public void serverStopped() {
        addLog("⛔ Server stopped");
    }

    public void error(String msg) {
        addLog("❌ ERROR: " + msg);
    }

    /* ================== USER EVENTS ================== */

    public void userOnline(User user) {
        Platform.runLater(() -> {
            if (onlineUsernames.add(user.getUsername())) {
                onlineUsers.add(user);
                onlineCount.set(onlineCount.get() + 1);
                addLog("🟢 " + user.getUsername() + " đăng nhập");
            }
        });
    }

    public void userOffline(User user) {
        Platform.runLater(() -> {
            if (onlineUsernames.remove(user.getUsername())) {
                onlineUsers.removeIf(u ->
                        u.getUsername().equals(user.getUsername())
                );
                onlineCount.set(onlineCount.get() - 1);
                addLog("🔴 " + user.getUsername() + " đăng xuất");
            }
        });
    }

    /* ================== LOG ================== */

    private void addLog(String msg) {
        logs.add("[" + LocalDateTime.now() + "] " + msg);
    }
}

