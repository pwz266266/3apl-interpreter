import java.util.ArrayList;
import java.util.HashMap;

public class EnvironmentInterface {
    Environment environment;
    Server server;
    HashMap<String, Integer> agentMap = new HashMap<>();
    HashMap<Integer, String> entityMap = new HashMap<>();


    public EnvironmentInterface(Environment env, Server server){
        this.environment = env;
        this.server = server;
    }

    public void setServer(Server server){
        this.server = server;
    }

    public void reset(Environment env){
        this.environment = env;
        agentMap = new HashMap<>();
    }

    public void link(String agentID, Integer entityID){
        this.agentMap.put(agentID,entityID);
        this.entityMap.put(entityID,agentID);
    }

    public void unlink(String agentID, Integer entityID){
        this.agentMap.put(agentID, null);
        this.entityMap.put(entityID,null);
    }


    public void forwardEnvActions(ArrayList<EnvironmentAction> actions){
        for(EnvironmentAction action: actions){
            action.setEntityID(agentMap.get(action.getAgentID()));
            this.environment.receiveRequest(action);
        }
        actions.clear();
    }

    public ArrayList<EnvironmentRespond> receiveResponds() {
        ArrayList<EnvironmentRespond> responds = this.environment.sendRespond();
        for(EnvironmentRespond respond : responds){
            if(respond.getActionID() == -1){
                respond.setAgentID(entityMap.get(respond.getEntityID()));
            }
        }
        return responds;
    }
}
