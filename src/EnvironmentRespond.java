import java.util.ArrayList;

public class EnvironmentRespond {
    private String agentID;
    private int actionID;
    private ArrayList<Literal> postCondition;
    private boolean success;

    public EnvironmentRespond(String agentID, ArrayList<Literal> postCondition, boolean success, int actionID){
        this.actionID = actionID;
        this.agentID = agentID;
        this.postCondition = postCondition;
        this.success = success;
    }


    public String getAgentID() {
        return agentID;
    }

    public int getActionID() {
        return actionID;
    }

    public ArrayList<Literal> getPostCondition() {
        return postCondition;
    }

    public boolean isSuccess() {
        return success;
    }

}
