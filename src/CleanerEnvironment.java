import java.util.ArrayList;

public class CleanerEnvironment extends Environment {
    private ArrayList<Garbage> garbageList = new ArrayList<>();
    private ArrayList<Bin> binList = new ArrayList<>();

    public void showGraph(){
        for(Garbage garbage : garbageList){
            System.out.println("Garbage at ("+garbage.getX()+","+garbage.getY()+").");
        }
        for(Bin bin : binList){
            System.out.println("Bin at ("+bin.getX()+","+bin.getY()+").");
        }
    }
    public CleanerEnvironment() {
        super();
        garbageList.add(new Garbage((int)(Math.random() * 20 + 1),(int)(Math.random() * 20 + 1)));
        garbageList.add(new Garbage((int)(Math.random() * 20 + 1),(int)(Math.random() * 20 + 1)));
        garbageList.add(new Garbage((int)(Math.random() * 20 + 1),(int)(Math.random() * 20 + 1)));
        garbageList.add(new Garbage((int)(Math.random() * 20 + 1),(int)(Math.random() * 20 + 1)));
        garbageList.add(new Garbage((int)(Math.random() * 20 + 1),(int)(Math.random() * 20 + 1)));
        garbageList.add(new Garbage((int)(Math.random() * 20 + 1),(int)(Math.random() * 20 + 1)));
        garbageList.add(new Garbage((int)(Math.random() * 20 + 1),(int)(Math.random() * 20 + 1)));
        binList.add(new Bin((int)(Math.random() * 20 + 1),(int)(Math.random() * 20 + 1)));
        binList.add(new Bin((int)(Math.random() * 20 + 1),(int)(Math.random() * 20 + 1)));
        for(Garbage garbage : garbageList){
            System.out.println("Garbage at ("+garbage.getX()+","+garbage.getY()+").");
        }
        for(Bin bin : binList){
            System.out.println("Bin at ("+bin.getX()+","+bin.getY()+").");
        }
    }

    private void oneTick(){
        for(EnvironmentAction action: handlingRequest){
            proceedAction(action);
        }
        handlingRequest.clear();
    }
    @Override
    public void run() {
        while(!terminate){
            oneTick();
        }
    }

    @Override
    public void proceedAction(EnvironmentAction action) {
        ArrayList<Literal> postcondition = new ArrayList<>();
        boolean success = true;
        Cleaner entity = (Cleaner) entities.get(action.getEntityID());
        int oldX = entity.getX();
        int oldY = entity.getY();

        if(action.getActionName().equals("move")){
            entity.move(action.getArguments().get(0));
            int newX = entity.getX();
            int newY = entity.getY();
            ArrayList<GpredClause> prePos = new ArrayList<>();
            ArrayList<GpredClause> posPos = new ArrayList<>();
            prePos.add(new Atom("self"));
            posPos.add(new Atom("self"));
            prePos.add(new Atom(String.valueOf(oldX)));
            prePos.add(new Atom(String.valueOf(oldY)));
            posPos.add(new Atom(String.valueOf(newX)));
            posPos.add(new Atom(String.valueOf(newY)));
            postcondition.add(new Literal(true, new VpredClause("position", prePos)));
            postcondition.add(new Literal(false, new VpredClause("position", posPos)));
            success = true;
        }else if(action.getActionName().equals("pickup")){
            Garbage garbage = null;
            for(Garbage buffer : garbageList){
                if(buffer.getX() == oldX && buffer.getY() == oldY){
                    garbage = buffer;
                    break;
                }
            }
            if(garbage != null){
                garbageList.remove(garbage);
                entity.pick();
                int x = garbage.getX();
                int y = garbage.getY();
                ArrayList<GpredClause> pos = new ArrayList<>();
                pos.add(new Atom("garbage"));
                pos.add(new Atom(String.valueOf(x)));
                pos.add(new Atom(String.valueOf(y)));
                postcondition.add(new Literal(true, new VpredClause("position", pos)));
                postcondition.add(new Literal(false, new VpredClause("occupied", new ArrayList<>())));
            }
            success = true;
        }else if(action.getActionName().equals("throw")){
            Bin bin = null;
            for(Bin buffer : binList){
                if(buffer.getX() == oldX && buffer.getY() == oldY){
                    bin = buffer;
                    break;
                }
            }
            entity.throwGarbage();
            if(bin != null){
                postcondition.add(new Literal(true, new VpredClause("occupied", new ArrayList<>())));
            }else{

                ArrayList<GpredClause> pos = new ArrayList<>();
                pos.add(new Atom("garbage"));
                pos.add(new Atom(String.valueOf(oldX)));
                pos.add(new Atom(String.valueOf(oldY)));
                postcondition.add(new Literal(true, new VpredClause("occupied", new ArrayList<>())));
                postcondition.add(new Literal(false, new VpredClause("position", pos)));
                garbageList.add(new Garbage(oldX,oldY));
            }
            success = true;
        }else if(action.getActionName().equals("init")){
            int x = entity.getX();
            int y = entity.getY();
            ArrayList<GpredClause> pos = new ArrayList<>();
            pos.add(new Atom("self"));
            pos.add(new Atom(String.valueOf(x)));
            pos.add(new Atom(String.valueOf(y)));
            postcondition.add(new Literal(false, new VpredClause("position", pos)));
            for(Garbage garbage: garbageList){
                x = garbage.getX();
                y = garbage.getY();
                pos = new ArrayList<>();
                pos.add(new Atom("garbage"));
                pos.add(new Atom(String.valueOf(x)));
                pos.add(new Atom(String.valueOf(y)));
                postcondition.add(new Literal(false, new VpredClause("position", pos)));
            }

            for(Bin bin: binList){
                x = bin.getX();
                y = bin.getY();
                pos = new ArrayList<>();
                pos.add(new Atom("bin"));
                pos.add(new Atom(String.valueOf(x)));
                pos.add(new Atom(String.valueOf(y)));
                postcondition.add(new Literal(false, new VpredClause("position", pos)));
            }
        }
        EnvironmentRespond respond = new EnvironmentRespond(action.getAgentID(),postcondition,success,action.getActionID(), entity.thisID);

        respondList.add(respond);
    }
}

class Garbage{
    private int x;
    private int y;

    public Garbage(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}


class Bin{
    private int x;
    private int y;

    public Bin(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}


class Cleaner extends ControllableEntity{
    private int x;
    private int y;
    private boolean occupied;

    public Cleaner(Environment env, int x, int y) {
        super(env);
        this.x = x;
        this.y = y;
        this.occupied = false;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void move(String direction){
        switch (direction) {
            case "left":
                this.x--;
                break;
            case "right":
                this.x++;
                break;
            case "up":
                this.y++;
                break;
            case "down":
                this.y--;
                break;
        }
    }

    public void pick(){
        this.occupied = true;
    }

    public void throwGarbage(){
        this.occupied = false;
    }
}