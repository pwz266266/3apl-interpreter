import java.util.ArrayList;
import java.util.HashMap;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import static java.lang.Thread.sleep;

public class CleanerEnvironmentSimple extends Environment {
    private ArrayList<GarbageSimple> garbageList = new ArrayList<>();
    private ArrayList<BinSimple> binList = new ArrayList<>();
    private Stage stage;
    private Group group = new Group();
    private Scene scene = new Scene(group);

    private void oneTick() {
        startHandling();
        for(EnvironmentAction action: handlingRequest){
            proceedAction(action);
        }
        handlingRequest.clear();
        passiveSensing();
        if(this.garbageList.isEmpty() && !((CleanerSimple)entities.get(entities.keySet().toArray()[0])).isOccupied()){
            this.terminate();
        }
    }
    @Override
    public void run() {
        while(!terminate){
            oneTick();
            try {
                sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void passiveSensing(){
        for(int entityID : entities.keySet()){
            EnvironmentRespond respond = entities.get(entityID).passiveSensing(envInter.getAgent(entityID));
            if(respond!=null) {
                this.respondList.add(respond);
            }
        }
    }

    @Override
    public void proceedAction(EnvironmentAction action) {
        ArrayList<GpredClause> postcondition = new ArrayList<>();
        boolean success = true;
        CleanerSimple entity = (CleanerSimple) entities.get(action.getEntityID());
        int oldX = (int)entity.getX();
        int oldY = (int)entity.getY();

        if(action.getActionName().equals("move")){
            entity.setDirection(action.getArguments().get(0));
            while(!entity.isFinishMoving()){ }
            int newX = (int)entity.getX();
            int newY = (int)entity.getY();
            ArrayList<GpredClause> posPos = new ArrayList<>();
            posPos.add(new Atom(String.valueOf(newX)));
            posPos.add(new Atom(String.valueOf(newY)));
            posPos.add(new Atom("self"));
            postcondition.add(new VpredClause("position", posPos));
            success = true;
        }else if(action.getActionName().equals("pickup")){
            GarbageSimple garbage = null;
            for(GarbageSimple buffer : garbageList){
                if(buffer.getX() == oldX && buffer.getY() == oldY){
                    garbage = buffer;
                    break;
                }
            }
            if(garbage != null && !entity.isOccupied()){
                garbageList.remove(garbage);
                garbage.setVisible(false);
                entity.pick();
                int x = (int)garbage.getX();
                int y = (int)garbage.getY();
                ArrayList<GpredClause> pos = new ArrayList<>();
                pos.add(new Atom(String.valueOf(x)));
                pos.add(new Atom(String.valueOf(y)));
                pos.add(new Atom("garbage"));
                postcondition.add(new VpredClause("no", pos));
                postcondition.add(new VpredClause("occupied", new ArrayList<>()));
                success = true;
            }else if(garbage==null){
                int x = (int)entity.getX();
                int y = (int)entity.getY();
                ArrayList<GpredClause> pos = new ArrayList<>();
                pos.add(new Atom(String.valueOf(x)));
                pos.add(new Atom(String.valueOf(y)));
                pos.add(new Atom("garbage"));
                postcondition.add(new VpredClause("no", pos));
                postcondition.add(new VpredClause("occupied", new ArrayList<>()));
                success = false;
            }
        }else if(action.getActionName().equals("throw")){
            BinSimple bin = null;
            for(BinSimple buffer : binList){
                if(buffer.getX() == oldX && buffer.getY() == oldY){
                    bin = buffer;
                    break;
                }
            }
            entity.throwGarbage();
            if(bin != null){
                postcondition.add(new VpredClause("occupied", new ArrayList<>()));
            }else{

                ArrayList<GpredClause> pos = new ArrayList<>();
                pos.add(new Atom(String.valueOf(oldX)));
                pos.add(new Atom(String.valueOf(oldY)));
                pos.add(new Atom("garbage"));
                postcondition.add(new VpredClause("occupied", new ArrayList<>()));
                postcondition.add(new VpredClause("position", pos));
                garbageList.add(new GarbageSimple(oldX,oldY));
            }
            success = true;
        }else if(action.getActionName().equals("init")){
            int x = (int)entity.getX();
            int y = (int)entity.getY();
            ArrayList<GpredClause> pos = new ArrayList<>();
            pos.add(new Atom(String.valueOf(x)));
            pos.add(new Atom(String.valueOf(y)));
            pos.add(new Atom("self"));
            postcondition.add(new VpredClause("position", pos));
            for(GarbageSimple garbage: garbageList){
                x = (int)garbage.getX();
                y = (int)garbage.getY();
                pos = new ArrayList<>();
                pos.add(new Atom(String.valueOf(x)));
                pos.add(new Atom(String.valueOf(y)));
                pos.add(new Atom("garbage"));
                postcondition.add(new VpredClause("position", pos));
            }

            for(BinSimple bin: binList){
                x = (int)bin.getX();
                y = (int)bin.getY();
                pos = new ArrayList<>();
                pos.add(new Atom(String.valueOf(x)));
                pos.add(new Atom(String.valueOf(y)));
                pos.add(new Atom("bin"));
                postcondition.add(new VpredClause("position", pos));
            }
        }
        EnvironmentRespond respond = new EnvironmentRespond(action.getAgentID(),postcondition,success,action.getActionID(), entity.getID());

        respondList.add(respond);
    }

    @Override
    public void showGUI() {
        stage = new Stage();
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public HashMap<String, ArrayList<String>> CreatableEntity() {
        HashMap<String, ArrayList<String>> result = new HashMap<>();
        ArrayList<String> parameters_cleaner = new ArrayList<>();
        parameters_cleaner.add("Int(X)");
        parameters_cleaner.add("Int(Y)");
        result.put("Cleaner (Controllable)", parameters_cleaner);
        ArrayList<String> parameters_bin = new ArrayList<>();
        parameters_bin.add("Int(X)");
        parameters_bin.add("Int(Y)");
        result.put("Bin", parameters_bin);
        ArrayList<String> parameters_garbage = new ArrayList<>();
        parameters_garbage.add("Int(X)");
        parameters_garbage.add("Int(Y)");
        result.put("Garbage", parameters_garbage);
        return result;
    }

    @Override
    public int createEntity(String type, ArrayList<String> parameter) {
        switch (type) {
            case "Cleaner (Controllable)":
                CleanerSimple newCleaner = new CleanerSimple(Double.parseDouble(parameter.get(0)), Double.parseDouble(parameter.get(1)));
                newCleaner.startAnimation();
                this.entities.put(newCleaner.getID(), newCleaner);
                group.getChildren().add(newCleaner);
                return newCleaner.getID();
            case "Garbage":
                GarbageSimple newGarbage = new GarbageSimple(Double.parseDouble(parameter.get(0)), Double.parseDouble(parameter.get(1)));
                this.garbageList.add(newGarbage);
                group.getChildren().add(newGarbage);
                break;
            case "Bin":
                BinSimple newBin = new BinSimple(Double.parseDouble(parameter.get(0)), Double.parseDouble(parameter.get(1)));
                this.binList.add(newBin);
                group.getChildren().add(newBin);
                break;
        }
        return -1;
    }
}

class GarbageSimple extends Rectangle {

    public GarbageSimple(double x, double y) {
        this.setX(x);
        this.setY(y);
        this.setHeight(5);
        this.setWidth(5);
        this.setFill(Color.BLUE);
    }
}


class BinSimple extends Rectangle{
    public BinSimple(double x, double y) {
        this.setX(x);
        this.setY(y);
        this.setX(x);
        this.setY(y);
        this.setHeight(10);
        this.setWidth(10);
        this.setFill(Color.RED);
    }

}


class CleanerSimple extends Rectangle implements ControllableEntity {
    private Timeline timeline;
    static int ID = 1;
    private int thisID = CleanerSimple.ID++;
    private boolean occupied;
    private String direction = "Stay";

    public boolean isFinishMoving() {
        synchronized (direction) {
            return finishMoving;
        }
    }

    private boolean finishMoving = true;
    public CleanerSimple(double x, double y) {
        this.setX(x);
        this.setY(y);
        this.occupied = false;
        this.setX(x);
        this.setY(y);
        this.setHeight(10);
        this.setWidth(10);
        this.setFill(Color.ROSYBROWN);
    }

    @Override
    public EnvironmentRespond passiveSensing(String agentID){
        return null;
    }

    public String getDirection() {
        synchronized (direction) {
            return direction;
        }
    }

    public void setDirection(String direction) {
        synchronized (direction) {
            this.finishMoving = false;
            this.direction = direction;
        }
    }

    public void move(){
        synchronized (direction) {
            if(occupied){
                this.setFill(Color.BLACK);
            }else{
                this.setFill(Color.ROSYBROWN);
            }
            switch (direction) {
                case "left":
                    this.setX(this.getX() - 1);
                    finishMoving = true;
                    direction = "Stay";
                    break;
                case "right":
                    this.setX(this.getX() + 1);
                    finishMoving = true;
                    direction = "Stay";
                    break;
                case "up":
                    this.setY(this.getY() + 1);
                    finishMoving = true;
                    direction = "Stay";
                    break;
                case "down":
                    this.setY(this.getY() - 1);
                    finishMoving = true;
                    direction = "Stay";
                    break;
                default:
                    break;
            }
        }
    }

    public void pick(){
        this.occupied = true;
    }

    public void throwGarbage(){
        this.occupied = false;
    }

    public boolean isOccupied(){
        return occupied;
    }
    @Override
    public int getID() {
        return thisID;
    }


    public void startAnimation() {
        if (timeline == null) {
            // lazily create timeline
            timeline = new Timeline(new KeyFrame(Duration.millis(1), event -> move()));
            timeline.setCycleCount(Animation.INDEFINITE);
        }

        // ensure the animation is playing
        timeline.play();
        // ensure the animation is playing
    }
}