import alice.tuprolog.exceptions.MalformedGoalException;
import alice.tuprolog.exceptions.NoSolutionException;

import java.io.IOException;
import java.util.ArrayList;

public class RuntimeManager {
    private ArrayList<Server> serverList;
    public RuntimeManager(ArrayList<Server> servers){
        this.serverList = servers;
    }

    public void addServer(Server newServer){
        this.serverList.add(newServer);
    }

    public void run() throws NoSolutionException, MalformedGoalException, IOException {
        for(Server server: this.serverList){
            server.run();
        }
    }
}
