import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Slider;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import static java.lang.Thread.sleep;

public class CleanerEnvironment extends Environment {
    ArrayList<ArrayList<Cell>> map;
    private HashMap<Integer,Rectangle> agents = new HashMap<>();
    private ArrayList<ArrayList<Rectangle>> rectangles;
    private ArrayList<ArrayList<Circle>> circles;
    private ArrayList<Bin> binList = new ArrayList<>();
    private Group group = new Group();
    private int clock = 0;
    private Scene scene = new Scene(group);
    private int score = 0;
    private int size;
    private Slider bar;
    public CleanerEnvironment(){
        super();
        this.size = 1000;
        init(size);
    }
    public CleanerEnvironment(int size){
        super();
        this.size = size;
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
        rectangles = new ArrayList<>(61);
        circles = new ArrayList<>(61);
        for(int i=0; i<61; i++){
            ArrayList<Rectangle> rectangleBuffer = new ArrayList<>(61);
            ArrayList<Circle> circleBuffer = new ArrayList<>(61);
            rectangles.add(rectangleBuffer);
            circles.add(circleBuffer);
            for(int j=0; j<61; j++){
                Rectangle rect = new Rectangle(i*11,j*11,10,10);
                rect.setVisible(false);
                Circle cir = new Circle(i*11+6,j*11+6,5);
                cir.setVisible(false);
                rectangleBuffer.add(rect);
                circleBuffer.add(cir);
            }
            this.group.getChildren().addAll(rectangleBuffer);
            this.group.getChildren().addAll(circleBuffer);
        }
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
                        Bin bin = new Bin(TrashType.RECYCLING);
                        map.get(i).add(j, bin);
                        this.binList.add(bin);
                    }else{
                        Bin bin = new Bin(TrashType.WASTE);
                        map.get(i).add(j, bin);
                        this.binList.add(bin);
                    }
                }else if(randomNum >= 0.9916){
                    RechargePoint recharge = new RechargePoint();
                    map.get(i).add(j, recharge);
                }else if(randomNum >= 0.9876){
                    if(subNum >= 0.5) {
                        Station station = new Station(TrashType.RECYCLING);
                        map.get(i).add(j, station);
                    }else{
                        Station station = new Station(TrashType.WASTE);
                        map.get(i).add(j, station);
                    }
                }
            }
        }
    }

    private void oneTick() {
        int prevScore = score;
        generateTasks();
        startHandling();
        if(! handlingRequest.isEmpty()){
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
        if(score != prevScore){
            this.envInter.updateEnvironmentInfo(clock,"Current Score: "+score);
        }
        updateGUI();
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

    public void updateGUI(){
        Cleaner entity = (Cleaner)entities.get((int)bar.getValue());
        int eX = entity.getX();
        int eY = entity.getY();
        int UpLimit = eY + 30;
        int DownLimit = eY - 30;
        int LeftLimit = eX - 30;
        int RightLimit = eX + 30;
        for(int i = LeftLimit; i <= RightLimit; i++){
            for(int j = DownLimit; j <= UpLimit; j++){
                int drawI = i - eX + 30;
                int drawJ = j - eY + 30;
                if(i >= size || j >= size || i < 0 || j < 0){
                    rectangles.get(drawI).get(drawJ).setVisible(false);
                    circles.get(drawI).get(drawJ).setVisible(false);
                }else {
                    Cell cell = map.get(i).get(j);
                    if (cell != null) {
                        switch (cell.getCellType()) {
                            case STATION:
                                Station station = (Station) cell;
                                if (station.getType() == TrashType.RECYCLING) {
                                    rectangles.get(drawI).get(drawJ).setFill(Color.GREEN);
                                } else {
                                    rectangles.get(drawI).get(drawJ).setFill(Color.BLACK);
                                }
                                circles.get(drawI).get(drawJ).setVisible(false);
                                rectangles.get(drawI).get(drawJ).setVisible(true);
                                break;
                            case BIN:
                                Bin bin = (Bin) cell;
                                if (bin.getType() == TrashType.RECYCLING) {
                                    circles.get(drawI).get(drawJ).setFill(Color.GREEN);
                                } else {
                                    circles.get(drawI).get(drawJ).setFill(Color.BLACK);
                                }
                                circles.get(drawI).get(drawJ).setVisible(true);
                                rectangles.get(drawI).get(drawJ).setVisible(false);
                                break;
                            case RECHARGE:
                                circles.get(drawI).get(drawJ).setFill(Color.RED);
                                circles.get(drawI).get(drawJ).setVisible(true);
                                rectangles.get(drawI).get(drawJ).setVisible(false);
                                break;
                        }
                    } else {
                        rectangles.get(drawI).get(drawJ).setVisible(false);
                        circles.get(drawI).get(drawJ).setVisible(false);
                    }
                }
            }
        }
        for(int i = 0; i<entities.size(); i++){
            Cleaner buffer = (Cleaner) entities.get(i);
            if(buffer.getX() >= LeftLimit && buffer.getX() <= RightLimit && buffer.getY() >= DownLimit && buffer.getY() <= UpLimit){
                agents.get(buffer.getID()).setX((buffer.getX() - eX + 30)*11+2);
                agents.get(buffer.getID()).setY((buffer.getY() - eY + 30)*11+2);
                agents.get(buffer.getID()).setVisible(true);
            }else{
                agents.get(buffer.getID()).setVisible(false);
            }
        }
    }

    public ArrayList<GpredClause> createView(Cleaner entity){
        ArrayList<GpredClause> postCondition = new ArrayList<>();
        int eX = entity.getX();
        int eY = entity.getY();
        int UpLimit = eY + 30;
        int DownLimit = eY - 30;
        int LeftLimit = eX - 30;
        int RightLimit = eX + 30;

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

        for(int i = LeftLimit; i <= RightLimit; i++){
            for(int j = DownLimit; j <= UpLimit; j++){
                if(!(i >= size || j >= size || i < 0 || j < 0)){
                    Cell cell = map.get(i).get(j);
                    if (cell != null) {
                        switch (cell.getCellType()) {
                            case STATION:
                                Station station = (Station) cell;
                                ArrayList<GpredClause> pos = new ArrayList<>();
                                ArrayList<GpredClause> atr = new ArrayList<>();
                                VpredClause attribute = new VpredClause("attribute", atr);
                                pos.add(new Atom(String.valueOf(i)));
                                pos.add(new Atom(String.valueOf(j)));
                                atr.add(new Atom(station.getType() == TrashType.RECYCLING ? "recycling" : "waste"));
                                pos.add(new Atom("station"));
                                pos.add(attribute);
                                postCondition.add(new VpredClause("position", pos));
                                break;
                            case BIN:
                                Bin bin = (Bin) cell;
                                pos = new ArrayList<>();
                                atr = new ArrayList<>();
                                attribute = new VpredClause("attribute", atr);
                                pos.add(new Atom(String.valueOf(i)));
                                pos.add(new Atom(String.valueOf(j)));
                                atr.add(new Atom(bin.getType() == TrashType.RECYCLING ? "recycling" : "waste"));
                                atr.add(new Atom(String.valueOf(bin.getTask())));
                                pos.add(new Atom("bin"));
                                pos.add(attribute);
                                postCondition.add(new VpredClause("position", pos));
                                break;
                            case RECHARGE:
                                pos = new ArrayList<>();
                                atr = new ArrayList<>();
                                attribute = new VpredClause("attribute", atr);
                                pos.add(new Atom(String.valueOf(i)));
                                pos.add(new Atom(String.valueOf(j)));
                                pos.add(new Atom("recharge"));
                                pos.add(attribute);
                                postCondition.add(new VpredClause("position", pos));
                                break;
                        }
                    }
                }
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
        Cell cell = map.get(oldX).get(oldY);
        if(action.getActionName().equals("move")){
            if(entity.move(action.getArguments().get(0))){
                postcondition = createView(entity);
                success = true;
            }else{
                success = false;
            }
        }else if(action.getActionName().equals("pickup")){
            Bin bin = null;
            if(cell.getCellType()==CellType.BIN){
                bin = (Bin) cell;
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
            if(cell.getCellType()==CellType.STATION){
                station = (Station) cell;
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
            if(cell.getCellType()==CellType.RECHARGE){
                recharge = (RechargePoint) cell;
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
    }

    @Override
    public void showGUI() {
        bar = new Slider(0,entities.size()-1,0);
        bar.setLayoutY(60*11);
        bar.setShowTickLabels(true);
        bar.setShowTickMarks(true);
        bar.setBlockIncrement(1);
        group.getChildren().add(bar);
        Stage stage = new Stage();
        stage.setHeight(66*11);
        stage.setWidth(61*11);
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
        return result;
    }

    @Override
    public int createEntity(String type, ArrayList<String> parameter) {
        if ("Cleaner (Controllable)".equals(type)) {
            Cleaner newCleaner = new Cleaner(Integer.parseInt(parameter.get(0)), Integer.parseInt(parameter.get(1)));
            this.entities.put(newCleaner.getID(), newCleaner);
            Rectangle rect = new Rectangle(7,7,Color.BROWN);
            rect.setVisible(false);
            group.getChildren().add(rect);
            agents.put(newCleaner.getID(), rect);
            return newCleaner.getID();
        }
        return -1;
    }
}



class Bin extends Cell{
    private TrashType type;
    private int task = 0;
    public Bin(TrashType type) {
        cellType = CellType.BIN;
        this.type = type;
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
    public Station(TrashType type){
        this.type = type;
        cellType = CellType.STATION;

    }

    public TrashType getType() {
        return type;
    }
}

class RechargePoint extends Cell{
    public RechargePoint(){
        cellType = CellType.RECHARGE;
    }
}



class Cleaner implements ControllableEntity {
    static int ID = 0;
    private int thisID = Cleaner.ID++;
    private TrashType type = null;
    final static public int capacity = 200;
    private int carrying = 0;
    final static public int batteryCapacity = 500;
    private int battery = batteryCapacity;
    private int x;
    private int y;

    public void recharge(){
        this.battery = batteryCapacity;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public Cleaner(int x, int y) {
        this.setX(x);
        this.setY(y);
        this.setX(x);
        this.setY(y);
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

    public boolean move(String direction){
        if(this.battery==0){
            return false;
        }
        this.battery--;
        switch (direction) {
            case "left":
                this.setX(this.getX() - 1);
                break;
            case "right":
                this.setX(this.getX() + 1);
                break;
            case "up":
                this.setY(this.getY() + 1);
                break;
            case "down":
                this.setY(this.getY() - 1);
                break;
            case "upleft":
                this.setX(this.getX() - 1);
                this.setY(this.getY() + 1);
                break;
            case "upright":
                this.setX(this.getX() + 1);
                this.setY(this.getY() + 1);
                break;
            case "downleft":
                this.setX(this.getX() - 1);
                this.setY(this.getY() - 1);
                break;
            case "downright":
                this.setX(this.getX() + 1);
                this.setY(this.getY() - 1);
                break;
            default:
                break;
        }
        return true;
    }




    @Override
    public int getID() {
        return thisID;
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

class Cell{
    protected CellType cellType;
    public CellType getCellType() {
        return cellType;
    }
}

enum CellType {STATION, BIN, RECHARGE}

enum TrashType { RECYCLING, WASTE }