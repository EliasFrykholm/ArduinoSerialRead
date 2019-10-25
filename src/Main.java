import javafx.application.Application;
import javafx.stage.Stage;
import sun.net.www.ApplicationLaunchException;

/**
 * Created by Frykiz on 2019-10-25.
 */
public class Main extends Application {
    public static void main(String[] args){
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        new GUI();
    }
}
