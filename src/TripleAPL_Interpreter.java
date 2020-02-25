import alice.tuprolog.exceptions.MalformedGoalException;
import alice.tuprolog.exceptions.NoSolutionException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

public class TripleAPL_Interpreter {
    public static void main(String args[]) throws ParseException, IOException, MalformedGoalException, NoSolutionException {
        String agentFile1 = "./src/TripAPL/testAgent1.3apl";
        String agentFile2 = "./src/TripAPL/testAgent2.3apl";
        String agentFile3 = "./src/TripAPL/Example.3apl";
        String buyerFile = "./src/TripAPL/buyer.3apl";
        String salerFile = "./src/TripAPL/saler.3apl";
        String cleanerFile = "./src/TripAPL/cleaner.3apl";

        Agent agent1 = TripAPL_parser.compile(buyerFile);
        Agent agent2 = TripAPL_parser.compile(salerFile);
        Agent agent3 = TripAPL_parser.compile(buyerFile);

        ArrayList<Agent> agents1 = new ArrayList<>();
        ArrayList<Agent> agents2 = new ArrayList<>();

        agents1.add(agent1);
        agents2.add(agent2);
        agents2.add(agent3);

        Container container1 = ContainerFactory.createContainer(agents1);
        Container container2 = ContainerFactory.createContainer(agents2);

        ArrayList<Container> containers = new ArrayList<>();
        containers.add(container1);
        containers.add(container2);

        Server server = ServerFactory.createServer(containers);
//        CleanerEnvironment env = new CleanerEnvironment();
//        Cleaner entity = new Cleaner(env,0,0);
//        env.addEntity(entity);
//        server.setEnvironment(env);
//        server.linkAgentEntity(agent1.getFullID(),entity.thisID);
        File logdir = new File("./log");
        if (logdir.exists()){
            delete(logdir);
        }
        logdir.mkdir();
        server.enableDebug("./log");
        server.restart();
        server.setMaxClock(3000);
        new Thread(server).start();
//        env.showGraph();
    }



    private static void delete(File file) throws IOException {

        for (File childFile : file.listFiles()) {

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
}
