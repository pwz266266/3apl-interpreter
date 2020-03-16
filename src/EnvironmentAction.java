import java.util.ArrayList;

public class EnvironmentAction {
    private String agentID;
    private String actionName;
    private int actionID;
    private ArrayList<String> arguments;
    private int entityID;

    public EnvironmentAction(String agentID, String actionName, ArrayList<String> arguments, int actionID){
        this.actionName = actionName;
        this.agentID = agentID;
        this.arguments = arguments;
        this.actionID = actionID;
    }


    public String getAgentID() {
        return agentID;
    }

    public String getActionName() {
        return actionName;
    }

    public int getActionID() {
        return actionID;
    }

    public ArrayList<String> getArguments() {
        return arguments;
    }

    public void setEntityID(int id){
        this.entityID = id;
    }

    public int getEntityID(){
        return this.entityID;
    }

    private String getStringArgs(){
        StringBuilder result = new StringBuilder("(");
        String prefix = "";
        for(String arg:this.arguments){
            result.append(prefix);
            result.append(arg);
            prefix = ", ";
        }
        result.append(")");
        return result.toString();
    }

    @Override
    public String toString(){
        return "EnvAction< Action ID: "+actionID+", Agent ID: "+agentID+", "+actionName+getStringArgs()+">";
    }
}
