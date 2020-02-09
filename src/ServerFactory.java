import java.util.ArrayList;
import java.util.Date;
public class ServerFactory {
    static int ID = 0;
    static public Server createServer(ArrayList<Container> containers){
        return new Server(containers, ID++);
    }
}
