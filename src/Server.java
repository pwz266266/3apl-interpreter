import alice.tuprolog.exceptions.MalformedGoalException;
import alice.tuprolog.exceptions.NoSolutionException;
import javafx.scene.control.TextArea;
import javafx.scene.text.Text;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TimeZone;
import java.util.Date;

import static java.lang.Thread.sleep;

public class Server implements Runnable{
    private File logfile;


    private long ID;
    private Boolean Debug = false;
    private ArrayList<Container> containers;
    private int clock = 0;
    private int maxClock = -1;
    private HashMap<Container, ArrayList<Message>> messagePool;
    private HashMap<Container, ArrayList<EnvironmentRespond>> envRespondPool;
    private EnvironmentInterface environmentInter;
    private ArrayList<Agent> agents = new ArrayList<>();
    public void addMessage(Message message) {
        synchronized (messageToSend) {
            this.messageToSend.add(message);
        }
    }
    private final ArrayList<EnvironmentAction> envActions;
    private final ArrayList<EnvironmentRespond> envResponds;
    private final ArrayList<Message> messageToSend;
    private boolean terminate = true;
    private TextArea agentMessageArea = null;
    private TextArea envMessageArea = null;
    private TextArea envInfoArea = null;
    private final StringBuilder agentMesBuffer = new StringBuilder();
    private final StringBuilder envMesBuffer = new StringBuilder();
    private final StringBuilder envInfoBuffer = new StringBuilder();
    public void terminate(){
        this.terminate = true;
        for(Container container: containers){
            container.terminate();
        }
        if(environmentInter!=null){
            environmentInter.environment.terminate();
        }
    }

    public void updateTextArea(){
        if(envInfoArea!=null){
            this.envInfoArea.appendText(envInfoBuffer.toString());
            envInfoBuffer.delete(0,envInfoBuffer.length());
            this.agentMessageArea.appendText(agentMesBuffer.toString());
            agentMesBuffer.delete(0,agentMesBuffer.length());
            this.envMessageArea.appendText(envMesBuffer.toString());
            envMesBuffer.delete(0,envMesBuffer.length());
        }
    }

    public void AddToEnvInfoArea(int clock, String message) {
        if (envInfoArea != null) {
            synchronized (envInfoBuffer) {
                this.envInfoBuffer.append("Environment tick " + clock + ": " + message + "\n");
            }
        }
    }
    public void AddToAgentMessageArea(Message message) {
        if (agentMessageArea != null) {
            synchronized (agentMesBuffer) {
                this.agentMesBuffer.append("Server tick " + clock + ": " + message.toString() + "\n");
            }
        }
    }

    public void AddToEnvMessageArea(EnvironmentRespond message) {
        if (envMessageArea != null) {
            synchronized (envMesBuffer) {
                this.envMesBuffer.append("Server tick " + clock + ": " + message.toString() + "\n");
            }
        }
    }

    public void AddToEnvMessageArea(EnvironmentAction message) {
        if (envMessageArea != null) {
            synchronized (envMesBuffer) {
                this.envMesBuffer.append("Server tick " + clock + ": " + message.toString() + "\n");
            }
        }
    }
    public void setAgentMessageArea(TextArea agentMessageArea) {
        this.agentMessageArea = agentMessageArea;
    }

    public void setEnvMessageArea(TextArea envMessageArea){
        this.envMessageArea = envMessageArea;
    }

    public void setEnvInfoArea(TextArea envInfoArea){
        this.envInfoArea = envInfoArea;
    }

    public void restart(){
        this.terminate = false;
        if(environmentInter!=null){
            environmentInter.environment.restart();
        }
        for(Container container: containers){
            container.restart();
        }
    }
    public Server(ArrayList<Container> containers, long ID, EnvironmentInterface environmentInter){
        this.envActions = new ArrayList<>();
        this.envResponds = new ArrayList<>();
        this.ID = ID;
        this.containers = containers;
        messagePool = new HashMap<>();
        envRespondPool = new HashMap<>();
        messageToSend = new ArrayList<>();
        for(Container container : containers){
            this.messagePool.put(container, new ArrayList<>());
            this.envRespondPool.put(container, new ArrayList<>());
            container.setServer(this);
        }
        this.environmentInter = environmentInter;
        if(environmentInter != null){
            this.environmentInter.setServer(this);
        }
    }

    public boolean hasEnv(){
        return environmentInter!=null;
    }

    public int getMaxClock() {
        return maxClock;
    }

    public void setMaxClock(int maxClock) {
        this.maxClock = maxClock;
    }

    public void enableDebug(String logfile)  {
        File logdir = new File(logfile+"/Server"+this.getID());
        if (! logdir.exists()){
            logdir.mkdir();
        }
        logfile = logdir.toString();
        this.logfile = new File(logfile+"/history.log");
        this.Debug = true;
        File directory = new File(logfile+"/Containers");
        if (! directory.exists()){
            directory.mkdir();
        }
        for(Container container : this.containers){
            String folderName = logfile+"/Containers/Container"+Long.toString(container.getID());
            directory = new File(folderName);
            if (! directory.exists()){
                directory.mkdir();
            }
            container.enableDebug(folderName);
        }
    }
    public void enableDebug()  {
        this.Debug = true;
        for(Container container : this.containers){
            container.enableDebug();
        }
    }
    public void disableDebug() {
        this.Debug = false;
        for(Container container : this.containers){
            container.disableDebug();
        }
    }


    public void oneTick() throws IOException {
        if(environmentInter != null && environmentInter.environment.state == State.READY){
            environmentInter.environment.state = State.ACTIVE;
            new Thread(environmentInter.environment).start();
        }
        synchronized (messageToSend) {
            for (Message message : this.messageToSend) {
                AddToAgentMessageArea(message);
            }
        }
        FileWriter fw = null;
        if(this.Debug){
            fw = new FileWriter(this.logfile, true);
        }
        if(this.Debug && this.clock == 0){
            Date currentTime = new Date();
            SimpleDateFormat sdf =
                    new SimpleDateFormat("EEE, MMM d, yyyy hh:mm:ss a ");
            sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
            fw.write("Server started @ GMT time: "+sdf.format(currentTime).toString()+"\n");
        }


        if(this.Debug){
            fw.write("-".repeat(70)+"\n");
            fw.write("Server Clock: "+this.clock+"\n");
        }
        String buffer = "Active containers: ";

        for(Container container: this.containers){
            if(container.getState() == State.READY){
                container.setState(State.ACTIVE);
                new Thread(container).start();
            }else if(container.getState() == State.ACTIVE){
                if (this.Debug) {
                    buffer += container.getID();
                    buffer += ", ";
                }
            }
        }
        proceedMessage(fw);
        if(environmentInter != null) {
            proceedResponds();
            forwardActions();
            receiveRespond();
        }
        if(this.Debug){
            fw.write(buffer+"\n");
            fw.flush();
            fw.close();
        }
//        if(this.clock % 1000 == 0){
//            System.out.println("Server"+this.getID()+" reaches clock " +this.clock+".");
//        }

        this.clock++;
    }
    @Override
    public void run(){
//        if(!this.Debug){
//            this.enableDebug("./log");
//        }
        try {
            while(!this.terminate){
                oneTick();
                sleep(1);
                if(clock%1000==0){
                    updateTextArea();
                }
                if(maxClock!=-1 && clock >= maxClock){
                    terminate();
                }else if(this.environmentInter!=null && this.environmentInter.environment.terminate){
                    this.terminate();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void receiveRespond() {
        synchronized (this.envResponds) {
            this.envResponds.addAll(this.environmentInter.receiveResponds());
        }
    }

    public void addContainer(Container newContainer) throws IOException {
        this.containers.add(newContainer);
        this.agents.addAll(newContainer.getAgents());
        messagePool.put(newContainer, new ArrayList<>());
        envRespondPool.put(newContainer, new ArrayList<>());
        newContainer.setServer(this);
        if(this.Debug){
            FileWriter fw = new FileWriter(this.logfile, true);
            fw.write("-".repeat(70)+"\n");
            fw.write("Add container "+newContainer.getID()+" @clock "+this.clock+"\n");
            fw.flush();
            fw.close();
            String folderName = this.logfile.toString().substring(0,this.logfile.toString().length()-12)+"/Containers/Container"+Long.toString(newContainer.getID());
            File directory = new File(folderName);
            if (! directory.exists()){
                directory.mkdir();
            }
            newContainer.enableDebug(folderName);
        }
    }

    public void removeContainer(Container container){
        containers.remove(container);
        messagePool.remove(container);
        envRespondPool.remove(container);
        for(Agent agent: container.getAgents()){
            removeAgent(agent);
        }
    }

    public void addAgent(Agent agent){
        this.agents.add(agent);
    }

    public void removeAgent(Agent agent){
        this.agents.remove(agent);
        if(this.environmentInter!=null){
            this.environmentInter.removeAgent(agent);
        }
    }

    public ArrayList<Message> forwardMessage(Container container){
        ArrayList<Message> messages;
        synchronized (this.messagePool.get(container)) {
            messages = new ArrayList<>(this.messagePool.get(container));
            this.messagePool.get(container).clear();
        }
        return messages;
    }

    public ArrayList<EnvironmentRespond> forwardResponds(Container container){
        ArrayList<EnvironmentRespond> responds;
        synchronized (this.envRespondPool.get(container)) {
            responds = new ArrayList<>(this.envRespondPool.get(container));
            this.envRespondPool.get(container).clear();
        }
        return responds;
    }

    public void forwardActions(){
        if(this.environmentInter!=null) {
            synchronized (this.envActions) {
                this.environmentInter.forwardEnvActions(this.envActions);
                this.envActions.clear();
            }
        }
    }


    private void proceedResponds() {
        ArrayList<EnvironmentRespond> currentResponds;
        synchronized (envResponds) {
            currentResponds = new ArrayList<>(this.envResponds);
            this.envResponds.clear();
        }
        for(EnvironmentRespond respond: currentResponds){
            AddToEnvMessageArea(respond);
            String ContainerID = respond.getAgentID();
            int containerid = Integer.parseInt(ContainerID.split("_")[1]);
            for(Container container : containers){
                if(container.getID() == containerid){
                    synchronized (this.envRespondPool.get(container)) {
                        this.envRespondPool.get(container).add(respond);
                    }
                    break;
                }
            }
        }
    }


    public long getID() {
        return ID;
    }

    public void proceedMessage(FileWriter fw) throws IOException {
        ArrayList<Message> receivedMessages;
        synchronized (messageToSend) {
            receivedMessages = new ArrayList<>(this.messageToSend);
            this.messageToSend.clear();
        }
        if(fw!=null){
            for(Message message : receivedMessages){
                fw.write("Received "+message.toString()+" from Containers.\n");
            }
            fw.write("\n");
        }
        for(Message message : receivedMessages){
            if(message.getReceiverID().equals("ams")){
                int containerID = Integer.parseInt(message.getSender().split("_")[1]);
                for(Container container : this.containers){
                    if(container.getID() != containerID){
                        synchronized (this.messagePool.get(container)) {
                            this.messagePool.get(container).add(message);
                        }
                        if(fw!=null){
                            fw.write("Distribute " + message.toString() + " to Container"+container.getID()+".\n");
                        }
                        break;
                    }
                }
            }else{
                int containerID = Integer.parseInt(message.getReceiverID().split("_")[1]);
                for(Container container : this.containers){
                    if(container.getID() == containerID){
                        synchronized (this.messagePool.get(container)) {
                            this.messagePool.get(container).add(message);
                        }
                        if(this.Debug){
                            fw.write("Send " + message.toString() + " to Container"+container.getID()+".\n");
                        }
                        break;
                    }
                }
            }
        }
    }
    public void setEnvironment(Environment env){
        if(env == null){
            this.environmentInter = null;
        }else if(this.environmentInter == null){
            this.environmentInter = new EnvironmentInterface(env, this);
            env.setEnvInter(environmentInter);
        }else{
            this.environmentInter.reset(env);
            env.setEnvInter(environmentInter);
        }
    }

    public void showEnv(){
        if(this.environmentInter != null){
            this.environmentInter.environment.showGUI();
        }
    }

    public void receiveAction(EnvironmentAction envAction){
        if(this.environmentInter == null){
            System.out.println("WARNING: No environment associated with current server!");
        }else{
            synchronized (envActions) {
                AddToEnvMessageArea(envAction);
                this.envActions.add(envAction);
            }
        }
    }


    public void linkAgentEntity(String agentID, int entityID){
        if(this.environmentInter == null){
            System.out.println("WARNING: No environment associated with current server!");
        }else{
            this.environmentInter.link(agentID, entityID);
        }
    }

    public void unlinkAgentEntity(String agentID, int entityID){
        if(this.environmentInter == null){
            System.out.println("WARNING: No environment associated with current server!");
        }else{
            this.environmentInter.unlink(agentID, entityID);
        }
    }

    public HashMap<String, ArrayList<String>> CreatableEntity() {
        if(this.environmentInter == null){
            System.out.println("WARNING: No environment associated with current server!");
        }else{
            return this.environmentInter.environment.CreatableEntity();
        }
        return null;
    }

    public int createEntity(String type, ArrayList<String> parameter) {
        if(this.environmentInter == null){
            System.out.println("WARNING: No environment associated with current server!");
        }else{
            return this.environmentInter.environment.createEntity(type, parameter);
        }
        return -2;
    }
}
