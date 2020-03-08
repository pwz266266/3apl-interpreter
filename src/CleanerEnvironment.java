import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import static java.lang.Thread.sleep;

public class CleanerEnvironment extends Environment {
    ArrayList<ArrayList<Cell>> map;
    private ArrayList<Station> stationList = new ArrayList<>();
    private ArrayList<Bin> binList = new ArrayList<>();
    private ArrayList<RechargePoint> rechargeList = new ArrayList<>();
    private Stage stage;
    private Group group = new Group();
    private int clock = 0;
    private Scene scene = new Scene(group);
    private boolean printClock = true;
    private int score = 0;

    public CleanerEnvironment(int size){
        super();
        init(size);
    }
    private void generateTasks(){
        for(Bin bin : this.binList){
            if(Math.random()<=0.001){
                bin.generateTask();
            }
        }
    }

    private void init(int size){
        map = new ArrayList<>(size);
        for(int i=0; i<size; i++){
            map.add(new ArrayList<>(Arrays.asList(new Cell[size])));
        }
        for(int i = 0; i<size; i++){
            for(int j = 0; j<size; j++){
                double randomNum = Math.random();
                double subNum = Math.random();
                if(randomNum>=0.992) {
                    if(subNum >= 0.5) {
                        Bin bin = new Bin(i * 5, j * 5, TrashType.RECYCLING);
                        map.get(i).add(j, bin);
                        this.binList.add(bin);
                    }else{
                        Bin bin = new Bin(i * 5, j * 5, TrashType.WASTE);
                        map.get(i).add(j, bin);
                        this.binList.add(bin);
                    }
                }else if(randomNum >= 0.9916){
                    RechargePoint recharge = new RechargePoint(i * 5, j * 5);
                    map.get(i).add(j, recharge);
                    this.rechargeList.add(recharge);
                }else if(randomNum >= 0.9876){
                    if(subNum >= 0.5) {
                        Station station = new Station(i * 5, j * 5, TrashType.RECYCLING);
                        map.get(i).add(j, station);
                        this.stationList.add(station);
                    }else{
                        Station station = new Station(i * 5, j * 5, TrashType.WASTE);
                        map.get(i).add(j, station);
                        this.stationList.add(station);
                    }
                }
            }
        }
        this.group.getChildren().addAll(this.binList);
        this.group.getChildren().addAll(this.stationList);
        this.group.getChildren().addAll(this.rechargeList);
    }

    private void oneTick() {
        generateTasks();
        startHandling();
        if(! handlingRequest.isEmpty()){
            printClock = true;
            clock ++;
        }
        for(EnvironmentAction action: handlingRequest){
            proceedAction(action);
        }
        handlingRequest.clear();
        passiveSensing();
        if(clock>=10000){
            this.terminate();
        }
        if(printClock){
            System.out.println("Environment tick "+clock);
            printClock = false;
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

    public ArrayList<GpredClause> createView(Cleaner entity){
        ArrayList<GpredClause> postCondition = new ArrayList<>();
        int eX = (int)entity.getX();
        int eY = (int)entity.getY();
        int UpLimit = eY + 30*5;
        int DownLimit = eY - 30*5;
        int LeftLimit = eX - 30*5;
        int RightLimit = eX + 30*5;

        ArrayList<GpredClause> poss = new ArrayList<>();
        ArrayList<GpredClause> atrs = new ArrayList<>();
        VpredClause attributes = new VpredClause("attribute", atrs);
        poss.add(new Atom(String.valueOf(eX)));
        poss.add(new Atom(String.valueOf(eY)));
        poss.add(new Atom("self"));
        if(entity.getType()!=null) {
            atrs.add(new Atom(entity.getType() == TrashType.RECYCLING ? "recycling" : "waste"));
        }else{
            atrs.add(new Atom("none"));
        }
        atrs.add(new Atom(String.valueOf(entity.getCarrying())));
        atrs.add(new Atom(String.valueOf(entity.getBattery())));
        poss.add(attributes);
        postCondition.add(new VpredClause("position", poss));

        for(Bin bin : binList){
            if(bin.getX() <= RightLimit && bin.getX() >= LeftLimit && bin.getY() >= DownLimit && bin.getY() <= UpLimit){
                ArrayList<GpredClause> pos = new ArrayList<>();
                ArrayList<GpredClause> atr = new ArrayList<>();
                VpredClause attribute = new VpredClause("attribute", atr);
                pos.add(new Atom(String.valueOf((int)bin.getX())));
                pos.add(new Atom(String.valueOf((int)bin.getY())));
                atr.add(new Atom(bin.getType() == TrashType.RECYCLING? "recycling" : "waste"));
                atr.add(new Atom(String.valueOf(bin.getTask())));
                pos.add(new Atom("bin"));
                pos.add(attribute);
                postCondition.add(new VpredClause("position", pos));
            }
        }

        for(RechargePoint recharge : rechargeList){
            if(recharge.getX() <= RightLimit && recharge.getX() >= LeftLimit && recharge.getY() >= DownLimit && recharge.getY() <= UpLimit){
                ArrayList<GpredClause> pos = new ArrayList<>();
                ArrayList<GpredClause> atr = new ArrayList<>();
                VpredClause attribute = new VpredClause("attribute", atr);
                pos.add(new Atom(String.valueOf((int)recharge.getX())));
                pos.add(new Atom(String.valueOf((int)recharge.getY())));
                pos.add(new Atom("recharge"));
                pos.add(attribute);
                postCondition.add(new VpredClause("position", pos));
            }
        }

        for(Station station : stationList){
            if(station.getX() <= RightLimit && station.getX() >= LeftLimit && station.getY() >= DownLimit && station.getY() <= UpLimit){
                ArrayList<GpredClause> pos = new ArrayList<>();
                ArrayList<GpredClause> atr = new ArrayList<>();
                VpredClause attribute = new VpredClause("attribute", atr);
                pos.add(new Atom(String.valueOf((int)station.getX())));
                pos.add(new Atom(String.valueOf((int)station.getY())));
                atr.add(new Atom(station.getType() == TrashType.RECYCLING? "recycling" : "waste"));
                pos.add(new Atom("station"));
                pos.add(attribute);
                postCondition.add(new VpredClause("position", pos));
            }
        }

        return postCondition;
    }

    @Override
    public void proceedAction(EnvironmentAction action) {
        ArrayList<GpredClause> postcondition = new ArrayList<>();
        boolean success = true;
        Cleaner entity = (Cleaner) entities.get(action.getEntityID());
        int oldX = (int)entity.getX();
        int oldY = (int)entity.getY();

        if(action.getActionName().equals("move")){
            if(entity.setDirection(action.getArguments().get(0))){
                while(!entity.isFinishMoving());
                postcondition = createView(entity);
                success = true;
            }else{
                success = false;
            }
        }else if(action.getActionName().equals("pickup")){
            Bin bin = null;
            for(Bin buffer : binList){
                if(buffer.getX() == oldX && buffer.getY() == oldY){
                    bin = buffer;
                    break;
                }
            }
            if(bin == null || (entity.getType() != null && entity.getType() != bin.getType())){
                success = false;
            }else{
                success = true;
                int amount = Integer.parseInt(action.getArguments().get(0));
                int limit0 = Cleaner.capacity - entity.getCarrying();
                int limit1 = bin.getTask();
                int minim = Math.min(limit0,limit1);
                if(amount>minim){
                    bin.pick(minim);
                    entity.pickTrash(minim,bin.getType());
                }else{
                    bin.pick(amount);
                    entity.pickTrash(amount,bin.getType());
                }
            }
            postcondition = createView(entity);
        }else if(action.getActionName().equals("throw")){
            Station station = null;
            for(Station buffer : stationList){
                if(buffer.getX() == oldX && buffer.getY() == oldY){
                    station = buffer;
                    break;
                }
            }
            if(station != null && station.getType() == entity.getType()){
                this.score+=entity.getCarrying();
                entity.throwTrash();
                success = true;
            }else{
                success = false;
            }
            postcondition = createView(entity);
        }else if(action.getActionName().equals("init")){
            postcondition = createView(entity);
        }else if(action.getActionName().equals("recharge")){
            RechargePoint recharge = null;
            for(RechargePoint buffer : rechargeList){
                if(buffer.getX() == oldX && buffer.getY() == oldY){
                    recharge = buffer;
                    break;
                }
            }
            success = false;
            if(recharge!=null){
                entity.recharge();
                success = true;
            }
            postcondition = createView(entity);
        }
        EnvironmentRespond respond = new EnvironmentRespond(action.getAgentID(),postcondition,success,action.getActionID(), entity.getID());

        respondList.add(respond);
        System.out.println("Score: "+this.score);

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
        parameters_bin.add("recycling/waste");
        result.put("Bin", parameters_bin);
        ArrayList<String> parameters_station = new ArrayList<>();
        parameters_station.add("Int(X)");
        parameters_station.add("Int(Y)");
        parameters_station.add("recycling/waste");
        result.put("Station", parameters_station);
        ArrayList<String> parameters_recharge = new ArrayList<>();
        parameters_recharge.add("Int(X)");
        parameters_recharge.add("Int(Y)");
        result.put("RechargePoint", parameters_recharge);
        return result;
    }

    @Override
    public int createEntity(String type, ArrayList<String> parameter) {
        switch (type) {
            case "Cleaner (Controllable)":
                Cleaner newCleaner = new Cleaner(Integer.parseInt(parameter.get(0)), Integer.parseInt(parameter.get(1)));
                newCleaner.startAnimation();
                this.entities.put(newCleaner.getID(), newCleaner);
                group.getChildren().add(newCleaner);
                return newCleaner.getID();
            case "Station":
                Station newStation = new Station(Integer.parseInt(parameter.get(0)), Integer.parseInt(parameter.get(1)), parameter.get(2).equals("waste")? TrashType.WASTE : TrashType.RECYCLING);
                this.stationList.add(newStation);
                group.getChildren().add(newStation);
                break;
            case "Bin":
                Bin newBin = new Bin(Integer.parseInt(parameter.get(0)), Integer.parseInt(parameter.get(1)), parameter.get(2).equals("waste")? TrashType.WASTE : TrashType.RECYCLING);
                this.binList.add(newBin);
                group.getChildren().add(newBin);
                break;
            case "RechargePoint":
                RechargePoint recharge = new RechargePoint(Integer.parseInt(parameter.get(0)), Integer.parseInt(parameter.get(1)));
                this.rechargeList.add(recharge);
                group.getChildren().add(recharge);
                break;
        }
        return -1;
    }
}



class Bin extends Cell{
    private TrashType type;
    private int task = 0;
    public Bin(int x, int y, TrashType type) {
        cellType = CellType.BIN;
        this.setX(x);
        this.setY(y);
        this.setX(x);
        this.setY(y);
        this.setHeight(5);
        this.setWidth(5);
        this.type = type;
        if(type==TrashType.RECYCLING) {
            this.setFill(Color.RED);
        }else{
            this.setFill(Color.BROWN);
        }
    }

    public void generateTask(){
        if(this.task == 0) {
            this.task = (int) (Math.random() * 100);
        }
    }

    public void pick(int amount){
        if(amount > task){
            this.task = 0;
        }else{
            task -= amount;
        }
    }

    public TrashType getType() {
        return type;
    }

    public int getTask() {
        return task;
    }

}

class Station extends Cell{
    private TrashType type;
    public Station(int x, int y, TrashType type){
        this.setX(x);
        this.setY(y);
        this.setX(x);
        this.setY(y);
        this.setHeight(5);
        this.setWidth(5);
        this.type = type;
        cellType = CellType.STATION;

        if(type==TrashType.RECYCLING) {
            this.setFill(Color.YELLOW);
        }else{
            this.setFill(Color.YELLOWGREEN);
        }
    }

    public TrashType getType() {
        return type;
    }
}

class RechargePoint extends Cell{
    public RechargePoint(int x, int y){
        this.setX(x);
        this.setY(y);
        this.setX(x);
        this.setY(y);
        this.setHeight(5);
        this.setWidth(5);
        this.setFill(Color.BURLYWOOD);
        cellType = CellType.RECHARGE;
    }
}



class Cleaner extends Rectangle implements ControllableEntity {
    private Timeline timeline;
    static int ID = 1;
    private int thisID = Cleaner.ID++;
    private TrashType type = null;
    private String direction = "Stay";
    final static public int capacity = 200;
    private int carrying = 0;
    final static public int batteryCapacity = 500;
    private int battery = batteryCapacity;

    public void recharge(){
        this.battery = batteryCapacity;
    }

    public boolean isFinishMoving() {
        synchronized (this) {
            return finishMoving;
        }
    }

    private boolean finishMoving = true;
    public Cleaner(int x, int y) {
        this.setX(x);
        this.setY(y);
        this.setX(x);
        this.setY(y);
        this.setHeight(10);
        this.setWidth(10);
        this.setFill(Color.ROSYBROWN);
    }

    public void pickTrash(int amount, TrashType type){
        if(this.type == null){
            this.type = type;
        }else if(this.type != type){
            return;
        }
        if(this.carrying + amount > capacity){
            this.carrying = capacity;
        }else{
            this.carrying += amount;
        }
    }

    public void throwTrash(){
        this.carrying = 0;
        this.type = null;
    }

    @Override
    public EnvironmentRespond passiveSensing(String agentID){
        return null;
    }

    public boolean setDirection(String direction) {
        System.out.println(battery);
        synchronized (this) {
            if(this.battery == 0){
                return false;
            }
            this.battery--;
            this.finishMoving = false;
            this.direction = direction;
            return true;
        }
    }

    public void move(){
        synchronized (this) {
            if(type!=null){
                this.setFill(Color.BLACK);
            }else{
                this.setFill(Color.ROSYBROWN);
            }
            switch (direction) {
                case "left":
                    this.setX(this.getX() - 5);
                    finishMoving = true;
                    direction = "Stay";
                    break;
                case "right":
                    this.setX(this.getX() + 5);
                    finishMoving = true;
                    direction = "Stay";
                    break;
                case "up":
                    this.setY(this.getY() + 5);
                    finishMoving = true;
                    direction = "Stay";
                    break;
                case "down":
                    this.setY(this.getY() - 5);
                    finishMoving = true;
                    direction = "Stay";
                    break;
                case "upleft":
                    this.setX(this.getX() - 5);
                    this.setY(this.getY() + 5);
                    finishMoving = true;
                    direction = "Stay";
                    break;
                case "upright":
                    this.setX(this.getX() + 5);
                    this.setY(this.getY() + 5);
                    finishMoving = true;
                    direction = "Stay";
                    break;
                case "downleft":
                    this.setX(this.getX() - 5);
                    this.setY(this.getY() - 5);
                    finishMoving = true;
                    direction = "Stay";
                    break;
                case "downright":
                    this.setX(this.getX() + 5);
                    this.setY(this.getY() - 5);
                    this.
                    finishMoving = true;
                    direction = "Stay";
                    break;
                default:
                    break;
            }
        }
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

    public TrashType getType() {
        return this.type;
    }

    public int getCarrying() {
        return carrying;
    }

    public int getBattery() {
        return battery;
    }
}

class Cell extends Rectangle{
    protected int x;
    protected int y;
    protected CellType cellType;


    public CellType getCellType() {
        return cellType;
    }
}

enum CellType {STATION, BIN, RECHARGE}

enum TrashType { RECYCLING, WASTE }