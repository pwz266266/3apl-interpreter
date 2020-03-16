import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;

import java.net.URL;
import java.util.ResourceBundle;

public class StatusController implements Initializable {
    @FXML
    TextArea agentMessages;
    @FXML
    TextArea envActions;
    @FXML
    TextArea envInfo;
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        agentMessages.textProperty().addListener(new ChangeListener<Object>() {
            @Override
            public void changed(ObservableValue<?> observable, Object oldValue,
                                Object newValue) {
                agentMessages.setScrollTop(Double.MAX_VALUE);
            }
        });
        envActions.textProperty().addListener(new ChangeListener<Object>() {
            @Override
            public void changed(ObservableValue<?> observable, Object oldValue,
                                Object newValue) {
                envActions.setScrollTop(Double.MAX_VALUE);
            }
        });
        envInfo.textProperty().addListener(new ChangeListener<Object>() {
            @Override
            public void changed(ObservableValue<?> observable, Object oldValue,
                                Object newValue) {
                envInfo.setScrollTop(Double.MAX_VALUE);
            }
        });
        agentMessages.setWrapText(true);
        envActions.setWrapText(true);
        envInfo.setWrapText(true);
        agentMessages.setEditable(false);
        envActions.setEditable(false);
        envInfo.setEditable(false);
    }
    public void setServer(Server server){
        server.setAgentMessageArea(agentMessages);
        server.setEnvMessageArea(envActions);
        server.setEnvInfoArea(envInfo);
    }
}
