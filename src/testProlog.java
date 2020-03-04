import alice.tuprolog.*;
import alice.tuprolog.exceptions.*;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

public class testProlog {
    public static void main(String[] args) throws InvalidTheoryException,
            MalformedGoalException, NoSolutionException, NoMoreSolutionException, ClassNotFoundException, IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {

        test0();
//        Class cls = Class.forName("Environment");
//        Environment environment = (Environment) cls.getConstructor().newInstance();
    }

    public static void test0() throws InvalidTheoryException,
            MalformedGoalException, NoSolutionException, NoMoreSolutionException{
        Prolog engine = new Prolog();
        engine.loadLibrary(new ArithmeticLibrary());
        engine.addTheory(new Theory("price(pen,2)."));
        engine.addTheory(new Theory("price(pencil,1)."));
        engine.addTheory(new Theory("money(0)."));
        engine.addTheory(new Theory("goods(pen,6)."));
        engine.addTheory(new Theory("goods(pencil,2)."));
        engine.addTheory(new Theory("[goods(pencil,2), goods(pen,6)]."));
        engine.addTheory(new Theory("likes(mary,pizza)."));
        engine.addTheory(new Theory("likes(marco,pizza)."));
        engine.addTheory(new Theory("likes(Human,pizza) :- italian(Human)."));
        engine.addTheory(new Theory("italian(marco)."));
        engine.solve("money(0).");
//        SolveInfo info = engine.solve("(price(pencil,Price) , goods(pencil,Count), sub(Count,1,NCount)).");
        Env env = new Env();

        SolveInfo info = engine.solve("findall(Person, likes(Person, pizza), Bag).");
//                sum(1,2,A).
//                sub(1,2,B).
//                div(4,6,C).
//                div(4.0,6.0,D).
//                mul(3,4,E).
//                pow(4,6,F).
//                root(12,G).
        if(!info.isSuccess()){
            System.out.println("Not successful.");
        }
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
    public static void test1() throws InvalidTheoryException,
            MalformedGoalException, NoSolutionException, NoMoreSolutionException{
        Prolog engine = new Prolog();
        Struct clause1 = new Struct(":-", new Struct("p",new Var("X")),
                new Struct("q",new Var("X")));
        Struct clause2 = new Struct(":-", new Struct("q",new Int(0)),
                new Struct("true"));
        Struct clauseList = new Struct(clause1,
                new Struct(clause2, new Struct()));
        Theory t = new Theory(clauseList);
        engine.addTheory(t);
        engine.addTheory(new Theory("testing(testing2(12),3)."));
        SolveInfo info = engine.solve("testing(A,3)");
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
    public static void test2() throws InvalidTheoryException,
            MalformedGoalException, NoSolutionException, NoMoreSolutionException{
        Prolog engine = new Prolog();
        Int a = new Int(0);
        Int b = new Int(1);
        Int c = new Int(2);
        ArrayList<Term> x = new ArrayList<>();
        x.add(a);
        x.add(b);
        x.add(c);
        Term[] buffer = new Term[x.size()];
        Struct c1 = new Struct("p", x.toArray(new Term[x.size()]));
        Struct clauseList = new Struct(c1,
                new Struct());
        engine.addTheory(new Theory(clauseList));
        Var d = new Var("X");
        Var e = new Var("Y");
        Var f = new Var("Z");
        ArrayList<Term> y = new ArrayList<>();
        y.add(d);
        y.add(e);
        y.add(f);
        Struct c2 = new Struct("p", y.toArray(new Term[y.size()]));
        clauseList = new Struct(new Struct("a"),
                new Struct());
        engine.addTheory(new Theory(clauseList));
        SolveInfo info = engine.solve("b");

        System.out.println(info);

    }
    public static void test3() throws InvalidTheoryException,
            MalformedGoalException, NoSolutionException, NoMoreSolutionException{
        Prolog engine = new Prolog();
        Int a = new Int(0);
        Int b = new Int(1);
        Int c = new Int(2);
        ArrayList<Term> x = new ArrayList<>();
        x.add(a);
        x.add(b);
        x.add(c);
        Term[] buffer = new Term[x.size()];
        Struct c1 = new Struct("p", x.toArray(new Term[x.size()]));
        Struct clauseList = new Struct(c1,
                new Struct());
        engine.addTheory(new Theory(clauseList));
        Var d = new Var("X");
        Var e = new Var("Y");
        Var f = new Var("Z");
        ArrayList<Term> y = new ArrayList<>();
        y.add(d);
        y.add(e);
        y.add(f);
        Struct c2 = new Struct("p", y.toArray(new Term[y.size()]));

        SolveInfo info = engine.solve(c2);
        if(info.isSuccess()){
            System.out.println(info.getBindingVars().get(0).getTerm());
        }
        System.out.println(info);

    }
}