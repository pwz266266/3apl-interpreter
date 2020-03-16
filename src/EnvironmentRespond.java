import java.util.ArrayList;

public class EnvironmentRespond {
    private String agentID;
    private int actionID;
    private ArrayList<GpredClause> postCondition;
    private boolean success;
    private int entityID;

    public EnvironmentRespond(String agentID, ArrayList<GpredClause> postCondition, boolean success, int actionID, int entityID){
        this.actionID = actionID;
        this.agentID = agentID;
        this.postCondition = postCondition;
        this.success = success;
        this.entityID = entityID;
    }


    public String getAgentID() {
        return agentID;
    }

    public int getActionID() {
        return actionID;
    }

    public ArrayList<GpredClause> getPostCondition() {
        return postCondition;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setAgentID(String agentID) {
        this.agentID = agentID;
    }

    public int getEntityID() {
        return entityID;
    }

    @Override
    public String toString(){
        return "EnvRespond< Agent: "+agentID +", Entity: "+entityID+", Action ID: "+actionID+", "+(success?"Success>":"Failed>");
    }
}
