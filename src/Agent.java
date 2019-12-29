import java.util.ArrayList;
import java.util.List;

import alice.tuprolog.*;
import alice.tuprolog.exceptions.InvalidTheoryException;
import alice.tuprolog.exceptions.MalformedGoalException;
import alice.tuprolog.exceptions.NoMoreSolutionException;
import alice.tuprolog.exceptions.NoSolutionException;


public class Agent {
    private String name;
    private State state;
    private BeliefBase beliefBase;
    private GoalBase goalBase;
    private PlanBase planBase;
    private GoalPlanningRuleBase goalPlanningRuleBase;
    private PlanRevisionRuleBase planRevisionRuleBase;
    private Prolog engine;
    private CapabilityBase capabilityBase;

    public Agent(String name,
                 BeliefBase beliefBase,
                 GoalBase goalBase,
                 PlanBase planBase,
                 GoalPlanningRuleBase goalPlanningRuleBase,
                 PlanRevisionRuleBase planRevisionRuleBase,
                 CapabilityBase capabilityBase) {
        this.name = name;
        this.beliefBase = beliefBase;
        this.goalBase = goalBase;
        this.planBase = planBase;
        this.goalPlanningRuleBase = goalPlanningRuleBase;
        this.planRevisionRuleBase = planRevisionRuleBase;
        this.capabilityBase = capabilityBase;
        this.state = State.SUSPEND;
    }

    public boolean receiveMessage(){ return false; }

    public void deliberation() throws NoSolutionException, MalformedGoalException {
        if(this.state == State.SUSPEND && !this.receiveMessage()){
            return;
        }
        this.state = State.ACTIVE;
        boolean flag = false;
        flag = this.goalPlanningRuleBase.execute(goalBase,planBase,engine);
        flag = flag || this.planRevisionRuleBase.execute(planBase,engine);
        flag = flag || this.planBase.oneStep(engine) == -1;
        if(! flag){
            this.state = State.SUSPEND;
        }
    }

    public void initial(){
        this.engine = new Prolog();
        beliefBase.initial(engine);
        goalBase.initial();
        capabilityBase.initial(engine);
        planBase.initial(capabilityBase);
        goalPlanningRuleBase.initial(capabilityBase);
        planRevisionRuleBase.initial(capabilityBase);
        this.state = State.ACTIVE;
    }

    @Override
    public String toString() {
        String result = "<Agent: "+name;
        result += "\n\t Belief Base: "+ engine.getTheory().toString();
        result += "\n\t Goal Base: "+ goalBase.toString();
        result += "\n\t Capability Base: "+ capabilityBase.toString();
        result += "\n\t Plan Base: "+ planBase.toString();
        result += "\n\t Goal Planning Rule Base: "+ goalPlanningRuleBase.toString();
        result += "\n\t Plan Revision Rule Base: "+ planRevisionRuleBase.toString();
        result += ">";
        return result;
    }

    public static void main(String[] args)throws InvalidTheoryException,
            MalformedGoalException, NoSolutionException, NoMoreSolutionException {

        test2();
    }

    public static void test1() throws MalformedGoalException, NoSolutionException, NoMoreSolutionException {
        System.out.println();
        Atom x0 = new Atom("x0");
        Atom x1 = new Atom("x1");
        Atom x2 = new Atom("X2");

        Env env = new Env();
        env.changeVal("X2", "12");

        ArrayList<Atom> list1 = new ArrayList<>();
        list1.add(x0);
        list1.add(x1);
        list1.add(x2);
        VpredClause clause1 = new VpredClause("test", list1);

        Atom x3 = new Atom("x3");
        Atom x4 = new Atom("x4");
        Atom x5 = new Atom("X5");

        ArrayList<Atom> list2 = new ArrayList<>();
        list2.add(x3);
        list2.add(x4);
        list2.add(x5);
        VpredClause clause2 = new VpredClause("test", list2);

        Literal l1 = new Literal(false, clause1);
        Literal l2 = new Literal(false, clause2);

        wffBinary wffclause = new wffBinary(true, l1, l2);

        TrueQuery l3 = new TrueQuery();
        wffBinary wffclause0 = new wffBinary(true, wffclause, l3);
//        System.out.println(wffclause0.toProlog());

        ArrayList<String> argu = new ArrayList<>();
        ArrayList<Literal> post = new ArrayList<>();
        argu.add("X");
        argu.add("Y");
        post.add(l1);
        post.add(l2);
        Capability cap = new Capability(wffclause0, "capName", argu, post);
        System.out.println(cap);
        Prolog engine = new Prolog();
        Theory theory1 = new Theory("test(x0,x1,12).");
        Theory theory2 = new Theory("test(x3,x4,36).");
        engine.addTheory(theory1);
        engine.addTheory(theory2);
        engine.addTheory(new Theory("result."));
        SolveInfo info = wffclause0.performQuery(env,engine);
        while (info.isSuccess()) {
            System.out.println("solution: " + info.getSolution() +
                    " - bindings: " + info);
            if (engine.hasOpenAlternatives()) {
                info = engine.solveNext();
            } else {
                break;
            }
        }
    }
    public static void test2(){
        CodePosition pos = new CodePosition(4,false);
        CodePosition level1 = new CodePosition(6,false);
        CodePosition level2 = new CodePosition(0,true);
        CodePosition level3 = new CodePosition(3,false);
        pos.setNextLevel(level1);
        level1.setNextLevel(level2);
        level2.setNextLevel(level3);
        System.out.println(pos.toString());
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



class Atom{
    private String name;

    public Atom(String name){
        this.name = name;
    }
    @Override
    public String toString() {
        return this.name;
    }
    public Struct toProlog(){
        return new Struct(this.name);
    }

    public boolean isVar() {
        return Character.isUpperCase(this.name.charAt(0)) || this.name.charAt(0) == '_';
    }
}

class GpredClause{
    protected String predicate;
    protected ArrayList<Atom> arguments;
    public GpredClause(String predicate, ArrayList<Atom> arguments){
        this.predicate = predicate;
        this.arguments = arguments;
    }

    @Override
    public String toString(){
        StringBuilder result = new StringBuilder();
        result.append(predicate + "(");
        String prefix = "";
        for(Atom x: arguments){
            result.append(prefix);
            prefix = ",";
            result.append(x.toString());
        }
        result.append(")");
        return result.toString();
    }

    public Struct toProlog(){
        return new Struct(toString());
    }
}

class VpredClause extends GpredClause{

    public VpredClause(String predicate, ArrayList<Atom> arguments){
        super(predicate, arguments);
    }
    public String toString(Env env){
        StringBuilder result = new StringBuilder();
        result.append(predicate + "(");
        String prefix = "";
        for(Atom x: arguments){
            result.append(prefix);
            prefix = ",";
            if(x.isVar()){
                String temp = env.getVal(x.toString());
                result.append(temp == null ? x.toString() : temp);
            }else{
                result.append(x.toString());
            }
        }
        result.append(")");
        return result.toString();
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

    public void performChange(Env env, Prolog engine){
        if(isNeg){
            engine.getTheoryManager().retract(toString(env));
        }else{
            engine.addTheory(new Theory(toString(env)));
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
    ArrayList<Boolean> locked;

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
        return flag;
    }
    public boolean blockGoal(GpredClause goal){
        boolean flag = false;
        int i = 0;
        for(GpredClause clause: subgoals){
            flag = !locked.get(i) && clause.toString().equals(goal.toString());
            i += 1;
            if(flag){
                locked.set(i,true);
                break;
            }
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
        StringBuilder result = new StringBuilder();
        for(Goal goal: goals){
            result.append(prefix);
            prefix = "\n";
            result.append(goal.toString(indent+"\t"));
        }
        result.append("\n"+indent+">");
        return result.toString();
    }

    public void checkGoals(Prolog engine){
        for(Goal goal : goals){
            if(goal.checkGoals(engine)){
                goals.remove(goal);
            }
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
        StringBuilder result = new StringBuilder(indent);
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
        result.append("\n"+indent+">");
        return result.toString();
    }

    public void initial(Prolog engine){
        for(String hornClause : hornClauses){
            Theory theory = new Theory(hornClause);
            engine.addTheory(theory);
        }
        for(GpredClause gpredClause : gClauses){
            Theory theory = new Theory(gpredClause.toString());
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
        StringBuilder result = new StringBuilder(indent+"<Capability: " + name +"\n");
        result.append("(");
        String prefix = "";
        for(String arg : arguments){
            result.append(prefix);
            prefix = ",";
            result.append(arg);
        }
        result.append(")"+indent+"\n{");
        result.append(precondition.toString());
        result.append("}"+indent+"\n{");
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

    boolean perform(Prolog engine, Env env) throws MalformedGoalException, NoSolutionException {
        SolveInfo info = precondition.performQuery(env, engine);
        if(info.isSuccess()){
            List<Var> vars = info.getBindingVars();
            for (Var var : vars){
                String var_n = var.getName();
                String val_n = var.getTerm().toString();
                env.changeVal(var_n, val_n);
            }
            for(Literal cond :postcondition){
                cond.performChange(env,engine);
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
        StringBuilder result = new StringBuilder(indent);
        String prefix = "";
        for(Capability cap: capabilities){
            result.append(prefix);
            prefix = ";\n";
            result.append(cap.toString(indent));
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
    abstract public int oneStep(Env env, Prolog engine) throws NoSolutionException, MalformedGoalException;
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
    public BasicPlan findByPos(ArrayList<Integer> x){
        if(x.size() == 0){
            return null;
        }else if(x.get(0) >= components.size()) {
            return null;
        }else if(x.size() == 1) {
            return components.get(x.get(0));
        }else{
            int first = x.remove(0);
            return ((Sequential)components.get(first)).findByPos(x);
        }
    }

    @Override
    public PlanType type() {return PlanType.SEQUENTIAL;}

    public void replace(ArrayList<Integer> start, int length, SeqPlan newPlan){
        if(start.size() == 1){
            int start_idx = start.get(0);
            ArrayList<BasicPlan> first_part = new ArrayList<>(components.subList(0, start_idx));
            ArrayList<BasicPlan> second_part = newPlan.components;
            ArrayList<BasicPlan> third_part = new ArrayList<>(components.subList(start_idx+length, components.size()));
            components = new ArrayList<>();
            components.addAll(first_part);
            components.addAll(second_part);
            components.addAll(third_part);
        }else{
            int first = start.remove(0);
            ((Sequential)components.get(first)).replace(start, length, newPlan);
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
    public int oneStep(Env env, Prolog engine) throws MalformedGoalException, NoSolutionException {
        int result = this.components.get(currentPos).oneStep(env,engine);
        if(result == 1){
            currentPos += 1;
            if(currentPos == components.size()){
                currentPos = 0;
                return 1;
            }
        }
        return result;
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
    public int oneStep(Env env, Prolog engine) {
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
    public int oneStep(Env env, Prolog engine) throws NoSolutionException, MalformedGoalException {
        if(this.cap == null){
            return -2;
        }
        boolean flag = this.cap.perform(engine, env);
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
    ArrayList<Atom> arguments;

    public SendAction(ArrayList<Atom> arguments){
        this.arguments = arguments;
    }

    @Override
    public int oneStep(Env env, Prolog engine) throws NoSolutionException, MalformedGoalException {
        return 1;
    }

    @Override
    public String toString(String indent) {
        StringBuilder result = new StringBuilder(indent);
        result.append("<SendAction: argument = (");
        String prefix = "";
        if(arguments == null){
            result.append("NULL!");
        }else{
            for(Atom x: arguments){
                result.append(prefix);
                prefix = ",";
                if(x == null){
                    result.append("NULL");
                }
                else{
                    result.append(x.toString());
                }
            }
        }
        result.append(")>");
        return result.toString();
    }

    @Override
    public String toString(Env env, String indent) {
        StringBuilder result = new StringBuilder(indent);
        result.append("<SendAction: argument = (");
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
}

class TestAction extends BasicPlan{
    private Query query;
    public TestAction(Query query){
        this.query = query;
    }

    @Override
    public int oneStep(Env env, Prolog engine) throws NoSolutionException, MalformedGoalException {
        SolveInfo info = this.query.performQuery(env, engine);
        if(info.isSuccess()){
            List<Var> vars = info.getBindingVars();
            for (Var var : vars){
                String var_n = var.getName();
                String val_n = var.getTerm().toString();
                env.changeVal(var_n, val_n);
            }
            return 1;
        }else{
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
    public int oneStep(Env env, Prolog engine) throws NoSolutionException, MalformedGoalException {
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
            }
            else{
                this.insideEnv = new Env();
                for(String var: env.getVarList()){
                    insideEnv.changeVal(var, env.getVal(var));
                }
            }
            currentPos = 1;
            return 0;
        }
        int result = this.components.get(currCond ? 0 : 1).oneStep(insideEnv, engine);
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
        result.append("then: \n");
        result.append(this.components.get(0).toString("\t"+indent));
        result.append("else: \n");
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
    public int oneStep(Env env, Prolog engine) throws NoSolutionException, MalformedGoalException {
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
                return 0;
            }
            else{
                insideEnv = null;
                return 1;
            }
        }

        int result = this.components.get(currentPos).oneStep(insideEnv,engine);
        if(result == 1){
            currentPos += 1;
            if(currentPos == components.size()+1){
                currentPos = 0;
                return 0;
            }
        }
        return result;
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

    public int oneStep(Prolog engine) throws NoSolutionException, MalformedGoalException {
        return this.plan.oneStep(insideEnv, engine);
    }

    public void complete(Prolog engine){
        if(this.associatedGoal != null){
            engine.addTheory(new Theory(this.associatedGoal.toString()));
        }
    }

    private void match(){

    }

    public boolean revisePlan(SeqPlan oldPlan, SeqPlan newPlan, Env env){
        ArrayList<BasicPlan> subPlans = oldPlan.getComponents();
        for(BasicPlan plan : subPlans){
            for(BasicPlan orgPlan : this.plan.components){
                //orgPlan =
                //TODO:: Implement it!
            }
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
        StringBuilder result = new StringBuilder(indent);
        String prefix = "";
        for(Plan plan: plans){
            result.append(prefix);
            prefix = "\n";
            result.append(plan.toString(indent+"\t"));
        }
        return result.toString();
    }

    public void addPlan(Plan plan){
        plans.add(plan);
        status.add(0);
    }

    public int oneStep(Prolog engine) throws MalformedGoalException, NoSolutionException {
        boolean flag = false;
        for(int i = 0; i<status.size(); i++){
            if(status.get(i) != -1){
                flag = true;
                int result = plans.get(i).oneStep(engine);
                status.set(i,result);
                if(result == 1){
                    plans.get(i).complete(engine);
                    plans.remove(i);
                    status.remove(i);
                }
                break;
            }
        }
        return flag ? 1 : 0;
    }

    public boolean revisePlans(SeqPlan oldPlan, SeqPlan newPlan, Env env){
        for(Plan plan : plans){
            plan.revisePlan(oldPlan, newPlan, env);
        }
        return true;
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
    public boolean execute(GoalBase goalBase, PlanBase planBase, Prolog engine) throws MalformedGoalException, NoSolutionException {

        boolean result = goalBase.checkExist(this.goal);
        Env env = new Env();
        SolveInfo info = condition.performQuery(env,engine);
        if(result && info.isSuccess()){
            List<Var> vars = info.getBindingVars();
            for (Var var : vars){
                String var_n = var.getName();
                String val_n = var.getTerm().toString();
                env.changeVal(var_n, val_n);
            }
            planBase.addPlan(new Plan(plan, goal, env));
        }
        return result && info.isSuccess();
    }
    @Override
    public String toString(){
        return toString("");
    }
    public String toString(String indent) {
        StringBuilder result = new StringBuilder(indent);
        result.append("<Goal Planning Rule: \n\tGoal:");
        result.append(goal.toString());
        result.append("\n\tCondition: ");
        result.append(condition.toString());
        result.append("\n\tPlan: \n");
        result.append(this.plan.toString(indent+"\t"));
        return result.toString();
    }
}

class GoalPlanningRuleBase{
    private ArrayList<GoalPlanningRule> rules;
    public GoalPlanningRuleBase(ArrayList<GoalPlanningRule> rules){
        this.rules = rules;
    }

    public boolean execute(GoalBase goalBase, PlanBase planBase, Prolog engine) throws MalformedGoalException, NoSolutionException {
        boolean flag = false;
        for(GoalPlanningRule rule : rules){
            flag = rule.execute(goalBase, planBase, engine);
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
        StringBuilder result = new StringBuilder(indent);
        String prefix = "";
        for(GoalPlanningRule rule: rules){
            result.append(prefix);
            prefix = "\n";
            result.append(rule.toString(indent+"\t"));
        }
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
    public boolean execute(PlanBase planBase, Prolog engine) throws MalformedGoalException, NoSolutionException {
        Env env = new Env();
        SolveInfo info = condition.performQuery(env, engine);
        if (info.isSuccess()){
            List<Var> vars = info.getBindingVars();
            for (Var var : vars){
                String var_n = var.getName();
                String val_n = var.getTerm().toString();
                env.changeVal(var_n, val_n);
            }
            return planBase.revisePlans(this.oldPlan,this.newPlan,env);
        }
        return false;
    }
    @Override
    public String toString(){
        return toString("");
    }
    public String toString(String indent) {
        StringBuilder result = new StringBuilder(indent);
        result.append("<Goal Planning Rule: \n\tPlan pattern:\n");
        result.append(oldPlan.toString(indent+"\t"));
        result.append("\n\tCondition: ");
        result.append(condition.toString());
        result.append("\n\tRevised Plan: \n");
        result.append(newPlan.toString(indent+"\t"));
        return result.toString();
    }
}


class PlanRevisionRuleBase{
    private ArrayList<PlanRevisionRule> rules;
    public PlanRevisionRuleBase(ArrayList<PlanRevisionRule> rules){
        this.rules = rules;
    }

    public boolean execute(PlanBase planBase, Prolog engine) throws MalformedGoalException, NoSolutionException{
        boolean flag = false;
        for(PlanRevisionRule rule : rules){
            flag = rule.execute(planBase, engine);
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
        StringBuilder result = new StringBuilder(indent);
        String prefix = "";
        for(PlanRevisionRule rule: rules){
            result.append(prefix);
            prefix = "\n";
            result.append(rule.toString(indent+"\t"));
        }
        return result.toString();
    }
}


enum State{
    ACTIVE,SUSPEND
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