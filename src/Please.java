import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class Please {

    @FXML
    private TextField ttitle;

    @FXML
    void OnBtnClicked(ActionEvent event) {
        
        Stage mainWindow = (Stage) ttitle.getScene().getWindow();
        String title = ttitle.getText();
        mainWindow.setTitle(title);
    }

}
