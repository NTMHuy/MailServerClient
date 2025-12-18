package server.rmi;

import common.rmi.MailRemote;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import server.ui.ServerState;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class RMIServerMain extends Application {

    @Override
    public void start(Stage stage) {
        try {
            /* ========== 1. Start RMI Registry ========== */
            Registry registry = LocateRegistry.createRegistry(1099);

            /* ========== 2. Bind Mail Service ========== */
            MailRemote mailRemote = new MailRemoteImpl();
            registry.rebind("MailService", mailRemote);

            /* ========== 3. Update Server State ========== */
            server.ui.ServerState.getInstance().serverStarted(1099);

            /* ========== 4. Load Server UI ========== */
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/view/server.fxml")
            );

            Scene scene = new Scene(loader.load());
            stage.setTitle("TH Mail - RMI Server Manager");
            stage.setScene(scene);
            stage.setResizable(false);
            stage.show();

            System.out.println("✅ RMI Server running on port 1099");

        } catch (Exception e) {
            server.ui.ServerState.getInstance().error(e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {
        server.ui.ServerState.getInstance().serverStopped();
        System.out.println("⛔ Server stopped");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
