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

        Agent agent1 = TripAPL_parser.compile(agentFile1);
        Agent agent2 = TripAPL_parser.compile(agentFile2);
        Agent agent3 = TripAPL_parser.compile(agentFile3);

        ArrayList<Agent> agents1 = new ArrayList<>();
        ArrayList<Agent> agents2 = new ArrayList<>();

        agents1.add(agent1);
        agents1.add(agent2);
        agents2.add(agent3);

        Container container1 = ContainerFactory.createContainer(agents1);
        Container container2 = ContainerFactory.createContainer(agents2);

        ArrayList<Container> containers = new ArrayList<>();
        containers.add(container1);

        Server server = ServerFactory.createServer(containers);
        File logdir = new File("./log");
        if (logdir.exists()){
            delete(logdir);
        }
        logdir.mkdir();
        server.enableDebug("./log");
        for(int i = 0; i<=100; i++){
            if(i == 34){
                server.addContainer(container2);
            }
            server.run();
        }
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
