import java.util.ArrayList;
import java.util.Date;
public class ContainerFactory {
    static int ID = 0;
    static public Container createContainer(ArrayList<Agent> agents){
        return new Container(agents, ID++);
    }
}
