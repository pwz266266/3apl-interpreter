import java.util.ArrayList;

public class Message {
    private Performative performative;
    private String senderID;
    private String receiverID;
    private VpredClause reply;
    private VpredClause body;

    public Message(Performative performative, String senderID, String receiverID, VpredClause reply, VpredClause body) {
        this.performative = performative;
        this.senderID = senderID;
        this.receiverID = receiverID;
        this.reply = reply;
        this.body = body;
    }

    public Performative getPerformative() {
        return performative;
    }

    public void setPerformative(Performative performative) {
        this.performative = performative;
    }

    public String getSender() {
        return senderID;
    }

    public void setSender(String senderID) {
        this.senderID = senderID;
    }

    public String getReceiverID() {
        return receiverID;
    }

    public void setReceiverID(String receiverID) {
        this.receiverID = receiverID;
    }

    public VpredClause getReply() {
        return reply;
    }

    public void setReply(VpredClause reply) {
        this.reply = reply;
    }

    public VpredClause getBody() {
        return body;
    }

    public void setBody(VpredClause body) {
        this.body = body;
    }

    public String receive(){
        return "received("+Performative.toString(this.getPerformative())+","+this.getSender()+","+this.getBody().toString()+","+this.getReply().toString()+").";
    }

    public String toString(){
        return "Message<"+Performative.toString(this.getPerformative())+", Sender:"+this.getSender()+", Receiver:"+this.getReceiverID()+","+this.getBody().toString()+","+this.getReply().toString()+">";
    }
}







