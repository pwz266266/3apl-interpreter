import java.util.ArrayList;

public class Message {
    private Performative performative;
    private String sender;
    private String receiverID;
    private VpredClause reply;
    private VpredClause body;

    public Message(Performative performative, String sender, String receiverID, VpredClause reply, VpredClause body) {
        this.performative = performative;
        this.sender = sender;
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
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
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
}







