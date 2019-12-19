import java.lang.reflect.Array;
import java.security.DrbgParameters;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;

import alice.tuprolog.*;
import alice.tuprolog.Float;
import alice.tuprolog.exceptions.InvalidTheoryException;
import alice.tuprolog.exceptions.MalformedGoalException;
import alice.tuprolog.exceptions.NoMoreSolutionException;
import alice.tuprolog.exceptions.NoSolutionException;






public class Agent {
    public static void main(String[] args)throws InvalidTheoryException,
            MalformedGoalException, NoSolutionException, NoMoreSolutionException {
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
//        Prolog engine = new Prolog();
//        Theory theory1 = new Theory("test(x0,x1,12).");
//        Theory theory2 = new Theory("test(x3,x4,36).");
//        engine.addTheory(theory1);
//        engine.addTheory(theory2);
//        engine.addTheory(new Theory("result."));
//        SolveInfo info = wffclause0.performQuery(env,engine);
//        while (info.isSuccess()) {
//            System.out.println("solution: " + info.getSolution() +
//                    " - bindings: " + info);
//            if (engine.hasOpenAlternatives()) {
//                info = engine.solveNext();
//            } else {
//                break;
//            }
//        }
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
        Boolean flag = false;
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
        return null;
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

    public Boolean isVar() {
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
    private Boolean isNeg;

    public Literal(Boolean isNeg, VpredClause clause){
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
    private Boolean isOr;
    private Query argument1;
    private Query argument2;

    public wffBinary(Boolean isOr, Query arg1, Query arg2){
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
        StringBuilder result = new StringBuilder();
        String prefix = "";
        for(GpredClause goal: subgoals){
            result.append(prefix);
            prefix = ",";
            result.append(goal.toString());
        }
        return result.toString();
    }
    public boolean checkGoals(Prolog engine) {
        Boolean flag = false;
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

    public boolean checkExist(GpredClause goal){
        Boolean flag = false;
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
        String prefix = "";
        StringBuilder result = new StringBuilder();
        for(Goal goal: goals){
            result.append(prefix);
            prefix = ";\n";
            result.append(goal.toString());
        }
        return result.toString();
    }

    public void checkGoals(Prolog engine){
        for(Goal goal : goals){
            if(goal.checkGoals(engine)){
                goals.remove(goal);
            }
        }
    }

    public Boolean checkExist(Goal goal){
        Boolean flag = false;
        for(GpredClause subgoal: goal.getSubgoals()){
            Boolean tempFlag = false;
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
}

class BeliefBase{
    private ArrayList<String> hornClauses;
    private ArrayList<GpredClause> gClauses;
    public BeliefBase(ArrayList<String> hornClauses, ArrayList<GpredClause> gClauses){
        this.gClauses = gClauses;
        this.hornClauses = hornClauses;
    }
    public void addToEngine(Prolog engine){
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
        StringBuilder result = new StringBuilder("<Capability: " + name + ">");
        result.append("(");
        String prefix = "";
        for(String arg : arguments){
            result.append(prefix);
            prefix = ",";
            result.append(arg);
        }
        result.append(")\n{");
        result.append(precondition.toString());
        result.append("}\n{");
        prefix = "";
        for(Literal lit : postcondition){
            result.append(prefix);
            prefix = ",";
            result.append(lit.toString());
        }
        result.append("}\n");

        return result.toString();
    }

    public String toString(Env env){
        StringBuilder result = new StringBuilder("<Capability: " + name + ">");
        result.append("(");
        String prefix = "";
        for(String arg : arguments){
            result.append(prefix);
            prefix = ",";
            result.append(arg);
        }
        result.append(")\n{");
        result.append(precondition.toString(env) );
        result.append("}\n{");
        prefix = "";
        for(Literal lit : postcondition){
            result.append(prefix);
            prefix = ",";
            result.append(lit.toString(env));
        }
        result.append("}\n");

        return result.toString();
    }
    Boolean perform(Prolog engine, Env env) throws MalformedGoalException, NoSolutionException {
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
    public String toString() {
        StringBuilder result = new StringBuilder();
        String prefix = "";
        for(Capability cap: capabilities){
            result.append(prefix);
            prefix = ";\n";
            result.append(cap);
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
}

abstract class BasicPlan{
    abstract public int oneStep(Env env, Prolog engine) throws NoSolutionException, MalformedGoalException;
    abstract public String toString(Env env);
    abstract public String toString();
    public void binding(CapabilityBase caps){};
    public void ModifyEnv(Env env){};
}

abstract class Sequential extends BasicPlan{
    ArrayList<BasicPlan> components;
    int currentPos;
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
    public String toString(Env env) {
        StringBuilder result = new StringBuilder();
        result.append("<Sequence: ");
        String prefix = "\n\t";
        for(BasicPlan x: components){
            result.append(prefix);
            result.append(x.toString(env));
        }
        result.append(">");
        return result.toString();
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("<Sequence: ");
        String prefix = "\n\t";
        for(BasicPlan x: components){
            result.append(prefix);
            result.append(x.toString());
        }
        result.append(">");
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
    public String toString(Env env) {
        StringBuilder result = new StringBuilder();
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
    public String toString() {
        StringBuilder result = new StringBuilder();
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
        Boolean flag = this.cap.perform(engine, env);
        if(flag){
            return 1;
        }else{
            return -1;
        }
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("<CapAction: "+ this.predicate +"(");
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
    public String toString(Env env) {
        StringBuilder result = new StringBuilder();
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
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("<SendAction: argument = (");
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
    public String toString(Env env) {
        StringBuilder result = new StringBuilder();
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
    public String toString(Env env) {
        return "<TestAction: "+this.query.toString(env)+"?>";
    }

    @Override
    public String toString(){
        return "<TestAction: "+this.query.toString()+"?>";
    }
}

class IfPlan extends BasicPlan{
    private SeqPlan component;
    private int currentPos;
    private Query condition;
    private Env insideEnv;
    private ArrayList<BasicPlan> ifComponent;
    private ArrayList<BasicPlan> elseComponent;
    public IfPlan(ArrayList<BasicPlan> ifComponent, ArrayList<BasicPlan> elseComponent, Query condition){
        this.condition = condition;
        this.currentPos = 0;
        this.ifComponent = ifComponent;
        this.elseComponent = elseComponent;
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
                return -1;
            }
        }

        if(currentPos == 1){
            int result = this.component.oneStep(insideEnv, engine);
            if(result == 1){
                currentPos = 0;
                return 1;
            }
            return result;
        }
        return -2;
    }

    @Override
    public String toString(Env env) {
        return "<IfPlan: "+this.condition.toString(env)+"\n\t"+this.component.toString(env)+">";
    }

    @Override
    public String toString() {
        return "<IfPlan: "+this.condition.toString()+"\n\t"+this.component.toString()+">";
    }

    @Override
    public void binding(CapabilityBase caps) {
        this.component.binding(caps);
    }

    @Override
    public void ModifyEnv(Env env) {
        for(String var: env.getVarList()){
            insideEnv.changeVal(var, env.getVal(var));
        }
    }
}

class WhilePlan extends BasicPlan{
    private SeqPlan component;
    private int currentPos;
    private Query condition;
    private Env insideEnv;

    public WhilePlan(SeqPlan component, Query condition){
        this.component = component;
        this.condition = condition;
        this.currentPos = 0;
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
                return 1;
            }
        }

        if(currentPos == 1){
            int result = this.component.oneStep(insideEnv, engine);
            if(result == 1){
                currentPos = 0;
                return 0;
            }
            return result;
        }
        return -2;
    }

    @Override
    public String toString(Env env) {
        return "<WhilePlan: "+this.condition.toString(env)+"\n\t"+this.component.toString(env)+">";
    }

    @Override
    public String toString() {
        return "<WhilePlan: "+this.condition.toString()+"\n\t"+this.component.toString()+">";
    }

    @Override
    public void binding(CapabilityBase caps) {
        this.component.binding(caps);
    }

    @Override
    public void ModifyEnv(Env env) {
        for(String var: env.getVarList()){
            insideEnv.changeVal(var, env.getVal(var));
        }
    }
}

class Plan{
    private SeqPlan plan;
    private Goal associatedGoal;
    private int currentPos;
    private Env insideEnv;

    public Plan(SeqPlan plan, Goal associatedGoal, Env insideEnv){
        this.plan = plan;
        this.associatedGoal = associatedGoal;
        currentPos = 0;
        this.insideEnv = insideEnv;
    }

    public Plan(SeqPlan plan, Goal associatedGoal){
        this.plan = plan;
        this.associatedGoal = associatedGoal;
        currentPos = 0;
        insideEnv = new Env();
    }

    public int oneStep(Prolog engine) throws NoSolutionException, MalformedGoalException {
        return this.plan.oneStep(insideEnv, engine);
    }

    @Override
    public String toString() {
        String toprint;
        if(associatedGoal == null){
            toprint = "with no goal";
        }else{
            toprint = "with goal: "+associatedGoal.toString();
        }
        return "<Plan: "+toprint+"\n\t"+this.plan.toString(insideEnv)+">";
    }

    public void binding(CapabilityBase caps) {
        this.plan.binding(caps);
    }

    public void ModifyEnv(Env env) {
        for(String var: env.getVarList()){
            insideEnv.changeVal(var, env.getVal(var));
        }
    }
}


