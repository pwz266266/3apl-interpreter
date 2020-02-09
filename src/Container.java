import alice.tuprolog.exceptions.MalformedGoalException;
import alice.tuprolog.exceptions.NoSolutionException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

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
    private ArrayList<Message> messagesToSent;
    private DirectoryFacilitator DF;

    public Container(ArrayList<Agent> agents, long ID){
        this.agents = agents;
        this.ID = ID;
        this.MessagePool = new HashMap<>();
        this.messagesToSent = new ArrayList<>();
        this.DF = new DirectoryFacilitator();
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
        proceedMessage();
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
        if(this.Debug){
            fw.write(bufferActive+"\n");
            fw.write(bufferSuspend+"\n");
            fw.flush();
            fw.close();
        }
//        this.server.forwardMessage(this);
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
        newAgent.setContainer(this);
        if(this.Debug){
            FileWriter fw = new FileWriter(this.logfile);
            fw.write("-".repeat(70)+"\n");
            fw.write("Add agent "+newAgent.getID()+" @clock "+this.clock+"\n");
            fw.flush();
            fw.close();
        }
    }

    public void sendMessage(){

    }

    public void proceedMessage(){

    }

    public void addMessage(Message message){
        message.setSender("Container"+this.getID()+"_"+message.getSender());
        messagesToSent.add(message);
    }

    public ArrayList<Message> forwardMessage(Agent agent){
        ArrayList<Message> messages = new ArrayList(this.MessagePool.get(agent));
        this.MessagePool.get(agent).clear();
        return messages;
    }
}


class DirectoryFacilitator{
    
}