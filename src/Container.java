import alice.tuprolog.*;
import alice.tuprolog.exceptions.MalformedGoalException;
import alice.tuprolog.exceptions.NoSolutionException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.Long;
import java.text.SimpleDateFormat;
import java.util.*;

public class Container {
    public long getID() {
        return ID;
    }

    private Server server;
    private long ID;
    private Boolean Debug = false;
    private File logfile;
    private int clock = 0;
    private ArrayList<Agent> agents;
    private HashMap<Agent, ArrayList<Message>> MessagePool;
    private ArrayList<Message> messagesToSend;
    private Prolog engine = new Prolog();
    public Container(ArrayList<Agent> agents, long ID){
        this.agents = agents;
        this.ID = ID;
        this.MessagePool = new HashMap<>();
        this.messagesToSend = new ArrayList<>();
        for(Agent agent : agents){
            this.MessagePool.put(agent, new ArrayList<>());
            agent.setContainer(this);
        }
        this.engine.loadLibrary(new ArithmeticLibrary());
    }

    public void setServer(Server server){
        this.server = server;
    }

    public void run() throws NoSolutionException, MalformedGoalException, IOException {
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
        String bufferActive = "Active agent list: ";
        String bufferSuspend = "Suspended agent list: ";
        for(Agent agent : agents){
            if(agent.getState()==State.ACTIVE){
                agent.deliberation();
                if(this.Debug){
                    bufferActive += agent.getID() + "(" + agent.getName() + "), ";
                }
            }else if(agent.getState() == State.READY){
                agent.initial(this);
                if(this.Debug){
                    fw.write("Agent "+agent.getID()+"("+agent.getName()+") is initialized.\n");
                }
            }else if(this.Debug){
                bufferSuspend += agent.getID() + "(" + agent.getName() + "), ";
            }
        }
        receiveMessage(fw);
        proceedMessage(fw);
        if(this.Debug){
            fw.write(bufferActive+"\n");
            fw.write(bufferSuspend+"\n");
            fw.flush();
            fw.close();
        }
        this.clock++;
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
        newAgent.setContainer(this);
        if(this.Debug){
            FileWriter fw = new FileWriter(this.logfile);
            fw.write("-".repeat(70)+"\n");
            fw.write("Add agent "+newAgent.getID()+" @clock "+this.clock+"\n");
            fw.flush();
            fw.close();
        }
    }

    public void sendMessage(Message message) {
        this.server.addMessage(message);
    }

    public void proceedMessage(FileWriter fw) throws NoSolutionException, IOException {
        ArrayList<Message> receivedMessage = this.messagesToSend;
        this.messagesToSend = new ArrayList<>();
        for(Message message : receivedMessage){
            if(message.getReceiverID().equals("ams")){
                if(message.getPerformative() == Performative.INFORM){
                    this.engine.addTheory(new Theory(message.receive()));
                }else if(message.getPerformative() == Performative.QUERY){
                    SolveInfo info = this.engine.solve(new Struct("received(inform,Sender,"+message.getBody().toString()+",Reply)"));
                    if(info.isSuccess()){
                        if(fw!=null){
                            fw.write(message.toString()+" Request Succeed.\n");
                        }
                        Env env = new Env();
                        for(Var var : info.getBindingVars()){
                            env.changeVal(var.getName(), var.getTerm().toString());
                        }
                        VpredClause newBody = (VpredClause) message.getBody().applyEnv(env);
                        Message replyMessage = new Message(Performative.INFORM, env.getVal("Sender"), message.getSender(),message.getReply(),newBody);
                        if(Integer.parseInt(message.getSender().split("_")[1]) == this.getID()){
                            int agentID = Integer.parseInt(message.getSender().split("_")[2]);
                            for(Agent agent : this.agents){
                                if(agent.getID() == agentID){
                                    this.MessagePool.get(agent).add(replyMessage);
                                    if(fw!=null) {
                                        fw.write("Send " + replyMessage.toString() + " to Agent" + agent.getID() + ".\n");
                                    }
                                    break;
                                }
                            }
                            if(fw!=null) {
                                fw.write("Send request " + message.toString() + " to Server.\n");
                            }
                            this.sendMessage(message);
                        }else{
                            if(fw!=null) {
                                fw.write("Send " + replyMessage.toString() + " to Server.\n");
                            }
                            this.sendMessage(replyMessage);
                        }
                    }else{
                        if(Integer.parseInt(message.getSender().split("_")[1]) == this.getID()){
                            if(fw!=null) {
                                fw.write("Send " + message.toString() + " to Server.\n");
                            }
                            this.sendMessage(message);
                        }
                    }
                }
            }else if(Integer.parseInt(message.getReceiverID().split("_")[1]) == this.getID()){
                Agent agent = this.getAgent(Integer.parseInt(message.getReceiverID().split("_")[2]));
                if(agent!=null){
                    if(fw!=null) {
                        fw.write("Send " + message.toString() + " to Agent"+agent.getID()+".\n");
                    }
                    this.MessagePool.get(agent).add(message);
                }
            }
            else{
                if(fw!=null) {
                    fw.write("Send " + message.toString() + " to Server.\n");
                }
                this.sendMessage(message);
            }
        }
    }

    public void receiveMessage(FileWriter fw) throws IOException {
        ArrayList<Message> messages = this.server.forwardMessage(this);
        this.messagesToSend.addAll(messages);
        if(fw!=null){
            for(Message message: messages){
                fw.write("Received "+message.toString()+".\n");
            }
        }
    }

    public void addMessage(Message message) {
        messagesToSend.add(message);
    }

    public ArrayList<Message> forwardMessage(Agent agent){
        ArrayList<Message> messages = new ArrayList(this.MessagePool.get(agent));
        this.MessagePool.get(agent).clear();
        return messages;
    }

    public Agent getAgent(int id){
        for(Agent agent : agents){
            if(agent.getID() == id){
                return agent;
            }
        }
        return null;
    }
}
