import java.util.ArrayList;
import java.util.HashMap;

public abstract class Environment {
    private HashMap<Integer, ControllableEntity> entities = new HashMap<>();
    private ArrayList<EnvironmentAction> requestBuffer = new ArrayList<>();
    private ArrayList<EnvironmentAction> handlingRequest = new ArrayList<>();
    private ArrayList<EnvironmentRespond> respondList = new ArrayList<>();

    public Environment() {
        System.out.println("Create an environment!!!");
    }

    abstract void run();

    public void loop(){
        startHandling();
        run();
    }

    public void receiveRequest(EnvironmentAction envAction){
        this.requestBuffer.add(envAction);
    }

    public ArrayList<EnvironmentRespond> sendRespond(){
        ArrayList<EnvironmentRespond> responds = new ArrayList<>(respondList);
        respondList.clear();
        return responds;
    }

    public void storeRespond(EnvironmentRespond respond){
        this.respondList.add(respond);
    }

    public void startHandling(){
        handlingRequest.addAll(requestBuffer);
        requestBuffer.clear();
        for(EnvironmentAction action: handlingRequest){
            entities.get(action.getEntityID()).takeAction(action);
        }
    }
}