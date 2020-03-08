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

        engine.addTheory(new Theory("    takeBins(bins(A)) :- findall(position(X,Y,attribute(Type,Task)), (received_env(position(X,Y,bin,attribute(Type,Task))),grt(Task,0)), A).\n" +
                "    takeStations(stations(A)) :- findall(position(X,Y,Atr), received_env(position(X,Y,station,Atr)), A).\n" +
                "    takeRecharges(recharges(A)) :- findall(position(X,Y,Atr), received_env(position(X,Y,recharge,Atr)), A).\n" +
                "    takeSelf(self(X,Y,A)) :- received_env(position(X,Y,self,A)).\n" +
                "\n" +
                "    maximum(X,Y,X) :- geq(X,Y).\n" +
                "    maximum(X,Y,Y) :- grt(Y,X).\n" +
                "\n" +
                "    minimum(X,Y,Y) :- geq(X,Y).\n" +
                "    minimum(X,Y,X) :- grt(Y,X).\n" +
                "\n" +
                "    maxDist((X1,Y1,Dist1),(X2,Y2,Dist2),(X1,Y1,Dist1)) :- geq(Dist1,Dist2).\n" +
                "    maxDist((X1,Y1,Dist1),(X2,Y2,Dist2),(X2,Y2,Dist2)) :- grt(Dist2,Dist1).\n" +
                "\n" +
                "    minDist((X1,Y1,Dist1),(X2,Y2,Dist2),(X2,Y2,Dist2)) :- geq(Dist1,Dist2).\n" +
                "    minDist((X1,Y1,Dist1),(X2,Y2,Dist2),(X1,Y1,Dist1)) :- grt(Dist2,Dist1).\n" +
                "\n" +
                "    dist(X1,X2,Y1,Y2,Dist) :- sub(X1,X2,X3), sub(Y1,Y2,Y3), abs(X3,X), abs(Y3,Y), maximum(X,Y,Dist).\n" +
                "\n" +
                "    getScore(position(X,Y,attribute(Type)), score(X,Y,Score)) :- (self(Xs,Ys,attribute(none, SCarry, SBattery)) ; self(Xs,Ys,attribute(Type, SCarry, SBattery))),dist(X,Xs,Y,Ys,Dist), sum(SCarry, 0.0, Carry), \\=(Dist,0), div(Carry,Dist,Score), grt(Score,0).\n" +
                "    getScore(position(X,Y,attribute(Type,Task)), score(X,Y,Score)) :- (self(Xs,Ys,attribute(none, SCarry, SBattery)) ; self(Xs,Ys,attribute(Type, SCarry, SBattery))),dist(X,Xs,Y,Ys,Dist), sum(Task,0.0,Task1),sub(200.0,SCarry,Left), minimum(Left,Task1,Take), div(Take,Dist,Score).\n" +
                "\n" +
                "    getScores(Result) :- bins(A), stations(B), append(A,B,List), scoresHelper(List,Result).\n" +
                "    scoresHelper([],[]).\n" +
                "    scoresHelper([A|List], [(X,Y,Score)|Result]) :- getScore(A,score(X,Y,Score)), scoresHelper(List,Result).\n" +
                "    scoresHelper([A|List], Result):- \\+ getScore(A,B), scoresHelper(List,Result).\n" +
                "\n" +
                "    rechargeDist(X,Y,[],[]).\n" +
                "    rechargeDist(X,Y,[position(X1,Y1,Atr)|List],[(X1,Y1,Dist)|Result]) :- dist(X1,Y1,X,Y,Dist), rechargeDist(X,Y,List, Result).\n" +
                "    rechargeDist(Result) :- self(X,Y,Atr), recharges(List), rechargeDist(X,Y,List, Result).\n" +
                "\n" +
                "    mini([(X,Y,Min)], X, Y, Min).\n" +
                "    mini([(X1,Y1,Dist1),(X2,Y2,Dist2)|Rest],X3,Y3,Dist3) :- mini([(X2,Y2,Dist2)|Rest],X4,Y4,Dist4), minDist((X1,Y1,Dist1),(X4,Y4,Dist4),(X3,Y3,Dist3)).\n" +
                "\n" +
                "    maxi([(X,Y,Max)], X, Y, Max).\n" +
                "    maxi([(X1,Y1,Dist1),(X2,Y2,Dist2)|Rest],X3,Y3,Dist3) :- maxi([(X2,Y2,Dist2)|Rest],X4,Y4,Dist4), maxDist((X1,Y1,Dist1),(X4,Y4,Dist4),(X3,Y3,Dist3)).\n" +
                "\n" +
                "    maxScore(X,Y,Max) :- getScores(Result), maxi(Result,X,Y,Max).\n" +
                "    minRecharge(X,Y,Min) :- rechargeDist(Result), mini(Result, X, Y, Min).\n" +
                "\n" +
                "    goto(X,Y,left):- self(Xs,Ys,Atr), grt(Xs,X), eql(Ys,Y).\n" +
                "    goto(X,Y,right):- self(Xs,Ys,Atr), grt(X,Xs), eql(Ys,Y).\n" +
                "    goto(X,Y,up):- self(Xs,Ys,Atr), grt(Y,Ys), eql(Xs,X).\n" +
                "    goto(X,Y,down):- self(Xs,Ys,Atr), grt(Ys,Y), eql(Xs,X).\n" +
                "    goto(X,Y,upleft):- self(Xs,Ys,Atr), grt(Y,Ys), grt(Xs,X).\n" +
                "    goto(X,Y,upright):- self(Xs,Ys,Atr), grt(Y,Ys), grt(X,Xs).\n" +
                "    goto(X,Y,downleft):- self(Xs,Ys,Atr), grt(Ys,Y), grt(Xs,X).\n" +
                "    goto(X,Y,downright):- self(Xs,Ys,Atr), grt(Ys,Y), grt(X,Xs).\n"));

        engine.addTheory(new Theory("stations([position(530,710,attribute(recycling)),position(560,680,attribute(waste)),position(560,730,attribute(recycling)),position(575,490,attribute(waste)),position(590,550,attribute(recycling)),position(590,680,attribute(waste)),position(600,480,attribute(waste)),position(605,675,attribute(waste)),position(620,440,attribute(waste)),position(635,610,attribute(waste)),position(640,685,attribute(recycling)),position(650,545,attribute(recycling)),position(660,565,attribute(recycling)),position(680,590,attribute(waste)),position(690,515,attribute(waste)),position(705,695,attribute(waste)),position(710,510,attribute(waste)),position(725,580,attribute(recycling)),position(735,460,attribute(recycling)),position(735,625,attribute(recycling)),position(740,735,attribute(waste)),position(750,645,attribute(recycling)),position(755,440,attribute(waste)),position(770,640,attribute(recycling)),position(800,695,attribute(recycling))]).\n"));
        engine.addTheory(new Theory("self(680,590,attribute(none,0,459)).\n\n"));
        engine.addTheory(new Theory("bins([position(535,720,attribute(waste,34)),position(540,565,attribute(waste,64)),position(545,620,attribute(recycling,42)),position(550,655,attribute(recycling,93)),position(555,515,attribute(recycling,53)),position(555,730,attribute(recycling,75)),position(560,650,attribute(waste,8)),position(570,545,attribute(recycling,6)),position(575,505,attribute(recycling,44)),position(580,530,attribute(recycling,40)),position(595,680,attribute(recycling,95)),position(605,605,attribute(waste,84)),position(610,540,attribute(recycling,62)),position(625,640,attribute(waste,44)),position(630,545,attribute(waste,51)),position(645,600,attribute(recycling,91)),position(650,640,attribute(waste,38)),position(650,740,attribute(waste,91)),position(655,535,attribute(waste,52)),position(670,485,attribute(recycling,93)),position(680,540,attribute(recycling,19)),position(685,495,attribute(waste,57)),position(690,680,attribute(recycling,75)),position(695,635,attribute(waste,23)),position(700,615,attribute(recycling,69)),position(710,605,attribute(recycling,94)),position(710,685,attribute(waste,99)),position(710,720,attribute(recycling,74)),position(715,465,attribute(waste,58)),position(720,585,attribute(waste,92)),position(720,705,attribute(waste,58)),position(725,535,attribute(waste,60)),position(725,610,attribute(recycling,66)),position(730,725,attribute(waste,27)),position(740,590,attribute(recycling,91)),position(745,460,attribute(recycling,50)),position(745,710,attribute(waste,72)),position(755,645,attribute(recycling,56)),position(765,445,attribute(waste,53)),position(770,545,attribute(recycling,49)),position(775,540,attribute(recycling,38)),position(780,555,attribute(recycling,40)),position(780,735,attribute(waste,36)),position(785,560,attribute(waste,85)),position(785,565,attribute(recycling,62)),position(785,665,attribute(recycling,57)),position(790,485,attribute(recycling,49)),position(795,485,attribute(recycling,88)),position(795,700,attribute(recycling,87)),position(805,445,attribute(recycling,13)),position(810,530,attribute(recycling,37)),position(810,630,attribute(waste,7)),position(810,640,attribute(recycling,53)),position(810,700,attribute(recycling,37)),position(815,535,attribute(recycling,30)),position(815,540,attribute(recycling,88)),position(815,695,attribute(recycling,23)),position(820,545,attribute(waste,99)),position(820,595,attribute(waste,30))]).\n"));
        engine.addTheory(new Theory("recharges([position(575,630,attribute),position(630,600,attribute),position(655,685,attribute),position(660,710,attribute),position(700,655,attribute),position(795,565,attribute),position(815,460,attribute)]).\n"));
//        engine.addTheory(new Theory("likes(marco,pizza)."));
//        engine.addTheory(new Theory("likes(Human,pizza) :- italian(Human)."));
//        engine.addTheory(new Theory("italian(marco)."));
//        engine.solve("price(pen,2).");
//        SolveInfo info = engine.solve("(price(pencil,Price) , goods(pencil,Count), sub(Count,1,NCount)).");
        Env env = new Env();
//        SolveInfo info = engine.solve("findall(price(Item,Price), price(Item, Price), Bag).");
        SolveInfo info = engine.solve("minRecharge(X,Y,Min).");
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