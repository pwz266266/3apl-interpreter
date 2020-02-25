import alice.tuprolog.*;
import alice.tuprolog.exceptions.MalformedGoalException;
import alice.tuprolog.exceptions.NoSolutionException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.Long;
import java.text.SimpleDateFormat;
import java.util.*;

public class Container implements Runnable{
    public long getID() {
        return ID;
    }
    private final ArrayList<EnvironmentAction> envActions = new ArrayList<>();
    private HashMap<Agent,ArrayList<EnvironmentRespond>> envRespondPool = new HashMap<>();
    private final ArrayList<EnvironmentRespond> envRespondList = new ArrayList<>();
    private Server server;
    private long ID;
    private Boolean Debug = false;
    private File logfile;
    private int clock = 0;
    private ArrayList<Agent> agents;
    private HashMap<Agent, ArrayList<Message>> MessagePool;
    private final ArrayList<Message> messagesToSend;
    private Prolog engine = new Prolog();
    private boolean init = true;
    private boolean terminate = true;
    private State state = State.READY;

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public void terminate(){
        this.terminate = true;
        this.setState(State.FINISHED);
        for(Agent agent: this.agents){
            agent.terminate();
        }
    }

    public void restart(){
        this.terminate = false;
        this.setState(State.READY);
        for(Agent agent: this.agents){
            agent.restart();
        }
    }
    public Container(ArrayList<Agent> agents, long ID){
        this.agents = agents;
        this.ID = ID;
        this.MessagePool = new HashMap<>();
        this.messagesToSend = new ArrayList<>();
        for(Agent agent : agents){
            this.MessagePool.put(agent, new ArrayList<>());
            this.envRespondPool.put(agent, new ArrayList<>());
            agent.setContainer(this);
        }
        this.engine.loadLibrary(new ArithmeticLibrary());
    }

    public void setServer(Server server){
        this.server = server;
    }

    public void oneTick() throws IOException, MalformedGoalException, NoSolutionException {
        FileWriter fw = null;
        if(this.Debug){
            fw = new FileWriter(this.logfile, true);
        }
        if(this.Debug && this.clock == 0){
            Date currentTime = new Date();
            SimpleDateFormat sdf =
                    new SimpleDateFormat("EEE, MMM d, yyyy hh:mm:ss a ");
            sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
            fw.write("Container started @ GMT time: "+sdf.format(currentTime).toString()+"\n");
        }
        if(this.Debug){
            fw.write("-".repeat(70)+"\n");
            fw.write("Container Clock: "+this.clock+"\n");
        }
        StringBuilder bufferActive = new StringBuilder("Active agent list: ");
        StringBuilder bufferSuspend = new StringBuilder("Suspended agent list: ");
        StringBuilder bufferInit = new StringBuilder("Non-initial agent list: ");
        StringBuilder bufferReady = new StringBuilder("Ready agent list: ");
        StringBuilder bufferFinish = new StringBuilder("Finished agent list: ");
        for (Agent agent : agents) {
            if(agent.getState()==State.INIT){
                bufferInit.append(agent.getName()).append(agent.getID()).append(", ");
                agent.initial(this);
            }else if(agent.getState()==State.READY){
                bufferReady.append(agent.getName()).append(agent.getID()).append(", ");
                agent.activate();
                new Thread(agent).start();
            }else if(agent.getState()==State.ACTIVE){
                bufferActive.append(agent.getName()).append(agent.getID()).append(", ");
            }else if(agent.getState()==State.SUSPEND){
                bufferSuspend.append(agent.getName()).append(agent.getID()).append(", ");
            }else{
                bufferFinish.append(agent.getName()).append(agent.getID()).append(", ");
            }

        }
        receiveResponds();
        proceedResponds();
        receiveMessage(fw);
        proceedMessage(fw);
        sendAction();
        if(this.Debug){
            fw.write(bufferInit.toString()+"\n");
            fw.write(bufferReady.toString()+"\n");
            fw.write(bufferActive.toString()+"\n");
            fw.write(bufferSuspend.toString()+"\n");
            fw.write(bufferFinish.toString()+"\n");
            fw.flush();
            fw.close();
        }
        this.clock++;
    }
    public void run() {
        try {
            while(!this.terminate){
                oneTick();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void enableDebug(String logfile)  {
        this.logfile = new File(logfile+"/history.log");
        this.Debug = true;
        File directory = new File(logfile+"/Agents");
        if (! directory.exists()){
            directory.mkdir();
        }
        for(Agent agent : this.agents){
            String fileName = logfile+"/Agents/Agent"+Long.toString(agent.getID()) + ".log";
            agent.enableDebug(fileName);
        }
    }
    public void enableDebug()  {
        this.Debug = true;
        for(Agent agent : this.agents){
            agent.enableDebug();
        }
    }
    public void disableDebug()  {
        this.Debug = false;
        for(Agent agent : this.agents){
            agent.disableDebug();
        }
    }
    public void addAgent(Agent newAgent) throws IOException {
        this.agents.add(newAgent);
        this.MessagePool.put(newAgent, new ArrayList<>());
        this.envRespondPool.put(newAgent, new ArrayList<>());
        newAgent.setContainer(this);
        if(this.Debug){
            FileWriter fw = new FileWriter(this.logfile);
            fw.write("-".repeat(70)+"\n");
            fw.write("Add agent "+newAgent.getID()+" @clock "+this.clock+"\n");
            fw.flush();
            fw.close();
        }
        if(this.server!=null){
            server.addAgent(newAgent);
        }
        if(terminate){
            newAgent.terminate();
        }else{
            newAgent.restart();
        }
    }

    public void sendMessage(Message message) {
        this.server.addMessage(message);
    }

    public void proceedMessage(FileWriter fw) throws NoSolutionException, IOException, MalformedGoalException {
        ArrayList<Message> receivedMessage;
        synchronized (messagesToSend) {
            receivedMessage = new ArrayList<>(this.messagesToSend);
            this.messagesToSend.clear();
        }
        for (Message message : receivedMessage) {
            if (message.getReceiverID().equals("ams")) {
                if (message.getPerformative() == Performative.INFORM) {
                    this.engine.addTheory(new Theory(message.receive()));
                } else if (message.getPerformative() == Performative.QUERY) {
                    SolveInfo info = this.engine.solve("received(inform,Sender," + message.getBody().toString() + "," + message.getReply() + ").");
                    if (info.isSuccess()) {
                        if (fw != null) {
                            fw.write(message.toString() + " Request Succeed.\n");
                        }
                        Env env = new Env();
                        for (Var var : info.getBindingVars()) {
                            env.changeVal(var.getName(), var.getTerm().toString());
                        }
                        VpredClause newBody = (VpredClause) message.getBody().applyEnv(env);
                        Message replyMessage = new Message(Performative.INFORM, env.getVal("Sender"), message.getSender(), message.getReply(), newBody);
                        if (Integer.parseInt(message.getSender().split("_")[1]) == this.getID()) {
                            int agentID = Integer.parseInt(message.getSender().split("_")[2]);
                            for (Agent agent : this.agents) {
                                if (agent.getID() == agentID) {
                                    synchronized (this.MessagePool.get(agent)) {
                                        this.MessagePool.get(agent).add(replyMessage);
                                    }
                                    if (fw != null) {
                                        fw.write("Send " + replyMessage.toString() + " to Agent" + agent.getID() + ".\n");
                                    }
                                    break;
                                }
                            }
                            if (fw != null) {
                                fw.write("Send request " + message.toString() + " to Server.\n");
                            }
                            this.sendMessage(message);
                        } else {
                            if (fw != null) {
                                fw.write("Send " + replyMessage.toString() + " to Server.\n");
                            }
                            this.sendMessage(replyMessage);
                        }
                    } else {
                        if (Integer.parseInt(message.getSender().split("_")[1]) == this.getID()) {
                            if (fw != null) {
                                fw.write("Send " + message.toString() + " to Server.\n");
                            }
                            this.sendMessage(message);
                        }
                    }
                }
            } else if (Integer.parseInt(message.getReceiverID().split("_")[1]) == this.getID()) {
                Agent agent = this.getAgent(Integer.parseInt(message.getReceiverID().split("_")[2]));
                if (agent != null) {
                    if (fw != null) {
                        fw.write("Send " + message.toString() + " to Agent" + agent.getID() + ".\n");
                    }
                    synchronized (this.MessagePool.get(agent)) {
                        this.MessagePool.get(agent).add(message);
                    }
                }
            } else {
                if (fw != null) {
                    fw.write("Send " + message.toString() + " to Server.\n");
                }
                this.sendMessage(message);
            }
        }
    }

    public void receiveMessage(FileWriter fw) throws IOException {
        ArrayList<Message> messages = this.server.forwardMessage(this);
        synchronized (messagesToSend) {
            this.messagesToSend.addAll(messages);
        }
        if(fw!=null){
            for(Message message: messages){
                fw.write("Received "+message.toString()+".\n");
            }
        }
    }

    public void addMessage(Message message) {
        synchronized (messagesToSend) {
            messagesToSend.add(message);
        }
    }

    public ArrayList<Message> forwardMessage(Agent agent){
        ArrayList<Message> messages;
        synchronized (this.MessagePool.get(agent)) {
            messages = new ArrayList<>(this.MessagePool.get(agent));
            this.MessagePool.get(agent).clear();
        }
        return messages;
    }


    public ArrayList<EnvironmentRespond> forwardRespond(Agent agent){
        ArrayList<EnvironmentRespond> responds;
        synchronized (this.envRespondPool.get(agent)) {
            responds = new ArrayList<>(this.envRespondPool.get(agent));
            this.envRespondPool.get(agent).clear();
        }
        return responds;
    }

    private void proceedResponds() {
        ArrayList<EnvironmentRespond> currentResponds;
        synchronized (envRespondList) {
            currentResponds = new ArrayList<>(this.envRespondList);
            this.envRespondList.clear();
        }
        for(EnvironmentRespond respond: currentResponds){
            String agentID = respond.getAgentID();
            int agentid = Integer.parseInt(agentID.split("_")[2]);
            for(Agent agent : agents){
                if(agent.getID() == agentid){
                    synchronized (this.envRespondPool.get(agent)) {
                        this.envRespondPool.get(agent).add(respond);
                    }
                    break;
                }
            }
        }
    }

    public void receiveResponds(){
        synchronized (envRespondList) {
            this.envRespondList.addAll(this.server.forwardResponds(this));
        }
    }

    public Agent getAgent(int id){
        for(Agent agent : agents){
            if(agent.getID() == id){
                return agent;
            }
        }
        return null;
    }

    public void receiveAction(EnvironmentAction envAction) {
        synchronized (envActions) {
            this.envActions.add(envAction);
        }
    }

    public void sendAction(){
        synchronized (envActions) {
            for(EnvironmentAction action : this.envActions){
                this.server.receiveAction(action);
            }
            this.envActions.clear();
        }
    }
    public ArrayList<Agent> getAgents(){
        return this.agents;
    }
}
