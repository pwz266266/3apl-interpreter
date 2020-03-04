import java.util.ArrayList;
import java.util.HashMap;

import javafx.animation.Animation;
import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;

public class CleanerEnvironment extends Environment {
    private ArrayList<Garbage> garbageList = new ArrayList<>();
    private ArrayList<Bin> binList = new ArrayList<>();
    private Stage stage;
    private Group group = new Group();
    private Scene scene = new Scene(group);

    public CleanerEnvironment() {
        super();
    }

    private void oneTick(){
        startHandling();
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
        int oldX = (int)entity.getX();
        int oldY = (int)entity.getY();

        if(action.getActionName().equals("move")){
            entity.setDirection(action.getArguments().get(0));
            while(!entity.isFinishMoving()){ }
            int newX = (int)entity.getX();
            int newY = (int)entity.getY();
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
            if(garbage != null && !entity.isOccupied()){
                garbageList.remove(garbage);
                garbage.setVisible(false);
                entity.pick();
                int x = (int)garbage.getX();
                int y = (int)garbage.getY();
                ArrayList<GpredClause> pos = new ArrayList<>();
                pos.add(new Atom("garbage"));
                pos.add(new Atom(String.valueOf(x)));
                pos.add(new Atom(String.valueOf(y)));
                postcondition.add(new Literal(true, new VpredClause("position", pos)));
                postcondition.add(new Literal(false, new VpredClause("occupied", new ArrayList<>())));
                success = true;
            }else if(garbage==null){
                int x = (int)entity.getX();
                int y = (int)entity.getY();
                ArrayList<GpredClause> pos = new ArrayList<>();
                pos.add(new Atom("garbage"));
                pos.add(new Atom(String.valueOf(x)));
                pos.add(new Atom(String.valueOf(y)));
                postcondition.add(new Literal(false, new VpredClause("no", pos)));
                postcondition.add(new Literal(false, new VpredClause("occupied", new ArrayList<>())));
                success = false;
            }
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
            int x = (int)entity.getX();
            int y = (int)entity.getY();
            ArrayList<GpredClause> pos = new ArrayList<>();
            pos.add(new Atom("self"));
            pos.add(new Atom(String.valueOf(x)));
            pos.add(new Atom(String.valueOf(y)));
            postcondition.add(new Literal(false, new VpredClause("position", pos)));
            for(Garbage garbage: garbageList){
                x = (int)garbage.getX();
                y = (int)garbage.getY();
                pos = new ArrayList<>();
                pos.add(new Atom("garbage"));
                pos.add(new Atom(String.valueOf(x)));
                pos.add(new Atom(String.valueOf(y)));
                postcondition.add(new Literal(false, new VpredClause("position", pos)));
            }

            for(Bin bin: binList){
                x = (int)bin.getX();
                y = (int)bin.getY();
                pos = new ArrayList<>();
                pos.add(new Atom("bin"));
                pos.add(new Atom(String.valueOf(x)));
                pos.add(new Atom(String.valueOf(y)));
                postcondition.add(new Literal(false, new VpredClause("position", pos)));
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
                Cleaner newCleaner = new Cleaner(Double.parseDouble(parameter.get(0)), Double.parseDouble(parameter.get(1)));
                newCleaner.startAnimation();
                this.entities.put(newCleaner.getID(), newCleaner);
                group.getChildren().add(newCleaner);
                return newCleaner.getID();
            case "Garbage":
                Garbage newGarbage = new Garbage(Double.parseDouble(parameter.get(0)), Double.parseDouble(parameter.get(1)));
                this.garbageList.add(newGarbage);
                group.getChildren().add(newGarbage);
                break;
            case "Bin":
                Bin newBin = new Bin(Double.parseDouble(parameter.get(0)), Double.parseDouble(parameter.get(1)));
                this.binList.add(newBin);
                group.getChildren().add(newBin);
                break;
        }
        return -1;
    }
}

class Garbage extends Rectangle {

    public Garbage(double x, double y) {
        this.setX(x);
        this.setY(y);
        this.setHeight(5);
        this.setWidth(5);
        this.setFill(Color.BLUE);
    }
}


class Bin extends Rectangle{
    public Bin(double x, double y) {
        this.setX(x);
        this.setY(y);
        this.setX(x);
        this.setY(y);
        this.setHeight(10);
        this.setWidth(10);
        this.setFill(Color.RED);
    }

}


class Cleaner extends Rectangle implements ControllableEntity {
    static int ID = 1;
    private int thisID = Cleaner.ID++;
    private boolean occupied;
    private String direction = "Stay";

    public boolean isFinishMoving() {
        synchronized (direction) {
            return finishMoving;
        }
    }

    private boolean finishMoving = true;
    public Cleaner(double x, double y) {
        this.setX(x);
        this.setY(y);
        this.occupied = false;
        this.setX(x);
        this.setY(y);
        this.setHeight(10);
        this.setWidth(10);
        this.setFill(Color.ROSYBROWN);
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
        AnimationTimer animation = new AnimationTimer() {
            public void handle(long currentNanoTime) {
                move();
            }
        };

        // ensure the animation is playing
        animation.start();
    }
}