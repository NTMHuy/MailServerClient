package server.ui;

import common.User;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

public class ServerController {

    @FXML
    private Label lblOnline;

    @FXML
    private TableView<User> userTable;

    @FXML
    private TableColumn<User, String> colUser;

    @FXML
    private TableColumn<User, String> colPhone;

    @FXML
    private TableColumn<User, String> colStatus;

    @FXML
    private ListView<String> logList;

    private final ServerState state = ServerState.getInstance();

    @FXML
    public void initialize() {

        lblOnline.textProperty()
                .bind(state.onlineCountProperty().asString());

        colUser.setCellValueFactory(
                new PropertyValueFactory<>("username")
        );

        colPhone.setCellValueFactory(
                new PropertyValueFactory<>("phone")
        );

        colStatus.setCellValueFactory(
                new PropertyValueFactory<>("status")
        );

        userTable.setItems(state.getOnlineUsers());
        logList.setItems(state.getLogs());
    }
}
