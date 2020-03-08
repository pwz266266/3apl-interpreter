import java.util.ArrayList;
import java.util.HashMap;

public abstract class Environment implements Runnable{
    HashMap<Integer, ControllableEntity> entities = new HashMap<>();
    final ArrayList<EnvironmentAction> requestBuffer = new ArrayList<>();
    ArrayList<EnvironmentAction> handlingRequest = new ArrayList<>();
    final ArrayList<EnvironmentRespond> respondList = new ArrayList<>();
    EnvironmentInterface envInter;
    State state = State.READY;
    boolean terminate = true;

    public void setEnvInter(EnvironmentInterface envInter){
        this.envInter = envInter;
    }

    public void terminate(){
        state = State.FINISHED;
        this.terminate = true;
    }

    public void restart(){
        state = State.READY;
        this.terminate = false;
    }
    public void addEntity(ControllableEntity entity){
        entities.put(entity.getID(), entity);
    }

    public void receiveRequest(EnvironmentAction envAction){
        synchronized (requestBuffer) {
            this.requestBuffer.add(envAction);
        }
    }

    public ArrayList<EnvironmentRespond> sendRespond(){
        ArrayList<EnvironmentRespond> responds;
        synchronized (respondList) {
            responds = new ArrayList<>(respondList);
            respondList.clear();
        }
        return responds;
    }

    public void storeRespond(EnvironmentRespond respond){
        synchronized (respondList) {
            this.respondList.add(respond);
        }
    }

    public void startHandling(){
        synchronized (requestBuffer) {
            handlingRequest.addAll(requestBuffer);
            requestBuffer.clear();
        }
    }

    abstract public void proceedAction(EnvironmentAction action);

    abstract  public void showGUI();

    abstract public HashMap<String, ArrayList<String>> CreatableEntity();

    abstract public int createEntity(String type, ArrayList<String> parameter);

}