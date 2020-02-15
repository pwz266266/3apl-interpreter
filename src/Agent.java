import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

import alice.tuprolog.*;
import alice.tuprolog.exceptions.MalformedGoalException;
import alice.tuprolog.exceptions.NoSolutionException;


public class Agent {
    public String getName() {
        return name;
    }

    private String name;

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    private State state;
    private BeliefBase beliefBase;
    private GoalBase goalBase;
    private PlanBase planBase;
    private GoalPlanningRuleBase goalPlanningRuleBase;
    private PlanRevisionRuleBase planRevisionRuleBase;
    private Prolog engine;
    private CapabilityBase capabilityBase;
    private Boolean Debug = false;
    private int clock = 0;
    private Container container;

    public long getID() {
        return ID;
    }

    private long ID;
    private File logfile;
    public Agent(String name,
                 BeliefBase beliefBase,
                 GoalBase goalBase,
                 PlanBase planBase,
                 GoalPlanningRuleBase goalPlanningRuleBase,
                 PlanRevisionRuleBase planRevisionRuleBase,
                 CapabilityBase capabilityBase,
                 int ID)
    {
        this.name = name;
        this.beliefBase = beliefBase;
        this.goalBase = goalBase;
        this.planBase = planBase;
        this.goalPlanningRuleBase = goalPlanningRuleBase;
        this.planRevisionRuleBase = planRevisionRuleBase;
        this.capabilityBase = capabilityBase;
        this.state = State.READY;
        this.ID = ID;
    }

    public boolean receiveMessage(FileWriter fw) throws IOException {
        ArrayList<Message> messages = this.container.forwardMessage(this);
        if(messages.size()==0){
            return false;
        }

        for(Message message : messages){
            this.engine.addTheory(new Theory(message.receive()));
            if(fw!=null){
                fw.write("Received "+message.toString()+" from Container.\n");
            }
        }
        return true;
    }

    public void enableDebug(String logfile)  { this.logfile = new File(logfile); this.Debug = true;  }
    public void enableDebug()  { this.Debug = true;  }
    public void disableDebug() { this.Debug = false; }
    public void deliberation() throws NoSolutionException, MalformedGoalException, IOException {
        FileWriter fw = null;
        if(this.Debug){
            fw = new FileWriter(this.logfile, true);
        }
        if(this.Debug && this.clock == 0){
            Date currentTime = new Date();
            SimpleDateFormat sdf =
                    new SimpleDateFormat("EEE, MMM d, yyyy hh:mm:ss a ");
            sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
            fw.write("Agent started @ GMT time: "+sdf.format(currentTime).toString()+"\n");
            fw.write("\n");
            fw.write(this.toString()+"\n");
        }
        if(this.state == State.SUSPEND && !this.receiveMessage(fw)){
            if(this.Debug){
                fw.write("-".repeat(70)+"\n");
                fw.write("Agent Clock: "+this.clock+"\n\n");
                fw.write("Agent Suspended."+"\n");
                fw.flush();
                fw.close();
            }
            this.clock++;
            return;
        }
        if(this.Debug){
            fw.write("-".repeat(70)+"\n");
            fw.write("Agent Clock: "+this.clock+"\n\n");
        }
        this.state = State.ACTIVE;
        boolean flag1 = this.goalPlanningRuleBase.execute(goalBase,planBase,engine,fw);
        boolean flag2 = this.planRevisionRuleBase.execute(planBase,engine,fw);
        boolean flag3 = this.planBase.oneStep(engine,fw, container,this) == 1;
        if(! (flag1 || flag2 || flag3)){
            this.state = State.SUSPEND;
        }
        this.goalBase.checkGoals(engine, fw);
        if(this.Debug){
//            fw.write(this.planBase.toString()+"\n");
            if(this.state == State.SUSPEND){
                fw.write("No action taken, suspend."+"\n");
            }
//            fw.write("\nCurrent Belief: \n{\n"+ engine.getTheory().toString()+"}\n");
//            fw.write("\n"+ this.goalBase.toString()+"\n");
            fw.flush();
            fw.close();
        }
        this.clock++;
    }

    public void initial(Container container){
        this.engine = new Prolog();
        beliefBase.initial(engine);
        goalBase.initial();
        capabilityBase.initial(engine);
        planBase.initial(capabilityBase);
        goalPlanningRuleBase.initial(capabilityBase);
        planRevisionRuleBase.initial(capabilityBase);
        this.state = State.ACTIVE;
        this.container = container;
        this.engine.loadLibrary(new ArithmeticLibrary());
    }
    public void setContainer(Container container){
        this.container = container;
    }
    @Override
    public String toString() {
        String result = "<Agent: "+name;
        if(engine == null){
            result += "\n"+beliefBase.toString();
        }else{
            result += "\n\tBelief Base:\n "+ engine.getTheory().toString();
        }
        result += "\n"+ goalBase.toString();
        result += "\n"+ capabilityBase.toString();
        result += "\n"+ planBase.toString();
        result += "\n"+ goalPlanningRuleBase.toString();
        result += "\n"+ planRevisionRuleBase.toString();
        result += ">";
        return result;
    }
}



class Env{
    ArrayList<VVPair> list = new ArrayList<>();

    public ArrayList<String> getVarList(){
        ArrayList<String> result = new ArrayList<>();
        for(VVPair pair : list){
            result.add(pair.variable);
        }
        return result;
    }
    public void changeVal(String var, String val){
        boolean flag = false;
        for(VVPair x: list){
            if(x.variable.equals(var)){
                x.value = val;
                flag = true;
                break;
            }
        }
        if(!flag){
            list.add(new VVPair(var,val));
        }
    }

    public void deleteVar(String var){
        for(VVPair x: list){
            if(x.variable.equals(var)){
                list.remove(x);
                break;
            }
        }
    }

    String getVal(String var){
        for(VVPair x: list){
            if(x.variable.equals(var)){
                return x.value;
            }
        }
        return var;
    }

    class VVPair{
        String variable;
        String value;
        VVPair(String var, String val){
            this.variable = var;
            this.value = val;
        }
    }
}



class Atom extends GpredClause{

    public Atom(String name){
        super(name, null);
    }
    @Override
    public String toString() {
        return this.predicate;
    }
    @Override
    public String toString(Env env){ return env.getVal(this.predicate);}
    public boolean isVar() {
        return Character.isUpperCase(this.predicate.charAt(0)) || this.predicate.charAt(0) == '_';
    }
    @Override
    public GpredClause applyEnv(Env env){
        return new Atom(env.getVal(this.predicate));
    }
}

class GpredClause{
    protected String predicate;
    protected ArrayList<GpredClause> arguments;
    public GpredClause(String predicate, ArrayList<GpredClause> arguments){
        this.predicate = predicate;
        this.arguments = arguments;
    }

    @Override
    public String toString(){
        StringBuilder result = new StringBuilder();
        if(this.arguments.size() == 0){
            return predicate;
        }
        result.append(predicate + "(");
        String prefix = "";
        for(GpredClause x: arguments){
            result.append(prefix);
            prefix = ",";
            result.append(x.toString());
        }
        result.append(")");
        return result.toString();
    }

    public String toString(Env env){
        return toString();
    }

    public GpredClause applyEnv(Env env){
        ArrayList<GpredClause> newArguments = new ArrayList<>();
        for(GpredClause subClause : this.arguments){
            newArguments.add(subClause.applyEnv(env));
        }
        return new VpredClause(this.predicate, newArguments);
    }

    public Struct toProlog(){
        return new Struct(toString());
    }
}

class VpredClause extends GpredClause{

    public VpredClause(String predicate, ArrayList<GpredClause> arguments){
        super(predicate, arguments);
    }
    public ArrayList<GpredClause> getArguments(){ return this.arguments;}
    public String getPredicate(){ return this.predicate;}
    @Override
    public String toString(Env env){
        StringBuilder result = new StringBuilder();
        if(arguments.size() != 0){
            result.append(predicate + "(");
            String prefix = "";
            for(GpredClause x: arguments){
                result.append(prefix);
                prefix = ",";
                result.append(x.toString(env));
            }
            result.append(")");
            return result.toString();
        }
        return predicate;
    }

    public Struct toProlog(Env env){
        return new Struct(toString(env));
    }
}

class Literal extends Query{
    private VpredClause clause;
    private boolean isNeg;

    public Literal(boolean isNeg, VpredClause clause){
        this.clause = clause;
        this.isNeg = isNeg;
    }
    @Override
    public String toString(){
        return isNeg ? "\\+ "+clause.toString() : clause.toString();
    }

    @Override
    public String toString(Env env){
        return isNeg ? "\\+ "+clause.toString(env) : clause.toString(env);
    }

    public void performChange(Env env, Prolog engine, FileWriter fw){
        if(isNeg){
            engine.getTheoryManager().retract(clause.toString(env)+".");
            if(fw!=null){
                try{
                    fw.write("Belief \""+ this.clause.toString(env) +"\" deleted.\n");
                }catch(Exception e){
                    System.out.println("Can't write to file.");
                }
            }
        }else{
            engine.addTheory(new Theory(toString(env)+"."));
            if(fw!=null){
                try{
                    fw.write("Belief \""+ this.clause.toString(env) +"\" added.\n");
                }catch(Exception e){
                    System.out.println("Can't write to file.");
                }
            }
        }
    }
}

class wffBinary extends Query{
    private boolean isOr;
    private Query argument1;
    private Query argument2;

    public wffBinary(boolean isOr, Query arg1, Query arg2){
        this.isOr = isOr;
        argument1 = arg1;
        argument2 = arg2;
    }
    @Override
    public String toString(){
        String operator = isOr ? " , " : " ; ";
        return "(" + argument1.toString() + operator + argument2.toString() + ")";
    }

    @Override
    public String toString(Env env){
        String operator = isOr ? " , " : " ; ";
        return "(" + argument1.toString(env) + operator + argument2.toString(env) + ")";
    }
}

class TrueQuery extends Query{
    @Override
    public String toString(){
        return "true";
    }

    @Override
    public String toString(Env env){
        return "true";
    }
}

abstract class Query{
    public abstract String toString(Env env);
    public Struct toProlog(){
        return new Struct(toString());
    }

    public Struct toProlog(Env env){
        return new Struct(toString(env));
    }
    public SolveInfo performQuery(Env env, Prolog engine) throws MalformedGoalException {
        return engine.solve(toString(env));
    }
}

class Goal{
    ArrayList<GpredClause> subgoals;
    ArrayList<Boolean> locked = new ArrayList<>();

    public Goal(ArrayList<GpredClause> subgoals){
        this.subgoals = subgoals;
        for(GpredClause temp : subgoals){
            locked.add(false);
        }
    }

    public ArrayList<GpredClause> getSubgoals(){
        return this.subgoals;
    }

    @Override
    public String toString(){
        return toString("");
    }
    public String toString(String indent){
        StringBuilder result = new StringBuilder(indent);
        String prefix = "";
        for(GpredClause goal: subgoals){
            result.append(prefix);
            prefix = ",";
            result.append(goal.toString());
        }
        result.append(".");
        return result.toString();
    }
    public boolean checkGoals(Prolog engine) {
        boolean flag = false;
        for(GpredClause clause : subgoals){
            try{
                flag = engine.solve(clause.toString()).isSuccess();
            }
            catch(Exception e){
                System.out.println("Ground Predicate Clause Generation Error: " + clause.toString());
            }
            if(!flag){
                break;
            }
        }
        if(flag){
            for(GpredClause clause : subgoals) {
                engine.getTheoryManager().retract(clause.toString()+".");
            }
        }
        return flag;
    }
    public boolean blockGoal(GpredClause goal){
        boolean flag = false;
        int i = 0;
        for(GpredClause clause: subgoals){
            flag = !locked.get(i) && clause.toString().equals(goal.toString());
            if(flag){
                locked.set(i,true);
                break;
            }
            i += 1;
        }
        return flag;
    }

    public boolean checkExist(GpredClause goal){
        boolean flag = false;
        int i = 0;
        for(GpredClause clause: subgoals){
            flag = !locked.get(i) && clause.toString().equals(goal.toString());
            i += 1;
            if(flag){
                break;
            }
        }
        return flag;
    }
}

class GoalBase{
    private ArrayList<Goal> goals;
    public GoalBase(ArrayList<Goal> goals){
        this.goals = goals;
    }

    @Override
    public String toString(){
        return toString("");
    }
    public String toString(String indent){
        String prefix = "";
        StringBuilder result = new StringBuilder(indent+"Goal Base:\n");
        for(Goal goal: goals){
            result.append(prefix);
            prefix = "\n";
            result.append(goal.toString(indent+"\t"));
        }
        result.append("\n");
        return result.toString();
    }

    public void checkGoals(Prolog engine, FileWriter fw){
        ArrayList<Goal> toRemove = new ArrayList<>();
        for(Goal goal : goals){
            if(goal.checkGoals(engine)){
                toRemove.add(goal);
            }
        }
        for(Goal goal : toRemove){
            if(fw!=null){
                try{
                    fw.write("Goal \"" + goal.toString() + "\" completed and deleted.\n");
                }catch(Exception e){
                    System.out.println("Can't write to file.");
                }
            }
            goals.remove(goal);
        }
    }

    public boolean checkExist(Goal goal){
        boolean flag = false;
        for(GpredClause subgoal: goal.getSubgoals()){
            boolean tempFlag = false;
            for(Goal egoal: goals){
                if(egoal.checkExist(subgoal)){
                    tempFlag = true;
                    break;
                }
            }
            flag = tempFlag;
            if(!flag){
                break;
            }
        }
        return flag;
    }

    public boolean blockGoal(Goal goal){
        boolean flag = false;
        for(GpredClause subgoal: goal.getSubgoals()){
            boolean tempFlag = false;
            for(Goal egoal: goals){
                if(egoal.blockGoal(subgoal)){
                    tempFlag = true;
                    break;
                }
            }
            flag = tempFlag;
            if(!flag){
                break;
            }
        }
        return flag;
    }
    public void initial() { }
}

class BeliefBase{
    private ArrayList<String> hornClauses;
    private ArrayList<GpredClause> gClauses;
    public BeliefBase(ArrayList<String> hornClauses, ArrayList<GpredClause> gClauses){
        this.gClauses = gClauses;
        this.hornClauses = hornClauses;
    }

    @Override
    public String toString(){
        return toString("");
    }
    public String toString(String indent) {
        String prefix = "";
        StringBuilder result = new StringBuilder(indent+"Belief Base: \n ");
        for(GpredClause gclause: gClauses){
            result.append(prefix);
            prefix = "\n";
            result.append(indent+"\t"+gclause.toString());
        }
        for(String hclause: hornClauses){
            result.append(prefix);
            prefix = "\n";
            result.append(indent+"\t"+hclause);
        }
        result.append("\n");
        return result.toString();
    }

    public void initial(Prolog engine){
        for(String hornClause : hornClauses){
            Theory theory = new Theory(hornClause);
            engine.addTheory(theory);
        }
        for(GpredClause gpredClause : gClauses){
            Theory theory = new Theory(gpredClause.toString()+".");
            engine.addTheory(theory);
        }
    }
}

class Capability{
    private Query precondition;
    private String name;
    private ArrayList<String> arguments;
    private ArrayList<Literal> postcondition;
    public Capability(Query precondition, String name, ArrayList<String> arguments, ArrayList<Literal> postcondition){
        this.precondition = precondition;
        this.name = name;
        this.arguments = arguments;
        this.postcondition = postcondition;
    }

    public ArrayList<String> getArguments(){
        return arguments;
    }
    public String getName(){
        return name;
    }
    @Override
    public String toString(){
        return toString("");
    }
    public String toString(String indent) {
        StringBuilder result = new StringBuilder(indent+"<Capability: " + name +" ");
        result.append("(");
        String prefix = "";
        for(String arg : arguments){
            result.append(prefix);
            prefix = ",";
            result.append(arg);
        }
        result.append(")\n"+indent+"\t"+"{");
        result.append(precondition.toString());
        result.append("}\n"+indent+"\t"+"{");
        prefix = "";
        for(Literal lit : postcondition){
            result.append(prefix);
            prefix = ",";
            result.append(lit.toString());
        }
        result.append("}");
        result.append("\n"+indent+">");
        return result.toString();
    }

    public String toString(Env env, String indent){
        StringBuilder result = new StringBuilder(indent+"<Capability: " + name + "\n");
        result.append("(");
        String prefix = "";
        for(String arg : arguments){
            result.append(prefix);
            prefix = ",";
            result.append(arg);
        }
        result.append(")"+indent+"\n{");
        result.append(precondition.toString(env));
        result.append("}"+indent+"\n{");
        prefix = "";
        for(Literal lit : postcondition){
            result.append(prefix);
            prefix = ",";
            result.append(lit.toString(env));
        }
        result.append("}");
        result.append("\n"+indent+">");
        return result.toString();
    }

    public String toString(Env env){
        return toString(env, "");
    }

    boolean perform(Prolog engine, Env env, FileWriter fw) throws MalformedGoalException, NoSolutionException {
        SolveInfo info = precondition.performQuery(env, engine);
        if(info.isSuccess()){
            if(fw != null){
                try{
                    fw.write(", success.\n");
                }catch(Exception e){
                    System.out.println("Can't write to file.");
                }
            }
            List<Var> vars = info.getBindingVars();
            for (Var var : vars){
                String var_n = var.getName();
                String val_n = var.getTerm().toString();
                env.changeVal(var_n, val_n);
            }
            for(Literal cond :postcondition){
                cond.performChange(env,engine,fw);
            }
        }else{
            try{
                fw.write(", failed.\n");
            }catch(Exception e){
                System.out.println("Can't write to file.");
            }
        }
        return info.isSuccess();
    }
}
class CapabilityBase{
    private ArrayList<Capability> capabilities;
    public CapabilityBase(ArrayList<Capability> caps){
        capabilities = caps;
    }

    @Override
    public String toString(){
        return toString("");
    }
    public String toString(String indent) {
        StringBuilder result = new StringBuilder(indent+"Capability Base: \n");
        String prefix = "";
        for(Capability cap: capabilities){
            result.append(prefix);
            prefix = ";\n";
            result.append(cap.toString(indent+"\t"));
        }
        return result.toString();
    }

    public Capability searchCap(String name){
        for(Capability cap : capabilities){
            if(cap.getName().equals(name)){
                return  cap;
            }
        }
        return null;
    }

    public void initial(Prolog engine) { }
}

abstract class BasicPlan{
    abstract public int oneStep(Env env, Prolog engine, FileWriter fw, Container container, Agent agent) throws NoSolutionException, MalformedGoalException;
    abstract public String toString(Env env, String indent);
    abstract public String toString(String indent);
    @Override
    public String toString(){
        return toString("");
    }
    public  String toString(Env env){
        return toString(env, "");
    }
    public void binding(CapabilityBase caps){};
    public void ModifyEnv(Env env){};
    public PlanType type() {return PlanType.ATOMIC;}

}

abstract class Sequential extends BasicPlan{
    ArrayList<BasicPlan> components;
    int currentPos;
    public BasicPlan findByPos(CodePosition x){
        if(x == null){
            return null;
        }else if(x.getPos() >= components.size()) {
            return null;
        }else if(x.getNextLevel() == null) {
            return components.get(x.getPos());
        }else{
            CodePosition first = x.getNextLevel();
            return ((Sequential)components.get(x.getPos())).findByPos(first);
        }
    }

    public ArrayList<CodePosition> singleMatch(SeqPlan newPlan, CodePosition pos){
        ArrayList<CodePosition> result = new ArrayList<>();
        for(int i = 0; i<components.size(); i++){
            boolean match = false;
            if(components.get(i).toString().equals(newPlan.components.get(0).toString())){
                if(newPlan.components.size()!=1) {
                    for (int j = 1; j + i < components.size(); j++) {
                        if (!components.get(i + j).toString().equals(newPlan.components.get(j).toString())) {
                            break;
                        } else if (j == newPlan.components.size() - 1) {
                            CodePosition newPos;
                            if (pos == null) {
                                newPos = new CodePosition(i, false);
                            } else {
                                newPos = pos.clone();
                                newPos.addLevel(new CodePosition(i, false));
                            }
                            result.add(newPos);
                            match = true;
                            break;
                        }
                    }
                }else{
                    CodePosition newPos;
                    if (pos == null) {
                        newPos = new CodePosition(i, false);
                    } else {
                        newPos = pos.clone();
                        newPos.addLevel(new CodePosition(i, false));
                    }
                    result.add(newPos);
                    match = true;
                }
                if(match){
                    i = i+newPlan.components.size()-1;
                }
            }
            if(!match) {
                BasicPlan plan = components.get(i);
                if (plan.type() == PlanType.IF) {
                    CodePosition ifPos;
                    CodePosition elsePos;
                    if(pos == null){
                        ifPos = new CodePosition(i, false);
                        elsePos = new CodePosition(i, false);
                    }else{
                        ifPos = pos.clone();
                        elsePos = pos.clone();
                        ifPos.addLevel(new CodePosition(i,false));
                        elsePos.addLevel(new CodePosition(i,false));
                    }
                    ifPos.addLevel(new CodePosition(0, true));
                    elsePos.addLevel(new CodePosition(1, true));
                    result.addAll(((SeqPlan) ((IfPlan) plan).components.get(0)).singleMatch(newPlan, ifPos));
                    result.addAll(((SeqPlan) ((IfPlan) plan).components.get(1)).singleMatch(newPlan, elsePos));
                } else if (plan.type() == PlanType.SEQUENTIAL) {
                    CodePosition newPos;
                    if(pos == null){
                        newPos = new CodePosition(i, false);
                    }else{
                        newPos = pos.clone();
                        newPos.addLevel(new CodePosition(i,false));
                    }
                    result.addAll(((Sequential) plan).singleMatch(newPlan, newPos));
                }
            }
        }
        return result;
    }

    @Override
    public PlanType type() {return PlanType.SEQUENTIAL;}

    public void replace(CodePosition start, int length, SeqPlan newPlan){
        if(start.getNextLevel() == null){
            int start_idx = start.getPos();
            ArrayList<BasicPlan> first_part = new ArrayList<>(components.subList(0, start_idx));
            ArrayList<BasicPlan> second_part = newPlan.components;
            ArrayList<BasicPlan> third_part = new ArrayList<>(components.subList(start_idx+length, components.size()));
            components = new ArrayList<>();
            components.addAll(first_part);
            components.addAll(second_part);
            components.addAll(third_part);
        }else{
            CodePosition first = start.getNextLevel();
            ((Sequential)components.get(start.getPos())).replace(start, length, newPlan);
        }
    }

    public ArrayList<BasicPlan> getComponents(){
        return components;
    }
}

class SeqPlan extends Sequential{

    public SeqPlan(ArrayList<BasicPlan> components){
        this.components = components;
        this.currentPos = 0;
    }

    @Override
    public int oneStep(Env env, Prolog engine, FileWriter fw, Container container, Agent agent) throws MalformedGoalException, NoSolutionException {
        int result = this.components.get(currentPos).oneStep(env,engine, fw, container, agent);
        if(result == 1){
            currentPos += 1;
            if(currentPos == components.size()){
                currentPos = 0;
                return 1;
            }else{
                return 0;
            }
        }else{
            return result;
        }
    }

    @Override
    public String toString(Env env, String indent) {
        StringBuilder result = new StringBuilder(indent);
        result.append("<Sequence: ");
        String prefix = "\n";
        for(BasicPlan x: components){
            result.append(prefix);
            result.append(x.toString(env,indent+"\t"));
        }
        result.append("\n"+indent+">");
        return result.toString();
    }

    @Override
    public String toString(String indent) {
        StringBuilder result = new StringBuilder(indent);
        result.append("<Sequence: ");
        String prefix = "\n";
        for(BasicPlan x: components){
            result.append(prefix);
            result.append(x.toString(indent+"\t"));
        }
        result.append("\n"+indent+">");
        return result.toString();
    }

    @Override
    public void binding(CapabilityBase caps) {
        for(BasicPlan x : this.components){
            x.binding(caps);
        }
    }

    @Override
    public void ModifyEnv(Env env) {
        for(BasicPlan x : this.components){
            x.ModifyEnv(env);
        }
    }
}

class JavaAction extends BasicPlan{
    private ArrayList<Atom> arguments;

    public JavaAction(ArrayList<Atom> arguments){
        this.arguments = arguments;
    }

    @Override
    public int oneStep(Env env, Prolog engine, FileWriter fw, Container container, Agent agent) {
        return 1;
    }

    @Override
    public String toString(Env env, String indent) {
        StringBuilder result = new StringBuilder(indent);
        result.append("<JavaActoin: arguments = (");
        String prefix = "";
        for(Atom x: arguments){
            result.append(prefix);
            prefix = ",";
            if(x.isVar()){
                result.append(env.getVal(x.toString()));
            }else{
                result.append(x.toString());
            }
        }
        result.append(")>  (not implemented)");
        return result.toString();
    }

    @Override
    public String toString(String indent) {
        StringBuilder result = new StringBuilder(indent);
        result.append("<JavaActoin: arguments = (");
        String prefix = "";
        for(Atom x: arguments){
            result.append(prefix);
            prefix = ",";
            result.append(x.toString());
        }
        result.append(")>  (not implemented)");
        return result.toString();
    }
}


class CapAction extends BasicPlan{
    private String predicate;
    private ArrayList<Atom> arguments;
    private Capability cap;
    public CapAction(String predicate, ArrayList<Atom> arguments){
        this.predicate = predicate;
        this.arguments = arguments;
    }

    @Override
    public int oneStep(Env env, Prolog engine, FileWriter fw, Container container, Agent agent) throws NoSolutionException, MalformedGoalException {
        if(fw != null){
            try{
                fw.write("Execute: "+this.toString("") + " ");
            }catch(Exception e){
                System.out.println("Can't write to file.");
            }
        }
        if(this.cap == null){
            return -2;
        }
        Env insideEnv = new Env();
        ArrayList<String> capArgs = this.cap.getArguments();
        for(int i = 0; i<capArgs.size(); i++){
            insideEnv.changeVal(capArgs.get(i), env.getVal(this.arguments.get(i).toString()));
        }
        boolean flag = this.cap.perform(engine, insideEnv, fw);
        if(flag){
            return 1;
        }else{
            return -1;
        }
    }

    @Override
    public String toString(String indent) {
        StringBuilder result = new StringBuilder(indent);
        result.append("<CapAction: ").append(this.predicate).append("(");
        String prefix = "";
        for(Atom x: arguments){
            result.append(prefix);
            prefix = ",";
            result.append(x.toString());
        }
        result.append(")>");
        return result.toString();
    }

    @Override
    public String toString(Env env, String indent) {
        StringBuilder result = new StringBuilder(indent);
        result.append("<CapAction: "+ this.predicate +"(");
        String prefix = "";
        for(Atom x: arguments){
            result.append(prefix);
            prefix = ",";
            if(x.isVar()){
                result.append(env.getVal(x.toString()));
            }else{
                result.append(x.toString());
            }
        }
        result.append(")>");
        return result.toString();
    }

    @Override
    public void binding(CapabilityBase caps) {
        this.cap = caps.searchCap(this.predicate);
    }
}

class SendAction extends BasicPlan{
    private Performative performative;
    private String receiver;
    private VpredClause content;
    private VpredClause reply;
    public SendAction(String performative, String receiver, VpredClause content, VpredClause reply){
        this.performative = Performative.translate(performative);
        this.receiver = receiver;
        this.content = content;
        this.reply = reply;
    }

    @Override
    public int oneStep(Env env, Prolog engine, FileWriter fw, Container container, Agent agent) throws NoSolutionException, MalformedGoalException {
        try {
            VpredClause newBody = (VpredClause) content.applyEnv(env);
            VpredClause newReply = (VpredClause) reply.applyEnv(env);
            Message message = new Message(performative, "container" + "_" + container.getID() + "_" + agent.getID(), env.getVal(receiver), newReply, newBody);
            if(fw!=null) {
                fw.write("Send " + message.toString() + " to Container.\n");
            }
            container.addMessage(message);
        }catch(Exception e){}
        return 1;
    }

    @Override
    public String toString(String indent) {
        StringBuilder result = new StringBuilder(indent);
        result.append("<SendAction: Performative = ");
        result.append(performative+", Receiver = ");
        result.append(receiver+", Content = ");
        result.append(content.toString()+", "+reply.toString());
        result.append(">");
        return result.toString();
    }

    @Override
    public String toString(Env env, String indent) {
        StringBuilder result = new StringBuilder(indent);
        result.append("<SendAction: performative = ");
        result.append(performative+", receiver = ");
        result.append(receiver+", Content = ");
        result.append(content.toString(env)+", "+reply.toString(env));
        result.append(">");
        return result.toString();
    }
}

class TestAction extends BasicPlan{
    private Query query;
    public TestAction(Query query){
        this.query = query;
    }

    @Override
    public int oneStep(Env env, Prolog engine, FileWriter fw, Container container, Agent agent) throws NoSolutionException, MalformedGoalException {
        SolveInfo info = this.query.performQuery(env, engine);
        if(info.isSuccess()){
            if(fw != null){
                try{
                    fw.write("Execute: "+this.toString() + " , success.\n");
                }catch(Exception e){
                    System.out.println("Can't write to file.");
                }
            }
            List<Var> vars = info.getBindingVars();
            for (Var var : vars){
                String var_n = var.getName();
                String val_n = var.getTerm().toString();
                env.changeVal(var_n, val_n);
            }
            return 1;
        }else{
            if(fw != null){
                try{
                    fw.write("Execute: "+this.toString("")+" ,failed. \n");
                }catch(Exception e){
                    System.out.println("Can't write to file.");
                }
            }
            return -1;
        }
    }

    @Override
    public String toString(Env env, String indent) {
        return indent+"<TestAction: "+this.query.toString(env)+"?>";
    }

    @Override
    public String toString(String indent){
        return indent+"<TestAction: "+this.query.toString()+"?>";
    }
}

class IfPlan extends Sequential{
    private Query condition;
    private Env insideEnv;
    private boolean currCond;
    public IfPlan(SeqPlan ifComponent, SeqPlan elseComponent, Query condition){
        this.condition = condition;
        this.currentPos = 0;
        this.components = new ArrayList<BasicPlan>();
        this.components.add(ifComponent);
        this.components.add(elseComponent);
        this.insideEnv = new Env();
    }

    @Override
    public PlanType type() {return PlanType.IF;}

    @Override
    public int oneStep(Env env, Prolog engine, FileWriter fw, Container container, Agent agent) throws NoSolutionException, MalformedGoalException {
        if(currentPos == 0){
            SolveInfo info = this.condition.performQuery(env, engine);
            currCond = info.isSuccess();
            if(currCond){
                this.insideEnv = new Env();
                for(String var: env.getVarList()){
                    insideEnv.changeVal(var, env.getVal(var));
                }
                List<Var> vars = info.getBindingVars();
                for (Var var : vars){
                    String var_n = var.getName();
                    String val_n = var.getTerm().toString();
                    insideEnv.changeVal(var_n, val_n);
                }
                if(fw != null){
                    try{
                        fw.write("Query: \""+this.condition.toString() +"\" in if statement, true.\n");
                    }catch(Exception e){
                        System.out.println("Can't write to file.");
                    }
                }
            }
            else{
                if(fw != null){
                    try{
                        fw.write("Query: \""+this.condition.toString() +"\" in if statement, false.\n");
                    }catch(Exception e){
                        System.out.println("Can't write to file.");
                    }
                }
                this.insideEnv = new Env();
                for(String var: env.getVarList()){
                    insideEnv.changeVal(var, env.getVal(var));
                }
            }
            currentPos = 1;
            return 0;
        }
        int result = this.components.get(currCond ? 0 : 1).oneStep(insideEnv, engine, fw, container, agent);
        if(result == 1){
            currentPos = 0;
            insideEnv = null;
        }
        return result;
    }

    @Override
    public String toString(Env env, String indent) {
        StringBuilder result = new StringBuilder(indent+"<IfPlan: "+this.condition.toString(env));
        result.append("\n"+indent+"then: \n");
        result.append(this.components.get(0).toString(env, "\t"+indent));
        result.append("\n"+indent+"else: \n");
        result.append(this.components.get(1).toString(env, "\t"+indent));
        result.append("\n"+indent+">");
        return result.toString();
    }

    @Override
    public String toString(String indent) {
        StringBuilder result = new StringBuilder(indent+"<IfPlan: "+this.condition.toString());
        result.append("\n"+indent+"then: \n");
        result.append(this.components.get(0).toString("\t"+indent));
        result.append("\n"+indent+"else: \n");
        result.append(this.components.get(1).toString("\t"+indent));
        result.append("\n"+indent+">");
        return result.toString();
    }

    @Override
    public void binding(CapabilityBase caps) {
        for(BasicPlan x: components){
            x.binding(caps);
        }
    }

    @Override
    public void ModifyEnv(Env env) {
        if(insideEnv != null){
            for(String var: env.getVarList()){
                insideEnv.changeVal(var, env.getVal(var));
            }
        }
        for(BasicPlan x : this.components){
            x.ModifyEnv(env);
        }
    }
}

class WhilePlan extends Sequential{
    private Query condition;
    private Env insideEnv;

    public WhilePlan(ArrayList<BasicPlan> components, Query condition){
        this.condition = condition;
        this.currentPos = 0;
        this.insideEnv = new Env();
        this.components = components;
    }

    @Override
    public int oneStep(Env env, Prolog engine, FileWriter fw, Container container, Agent agent) throws NoSolutionException, MalformedGoalException {
        if(currentPos == 0){
            SolveInfo info = this.condition.performQuery(env, engine);
            if(info.isSuccess()){
                this.insideEnv = new Env();
                for(String var: env.getVarList()){
                    insideEnv.changeVal(var, env.getVal(var));
                }
                List<Var> vars = info.getBindingVars();
                for (Var var : vars){
                    String var_n = var.getName();
                    String val_n = var.getTerm().toString();
                    insideEnv.changeVal(var_n, val_n);
                }
                if(fw != null){
                    try{
                        fw.write("Query: \""+this.condition.toString() +"\" in while statement, true.\n");
                    }catch(Exception e){
                        System.out.println("Can't write to file.");
                    }
                }

                currentPos = 1;
                return 0;
            }
            else{
                if(fw != null){
                    try{
                        fw.write("Query: \""+this.condition.toString() +"\" in while statement, false.\n");
                    }catch(Exception e){
                        System.out.println("Can't write to file.");
                    }
                }
                insideEnv = null;
                return 1;
            }
        }

        int result = this.components.get(currentPos-1).oneStep(insideEnv,engine, fw, container, agent);
        if(result == 1){
            currentPos += 1;
            if(currentPos == components.size()+1){
                currentPos = 0;
            }
            return 0;
        }else{
            return result;
        }
    }

    @Override
    public String toString(Env env, String indent) {
        StringBuilder result = new StringBuilder(indent+"<WhilePlan: "+this.condition.toString(env));
        String prefix = "\n";
        for(BasicPlan x: components){
            result.append(prefix);
            result.append(x.toString(env,indent+"\t"));
        }
        result.append("\n"+indent+">");
        return result.toString();
    }

    @Override
    public String toString(String indent) {
        StringBuilder result = new StringBuilder(indent+"<WhilePlan: "+this.condition.toString());
        String prefix = "\n";
        for(BasicPlan x: components){
            result.append(prefix);
            result.append(x.toString(indent+"\t"));
        }
        result.append("\n"+indent+">");
        return result.toString();
    }

    @Override
    public void binding(CapabilityBase caps) {
        for(BasicPlan x: components){
            x.binding(caps);
        }
    }

    @Override
    public void ModifyEnv(Env env) {
        if(insideEnv != null){
            for(String var: env.getVarList()){
                insideEnv.changeVal(var, env.getVal(var));
            }
        }
        for(BasicPlan x : this.components){
            x.ModifyEnv(env);
        }
    }
}

class Plan{
    private SeqPlan plan;
    private Goal associatedGoal = null;
    private Env insideEnv;
    public Plan(SeqPlan plan, Goal associatedGoal, Env insideEnv){
        this.plan = plan;
        this.associatedGoal = associatedGoal;
        this.insideEnv = insideEnv;
    }

    public Plan(SeqPlan plan){
        this.plan = plan;
        insideEnv = new Env();
    }

    public int oneStep(Prolog engine, FileWriter fw, Container container, Agent agent) throws NoSolutionException, MalformedGoalException {
        return this.plan.oneStep(insideEnv, engine, fw, container, agent);
    }

    public void complete(Prolog engine){
        if(this.associatedGoal != null){
            engine.addTheory(new Theory(this.associatedGoal.toString()));
        }
    }

    private ArrayList<CodePosition> match(SeqPlan pattern){
        return this.plan.singleMatch(pattern, null);
    }


    public boolean revisePlan(SeqPlan oldPlan, SeqPlan newPlan, Env env){
        class CodeCompare implements Comparator<CodePosition> {
            @Override
            public int compare(CodePosition thisPos, CodePosition thatPos) {
                if(thisPos.getPos() > thatPos.getPos()){
                    return 1;
                }else if(thisPos.getPos() < thatPos.getPos()){
                    return -1;
                }else if(thisPos.getNextLevel() == null && thatPos.getNextLevel() == null){
                    return 0;
                }else if(thisPos.getNextLevel() == null){
                    return -1;
                }else if(thatPos.getNextLevel() == null){
                    return 1;
                }else{
                    return compare(thisPos.getNextLevel() ,thatPos.getNextLevel());
                }
            }
        }
        ArrayList<CodePosition> positions = this.match(oldPlan);

        if(!positions.isEmpty()) {
            boolean flag = false;
            CodePosition currentPosition = new CodePosition(this.plan.currentPos,false);
            positions.sort(new CodeCompare());
            Collections.reverse(positions);
            this.ModifyEnv(env);
            for(CodePosition pos: positions){
                if((new CodeCompare()).compare(currentPosition,pos)>0){
                    break;
                }
                this.plan.replace(pos, oldPlan.components.size(), newPlan);
                flag = true;
            }
            return flag;
        }
        return false;
    }

    @Override
    public String toString(){
        return toString("");
    }
    public String toString(String indent) {
        String toPrint;
        if(associatedGoal == null){
            toPrint = "with no goal";
        }else{
            toPrint = "with goal: "+associatedGoal.toString();
        }
        return indent+"<Plan: "+toPrint+"\n"+this.plan.toString(insideEnv,"\t"+indent)+"\n"+indent+">";
    }

    public void binding(CapabilityBase caps) {
        this.plan.binding(caps);
    }

    public void ModifyEnv(Env env) {
        for(String var: env.getVarList()){
            insideEnv.changeVal(var, env.getVal(var));
        }
        plan.ModifyEnv(env);
    }
}

class PlanBase{
    private ArrayList<Plan> plans;
    private ArrayList<Integer> status;
    public PlanBase(ArrayList<Plan> plans){
        this.plans = plans;
        this.status = new ArrayList<>();
        for(int i = 0; i<plans.size(); i++){
            status.add(0);
        }
    }

    @Override
    public String toString(){
        return toString("");
    }
    public String toString(String indent) {
        StringBuilder result = new StringBuilder(indent+"Plan Base:");
        String prefix = "";
        for(Plan plan: plans){
            result.append(prefix);
            prefix = "\n";
            result.append(plan.toString(indent+"\t"));
        }
        result.append("\n");
        return result.toString();
    }

    public void addPlan(Plan plan){
        plans.add(plan);
        status.add(0);
    }

    public int oneStep(Prolog engine, FileWriter fw, Container container, Agent agent) throws MalformedGoalException, NoSolutionException {
        boolean flag = false;
        for(int i = 0; i<status.size(); i++){
            if(status.get(i) != -1){
                flag = true;
                int result = plans.get(i).oneStep(engine, fw, container, agent);
                status.set(i,result);
                if(result == 1){
                    plans.get(i).complete(engine);
                    plans.remove(i);
                    status.remove(i);
                }
            }
        }
        return flag ? 1 : 0;
    }

    public boolean revisePlans(SeqPlan oldPlan, SeqPlan newPlan, Env env, FileWriter fw){
        boolean flag = false;
        for(Plan plan : plans){
            flag = plan.revisePlan(oldPlan, newPlan, env);
            if(flag){

                break;
            }
        }
        return flag;
    }

    public void initial(CapabilityBase capabilityBase){
        for(Plan plan: plans){
            plan.binding(capabilityBase);
        }
    }
}


class GoalPlanningRule{
    private Goal goal;
    private Query condition;
    private SeqPlan plan;

    public GoalPlanningRule(Goal goal, Query condition, SeqPlan plan){
        this.goal = goal;
        this.condition = condition;
        this.plan = plan;
    }

    public void binding(CapabilityBase caps){
        this.plan.binding(caps);
    }
    public boolean execute(GoalBase goalBase, PlanBase planBase, Prolog engine, FileWriter fw) throws MalformedGoalException, NoSolutionException {

        boolean result = true;
        if(this.goal != null){
            result = goalBase.checkExist(this.goal);

        }

        Env env = new Env();
        SolveInfo info = condition.performQuery(env,engine);
        if(result && info.isSuccess()){
            if(fw != null){
                try{
                    fw.write("Goal planning rule applied, with guard \"" +this.condition.toString() + "\" and goal \"" + (this.goal == null ? "" : this.goal.toString())+"\".\n");
                }catch(Exception e){
                    System.out.println("Can't write to file.");
                }
            }
            List<Var> vars = info.getBindingVars();
            for (Var var : vars){
                String var_n = var.getName();
                String val_n = var.getTerm().toString();
                env.changeVal(var_n, val_n);
            }
            planBase.addPlan(new Plan(plan, goal, env));
        }
        goalBase.blockGoal(this.goal);
        return result && info.isSuccess();
    }
    @Override
    public String toString(){
        return toString("");
    }
    public String toString(String indent) {
        StringBuilder result = new StringBuilder(indent);
        result.append("<Goal Planning Rule: \n"+indent+"Goal:\n\t"+indent);
        result.append(goal.toString());
        result.append("\n"+indent+"Condition:\n\t"+indent);
        result.append(condition.toString());
        result.append("\n"+indent+"Plan: \n");
        result.append(this.plan.toString(indent+"\t"));
        result.append("\n"+indent+">");
        return result.toString();
    }
}

class GoalPlanningRuleBase{
    private ArrayList<GoalPlanningRule> rules;
    public GoalPlanningRuleBase(ArrayList<GoalPlanningRule> rules){
        this.rules = rules;
    }

    public boolean execute(GoalBase goalBase, PlanBase planBase, Prolog engine, FileWriter fw) throws MalformedGoalException, NoSolutionException {
        boolean flag = false;
        for(GoalPlanningRule rule : rules){
            flag = rule.execute(goalBase, planBase, engine, fw);
            if(flag){
                break;
            }
        }
        return flag;
    }
    public void initial(CapabilityBase capabilityBase){
        for(GoalPlanningRule rule: rules){
            rule.binding(capabilityBase);
        }
    }
    @Override
    public String toString(){
        return toString("");
    }
    public String toString(String indent) {
        StringBuilder result = new StringBuilder(indent+"Goal Planning Rule Base:\n");
        String prefix = "";
        for(GoalPlanningRule rule: rules){
            result.append(prefix);
            prefix = "\n";
            result.append(rule.toString(indent+"\t"));
        }
        result.append("\n");
        return result.toString();
    }
}

class PlanRevisionRule{
    private SeqPlan oldPlan;
    private SeqPlan newPlan;
    private Query condition;
    public PlanRevisionRule(SeqPlan oldPlan, SeqPlan newPlan, Query condition){
        this.oldPlan = oldPlan;
        this.newPlan = newPlan;
        this.condition = condition;
    }

    public void binding(CapabilityBase caps){
        this.oldPlan.binding(caps);
        this.newPlan.binding(caps);
    }
    public boolean execute(PlanBase planBase, Prolog engine, FileWriter fw) throws MalformedGoalException, NoSolutionException {
        Env env = new Env();
        SolveInfo info = condition.performQuery(env, engine);
        if (info.isSuccess()){
            List<Var> vars = info.getBindingVars();
            for (Var var : vars){
                String var_n = var.getName();
                String val_n = var.getTerm().toString();
                env.changeVal(var_n, val_n);
            }
            Boolean result =  planBase.revisePlans(this.oldPlan,this.newPlan,env, fw);
            if(result){
                if(fw != null){
                    try{
                        fw.write("Plan revision rule applied, with guard \"" +this.condition.toString() + "\".\n");
                    }catch(Exception e){
                        System.out.println("Can't write to file.");
                    }
                }
            }
            return result;
        }
        return false;
    }
    @Override
    public String toString(){
        return toString("");
    }
    public String toString(String indent) {
        StringBuilder result = new StringBuilder(indent);
        result.append("<Plan Revision Rule: "+indent+"\n\tPlan pattern:\n");
        result.append(oldPlan.toString(indent+"\t"));
        result.append("\n"+indent+"Condition: \n\t"+indent);
        result.append(condition.toString());
        result.append("\n"+indent+"Revised Plan: \n");
        result.append(newPlan.toString(indent+"\t"));
        result.append("\n"+indent+">");
        return result.toString();
    }
}


class PlanRevisionRuleBase{
    private ArrayList<PlanRevisionRule> rules;
    public PlanRevisionRuleBase(ArrayList<PlanRevisionRule> rules){
        this.rules = rules;
    }

    public boolean execute(PlanBase planBase, Prolog engine, FileWriter fw) throws MalformedGoalException, NoSolutionException{
        boolean flag = false;
        for(PlanRevisionRule rule : rules){
            flag = rule.execute(planBase, engine, fw);
            if(flag){
                break;
            }
        }
        return flag;
    }
    public void initial(CapabilityBase capabilityBase){
        for(PlanRevisionRule rule: rules){
            rule.binding(capabilityBase);
        }
    }
    @Override
    public String toString(){
        return toString("");
    }
    public String toString(String indent) {
        StringBuilder result = new StringBuilder(indent+"Plan Revision Rule Base:\n");
        String prefix = "";
        for(PlanRevisionRule rule: rules){
            result.append(prefix);
            prefix = "\n";
            result.append(rule.toString(indent+"\t"));
        }
        result.append("\n");
        return result.toString();
    }
}


enum State{
    ACTIVE,SUSPEND,READY
}

enum PlanType{
    ATOMIC,SEQUENTIAL,IF
}

class CodePosition{
    private int pos;
    private boolean special;
    private CodePosition nextLevel;
    public CodePosition(int pos, boolean special){
        this.pos = pos;
        this.special = special;
        this.nextLevel = null;
    }

    @Override
    protected CodePosition clone()  {
        CodePosition newPos = new CodePosition(pos, special);
        if(nextLevel != null){
            newPos.nextLevel = this.nextLevel.clone();
        }
        return newPos;
    }

    public int getPos() {
        return pos;
    }

    public void setPos(int pos) {
        this.pos = pos;
    }

    public boolean isSpecial() {
        return special;
    }

    public void setSpecial(boolean special) {
        this.special = special;
    }

    public CodePosition getNextLevel() {
        return nextLevel;
    }

    public void setNextLevel(CodePosition nextLevel) {
        this.nextLevel = nextLevel;
    }

    public void addLevel(CodePosition position){
        if(nextLevel == null){
            nextLevel = position;
        }else{
            nextLevel.addLevel(position);
        }
    }

    @Override
    public String toString(){
        String result = special ? pos == 0 ? "then" : "else" : Integer.toString(pos);
        if(nextLevel == null){
            return result + ";";
        }else{
            return result + "::" + nextLevel.toString();
        }
    }
}