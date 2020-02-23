import java.util.ArrayList;
import java.util.Date;
public class ServerFactory {
    static int ID = 0;
    static public Server createServer(){
        return new Server(new ArrayList<Container>(), ID++, null);
    }

    static public Server createServer(ArrayList<Container> containers){
        return new Server(containers, ID++, null);
    }

    static public Server createServer(ArrayList<Container> containers, EnvironmentInterface environmentInterface){
        return new Server(containers, ID++, environmentInterface);
    }
}
