import alice.tuprolog.*;
import alice.tuprolog.Float;
import alice.tuprolog.Number;

public class ArithmeticLibrary extends Library {

    public boolean sum_3(Number arg0, Number arg1, Var arg2){
        float n0 = arg0.floatValue();
        float n1 = arg1.floatValue();
        if(arg0.isInteger() && arg1.isInteger()){
            return arg2.unify(getEngine(),new Int((int) (n0+n1)));
        }
        return arg2.unify(getEngine(),new Float(n0+n1));
    }

    public boolean sub_3(Number arg0, Number arg1, Var arg2){
        float n0 = arg0.floatValue();
        float n1 = arg1.floatValue();
        if(arg0.isInteger() && arg1.isInteger()){
            return arg2.unify(getEngine(),new Int((int) (n0-n1)));
        }
        return arg2.unify(getEngine(),new Float(n0-n1));
    }

    public boolean div_3(Number arg0, Number arg1, Var arg2){
        float n0 = arg0.floatValue();
        float n1 = arg1.floatValue();
        if(arg0.isInteger() && arg1.isInteger()){
            return arg2.unify(getEngine(),new Int((int) (n0/n1)));
        }
        return arg2.unify(getEngine(),new Float(n0/n1));
    }

    public boolean mul_3(Number arg0, Number arg1, Var arg2){
        float n0 = arg0.floatValue();
        float n1 = arg1.floatValue();
        if(arg0.isInteger() && arg1.isInteger()){
            return arg2.unify(getEngine(),new Int((int) (n0*n1)));
        }
        return arg2.unify(getEngine(),new Float(n0*n1));
    }

    public boolean pow_3(Number arg0, Number arg1, Var arg2){
        float n0 = arg0.floatValue();
        float n1 = arg1.floatValue();
        if(arg0.isInteger() && arg1.isInteger()){
            return arg2.unify(getEngine(),new Int((int) (Math.pow(n0,n1))));
        }
        return arg2.unify(getEngine(),new Float((float) Math.pow(n0,n1)));
    }

    public boolean root_2(Number arg0, Var arg1){
        float n0 = arg0.floatValue();
        return arg1.unify(getEngine(),new Float((float) Math.sqrt(n0)));
    }
}
