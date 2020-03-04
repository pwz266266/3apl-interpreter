import alice.tuprolog.exceptions.MalformedGoalException;
import alice.tuprolog.exceptions.NoSolutionException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class TripleAPL_Interpreter extends Application{
    private static Server server;
    private Stage stage;
    public static void main(String args[]) throws ParseException, IOException {
        File logdir = new File("./log");
        delete(logdir);
        launch(args);
    }



    static void delete(File file) throws IOException {

        for (File childFile : Objects.requireNonNull(file.listFiles())) {

            if (childFile.isDirectory()) {
                delete(childFile);
            } else {
                if (!childFile.delete()) {
                    throw new IOException();
                }
            }
        }

        if (!file.delete()) {
            throw new IOException();
        }
    }

    @Override
    public void start(Stage stage) throws Exception {
        new FirstStage();
    }
}


class FirstStage extends Stage{
    Button openOther = new Button("Open other Stage");
    HBox x = new HBox();
    FirstStage(){
        x.getChildren().add(openOther);
        this.setScene(new Scene(x, 300, 300));
        this.show();

        openOther.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                String agentFile1 = "./src/TripAPL/testAgent1.3apl";
                String agentFile2 = "./src/TripAPL/testAgent2.3apl";
                String agentFile3 = "./src/TripAPL/Example.3apl";
                String buyerFile = "./src/TripAPL/buyer.3apl";
                String salerFile = "./src/TripAPL/saler.3apl";
                String cleanerFile = "./src/TripAPL/cleaner.3apl";
                String pathFinder = "./src/TripAPL/pathFinder.3apl";
                Agent agent1 = null;
                try {
                    agent1 = TripAPL_parser.compile(cleanerFile);
                } catch (FileNotFoundException | ParseException e) {
                    e.printStackTrace();
                }
//        Agent agent2 = TripAPL_parser.compile(salerFile);
//        Agent agent3 = TripAPL_parser.compile(buyerFile);

                ArrayList<Agent> agents1 = new ArrayList<>();
//        ArrayList<Agent> agents2 = new ArrayList<>();

                agents1.add(agent1);
//        agents2.add(agent2);
//        agents2.add(agent3);

                Container container1 = ContainerFactory.createContainer(agents1);
//        Container container2 = ContainerFactory.createContainer(agents2);

                ArrayList<Container> containers = new ArrayList<>();
                containers.add(container1);
//        containers.add(container2);

                Server server = ServerFactory.createServer(containers);
                server.showEnv();

                CleanerEnvironment env = new CleanerEnvironment();
                server.setEnvironment(env);
                server.linkAgentEntity(agent1.getFullID(),server.createEntity("Cleaner (Controllable)", new ArrayList<>(Arrays.asList("0", "0"))));
                for(int i = 0; i<=10; i++){
                    server.createEntity("Garbage", new ArrayList<>(Arrays.asList(String.valueOf((int)(Math.random()*200)), String.valueOf((int)(Math.random()*200)))));
                }
                for(int i = 0; i<=3; i++){
                    server.createEntity("Bin", new ArrayList<>(Arrays.asList(String.valueOf((int)(Math.random()*200)), String.valueOf((int)(Math.random()*200)))));
                }
                File logdir = new File("./log");
                logdir.mkdir();
                server.enableDebug("./log");
                server.restart();
                server.setMaxClock(1000000);
                server.showEnv();
                new Thread(server).start();
            }//end action
        });
    }
}