import alice.tuprolog.exceptions.MalformedGoalException;
import alice.tuprolog.exceptions.NoSolutionException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TimeZone;
import java.util.Date;

public class Server {
    private File logfile;


    private long ID;
    private Boolean Debug = false;
    private ArrayList<Container> containers;
    private int clock = 0;
    private HashMap<Container, ArrayList<Message>> messagePool;
    private HashMap<Container, ArrayList<EnvironmentRespond>> envRespondPool;
    private EnvironmentInterface environmentInter;
    private ArrayList<Agent> agents = new ArrayList<>();
    public void addMessage(Message message) {
        this.messageToSend.add(message);
    }
    private ArrayList<EnvironmentAction> envActions;
    private ArrayList<EnvironmentRespond> envResponds;
    private ArrayList<Message> messageToSend;
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
    public void run() throws NoSolutionException, MalformedGoalException, IOException {
        environmentInter.environment.loop();
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
            container.run();
            if(this.Debug){
                buffer += container.getID();
                buffer += ", ";
            }
        }
        proceedMessage(fw);
        proceedResponds();
        forwardActions();
        receiveRespond();
        if(this.Debug){
            fw.write(buffer+"\n");
            fw.flush();
            fw.close();
        }
        if(this.clock % 10 == 0){
            System.out.println("Server"+this.getID()+" reaches clock " +this.clock+".");
        }
        this.clock++;
    }

    private void receiveRespond() {
        this.envResponds.addAll(this.environmentInter.receiveResponds());
    }

    public void addContainer(Container newContainer) throws IOException {
        this.containers.add(newContainer);
        this.agents.addAll(newContainer.getAgents());
        messagePool.put(newContainer, new ArrayList<>());
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

    public void addAgent(Agent agent){
        this.agents.add(agent);
    }

    public ArrayList<Message> forwardMessage(Container container){
        ArrayList<Message> messages = new ArrayList(this.messagePool.get(container));
        this.messagePool.get(container).clear();
        return messages;
    }

    public ArrayList<EnvironmentRespond> forwardResponds(Container container){
        ArrayList<EnvironmentRespond> responds = new ArrayList(this.envRespondPool.get(container));
        this.envRespondPool.get(container).clear();
        return responds;
    }

    public void forwardActions(){
        if(this.environmentInter!=null){
            this.environmentInter.forwardEnvActions(this.envActions);
        }
    }


    private void proceedResponds() {
        ArrayList<EnvironmentRespond> currentResponds = new ArrayList<>(this.envResponds);
        this.envResponds.clear();
        for(EnvironmentRespond respond: currentResponds){
            String ContainerID = respond.getAgentID();
            int containerid = Integer.parseInt(ContainerID.split("_")[1]);
            for(Container container : containers){
                if(container.getID() == containerid){
                    this.envRespondPool.get(container).add(respond);
                    break;
                }
            }
        }
    }


    public long getID() {
        return ID;
    }

    public void proceedMessage(FileWriter fw) throws IOException {
        ArrayList<Message> receivedMessages = this.messageToSend;
        this.messageToSend = new ArrayList<>();
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
                        this.messagePool.get(container).add(message);
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
                        this.messagePool.get(container).add(message);
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
        }else{
            this.environmentInter.reset(env);
        }
    }

    public void receiveAction(EnvironmentAction envAction){
        if(this.environmentInter == null){
            System.out.println("WARNING: No environment associated with current server!");
        }else{
            this.envActions.add(envAction);
        }
    }

    public ArrayList<EnvironmentAction> getEnvActions(){
        return this.envActions;
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
}
