import alice.tuprolog.*;
import alice.tuprolog.Float;
import alice.tuprolog.Number;

public class ArithmeticLibrary extends Library {

    public boolean sum_3(Term arg0, Term arg1, Term arg2){
        float n0;
        float n1;
        float n2;
        if(arg0.isGround() && arg1.isGround() && arg2.isGround()){
            n0 = (Number.createNumber(arg0.getTerm().toString())).floatValue();
            n1 = (Number.createNumber(arg1.getTerm().toString())).floatValue();
            n2 = (Number.createNumber(arg2.getTerm().toString())).floatValue();
            if(((Number)arg0.getTerm()).isInteger() && ((Number)arg1.getTerm()).isInteger() && ((Number)arg2.getTerm()).isInteger()){
                return ((int)n0) + ((int)n1) == ((int)n2);
            }
            return n0 + n1 == n2;
        }else if(arg0.isGround() && arg1.isGround()){
            n0 = (Number.createNumber(arg0.getTerm().toString())).floatValue();
            n1 = (Number.createNumber(arg1.getTerm().toString())).floatValue();
            if(((Number)arg0.getTerm()).isInteger() && ((Number)arg1.getTerm()).isInteger()){
                return arg2.unify(getEngine(),new Int((int) (n0+n1)));
            }
            return arg2.unify(getEngine(),new Float(n0+n1));
        }else if(arg0.isGround() && arg2.isGround()){
            n0 = (Number.createNumber(arg0.getTerm().toString())).floatValue();
            n2 = (Number.createNumber(arg2.getTerm().toString())).floatValue();
            if(((Number)arg0.getTerm()).isInteger() && ((Number)arg2.getTerm()).isInteger()){
                return arg1.unify(getEngine(),new Int((int) (n2 - n0)));
            }
            return arg1.unify(getEngine(),new Float(n2 - n0));
        }else if(arg1.isGround() && arg2.isGround()){
            n1 = (Number.createNumber(arg1.getTerm().toString())).floatValue();
            n2 = (Number.createNumber(arg2.getTerm().toString())).floatValue();
            if(((Number)arg2.getTerm()).isInteger() && ((Number)arg1.getTerm()).isInteger()){
                return arg0.unify(getEngine(),new Int((int) (n2 - n1)));
            }
            return arg0.unify(getEngine(),new Float(n2 - n1));
        }
        return false;
    }

    public boolean sub_3(Term arg0, Term arg1, Term arg2){
        float n0;
        float n1;
        float n2;
        if(arg0.isGround() && arg1.isGround() && arg2.isGround()){
            n0 = (Number.createNumber(arg0.getTerm().toString())).floatValue();
            n1 = (Number.createNumber(arg1.getTerm().toString())).floatValue();
            n2 = (Number.createNumber(arg2.getTerm().toString())).floatValue();
            if(((Number)arg0.getTerm()).isInteger() && ((Number)arg1.getTerm()).isInteger() && ((Number)arg2.getTerm()).isInteger()){
                return ((int)n0) - ((int)n1) == ((int)n2);
            }
            return n0 - n1 == n2;
        }else if(arg0.isGround() && arg1.isGround()){
            n0 = (Number.createNumber(arg0.getTerm().toString())).floatValue();
            n1 = (Number.createNumber(arg1.getTerm().toString())).floatValue();
            if(((Number)arg0.getTerm()).isInteger() && ((Number)arg1.getTerm()).isInteger()){
                return arg2.unify(getEngine(),new Int((int) (n0-n1)));
            }
            return arg2.unify(getEngine(),new Float(n0-n1));
        }else if(arg0.isGround() && arg2.isGround()){
            n0 = (Number.createNumber(arg0.getTerm().toString())).floatValue();
            n2 = (Number.createNumber(arg2.getTerm().toString())).floatValue();
            if(((Number)arg0.getTerm()).isInteger() && ((Number)arg2.getTerm()).isInteger()){
                return arg1.unify(getEngine(),new Int((int) (n0 - n2)));
            }
            return arg1.unify(getEngine(),new Float(n0 - n2));
        }else if(arg1.isGround() && arg2.isGround()){
            n1 = (Number.createNumber(arg1.getTerm().toString())).floatValue();
            n2 = (Number.createNumber(arg2.getTerm().toString())).floatValue();
            if(((Number)arg2.getTerm()).isInteger() && ((Number)arg1.getTerm()).isInteger()){
                return arg0.unify(getEngine(),new Int((int) (n1 + n2)));
            }
            return arg0.unify(getEngine(),new Float(n1 + n2));
        }
        return false;
    }

    public boolean div_3(Term arg0, Term arg1, Term arg2){
        float n0;
        float n1;
        float n2;
        if(arg0.isGround() && arg1.isGround() && arg2.isGround()){
            n0 = (Number.createNumber(arg0.getTerm().toString())).floatValue();
            n1 = (Number.createNumber(arg1.getTerm().toString())).floatValue();
            n2 = (Number.createNumber(arg2.getTerm().toString())).floatValue();
            if(((Number)arg0.getTerm()).isInteger() && ((Number)arg1.getTerm()).isInteger() && ((Number)arg2.getTerm()).isInteger()){
                return ((int)n0) / ((int)n1) == ((int)n2);
            }
            return n0 / n1 == n2;
        }else if(arg0.isGround() && arg1.isGround()){
            n0 = (Number.createNumber(arg0.getTerm().toString())).floatValue();
            n1 = (Number.createNumber(arg1.getTerm().toString())).floatValue();
            if(((Number)arg0.getTerm()).isInteger() && ((Number)arg1.getTerm()).isInteger()){
                return arg2.unify(getEngine(),new Int((int) (n0/n1)));
            }
            return arg2.unify(getEngine(),new Float(n0/n1));
        }else if(arg0.isGround() && arg2.isGround()){
            n0 = (Number.createNumber(arg0.getTerm().toString())).floatValue();
            n2 = (Number.createNumber(arg2.getTerm().toString())).floatValue();
            if(((Number)arg0.getTerm()).isInteger() && ((Number)arg2.getTerm()).isInteger()){
                return arg1.unify(getEngine(),new Int((int) (n0 / n2)));
            }
            return arg1.unify(getEngine(),new Float(n0 / n2));
        }else if(arg1.isGround() && arg2.isGround()){
            n1 = (Number.createNumber(arg1.getTerm().toString())).floatValue();
            n2 = (Number.createNumber(arg2.getTerm().toString())).floatValue();
            if(((Number)arg2.getTerm()).isInteger() && ((Number)arg1.getTerm()).isInteger()){
                return arg0.unify(getEngine(),new Int((int) (n1 * n2)));
            }
            return arg0.unify(getEngine(),new Float(n1 * n2));
        }
        return false;
    }

    public boolean mul_3(Term arg0, Term arg1, Term arg2){
        float n0;
        float n1;
        float n2;
        if(arg0.isGround() && arg1.isGround() && arg2.isGround()){
            n0 = (Number.createNumber(arg0.getTerm().toString())).floatValue();
            n1 = (Number.createNumber(arg1.getTerm().toString())).floatValue();
            n2 = (Number.createNumber(arg2.getTerm().toString())).floatValue();
            if(((Number)arg0.getTerm()).isInteger() && ((Number)arg1.getTerm()).isInteger() && ((Number)arg2.getTerm()).isInteger()){
                return ((int)n0) * ((int)n1) == ((int)n2);
            }
            return n0 * n1 == n2;
        }else if(arg0.isGround() && arg1.isGround()){
            n0 = (Number.createNumber(arg0.getTerm().toString())).floatValue();
            n1 = (Number.createNumber(arg1.getTerm().toString())).floatValue();
            if(((Number)arg0.getTerm()).isInteger() && ((Number)arg1.getTerm()).isInteger()){
                return arg2.unify(getEngine(),new Int((int) (n0*n1)));
            }
            return arg2.unify(getEngine(),new Float(n0*n1));
        }else if(arg0.isGround() && arg2.isGround()){
            n0 = (Number.createNumber(arg0.getTerm().toString())).floatValue();
            n2 = (Number.createNumber(arg2.getTerm().toString())).floatValue();
            if(((Number)arg0.getTerm()).isInteger() && ((Number)arg2.getTerm()).isInteger()){
                return arg1.unify(getEngine(),new Int((int) (n2 / n0)));
            }
            return arg1.unify(getEngine(),new Float(n2 / n0));
        }else if(arg1.isGround() && arg2.isGround()){
            n1 = (Number.createNumber(arg1.getTerm().toString())).floatValue();
            n2 = (Number.createNumber(arg2.getTerm().toString())).floatValue();
            if(((Number)arg1.getTerm()).isInteger() && ((Number)arg2.getTerm()).isInteger()){
                return arg0.unify(getEngine(),new Int((int) (n2 / n1)));
            }
            return arg0.unify(getEngine(),new Float(n2 / n1));
        }
        return false;
    }

    public boolean pow_3(Term arg0, Term arg1, Term arg2){
        float n0;
        float n1;
        float n2;
        if(arg0.isGround() && arg1.isGround() && arg2.isGround()){
            n0 = (Number.createNumber(arg0.getTerm().toString())).floatValue();
            n1 = (Number.createNumber(arg1.getTerm().toString())).floatValue();
            n2 = (Number.createNumber(arg2.getTerm().toString())).floatValue();
            if(((Number)arg0.getTerm()).isInteger() && ((Number)arg1.getTerm()).isInteger() && ((Number)arg2.getTerm()).isInteger()){
                return (int)Math.pow((int)n0, (int)n1) == ((int)n2);
            }
            return Math.pow(n0,n1) == n2;
        }else if(arg0.isGround() && arg1.isGround()){
            n0 = (Number.createNumber(arg0.getTerm().toString())).floatValue();
            n1 = (Number.createNumber(arg1.getTerm().toString())).floatValue();
            if(((Number)arg0.getTerm()).isInteger() && ((Number)arg1.getTerm()).isInteger()){
                return arg2.unify(getEngine(),new Int((int) Math.pow((int)n0, (int)n1)));
            }
            return arg2.unify(getEngine(),new Float((float) Math.pow(n0, n1)));
        }else if(arg0.isGround() && arg2.isGround()){
            n0 = (Number.createNumber(arg0.getTerm().toString())).floatValue();
            n2 = (Number.createNumber(arg2.getTerm().toString())).floatValue();
            return arg1.unify(getEngine(),new Float((float) (Math.log(n2)/Math.log(n0))));
        }else if(arg1.isGround() && arg2.isGround()){
            n1 = (Number.createNumber(arg1.getTerm().toString())).floatValue();
            n2 = (Number.createNumber(arg2.getTerm().toString())).floatValue();
            return arg0.unify(getEngine(),new Float((float) Math.pow(n2, 1/n1)));
        }
        return false;
    }

    public boolean root_3(Term arg0, Term arg1, Term arg2){
        float n0;
        float n1;
        float n2;
        if(arg0.isGround() && arg1.isGround() && arg2.isGround()){
            n0 = (Number.createNumber(arg0.getTerm().toString())).floatValue();
            n1 = (Number.createNumber(arg1.getTerm().toString())).floatValue();
            n2 = (Number.createNumber(arg2.getTerm().toString())).floatValue();
            return Math.pow(n0, 1/n1) == n2;
        }else if(arg0.isGround() && arg1.isGround()){
            n0 = (Number.createNumber(arg0.getTerm().toString())).floatValue();
            n1 = (Number.createNumber(arg1.getTerm().toString())).floatValue();
            return arg2.unify(getEngine(),new Float((float) Math.pow(n0, 1/n1)));
        }else if(arg0.isGround() && arg2.isGround()){
            n0 = (Number.createNumber(arg0.getTerm().toString())).floatValue();
            n2 = (Number.createNumber(arg2.getTerm().toString())).floatValue();
            return arg1.unify(getEngine(),new Float((float) (Math.log(n0)/Math.log(n2))));
        }else if(arg1.isGround() && arg2.isGround()){
            n1 = (Number.createNumber(arg1.getTerm().toString())).floatValue();
            n2 = (Number.createNumber(arg2.getTerm().toString())).floatValue();
            if(((Number)arg1.getTerm()).isInteger() && ((Number)arg2.getTerm()).isInteger()){
                arg0.unify(getEngine(),new Int((int) Math.pow(n2, n1)));
            }
            return arg0.unify(getEngine(),new Float((float) Math.pow(n2, n1)));
        }
        return false;
    }

    public boolean toInt_2(Term arg0, Var arg1){
        if(arg0.isGround() && arg0.getTerm().isNumber()){
            return arg1.unify(getEngine(), new Int((int)(Number.createNumber(arg0.getTerm().toString())).floatValue()));
        }
        return false;
    }

    public boolean toFloat_2(Term arg0, Var arg1){
        if(arg0.isGround() && arg0.getTerm().isNumber()){
            return arg1.unify(getEngine(), new Float((Number.createNumber(arg0.getTerm().toString())).floatValue()));
        }
        return false;
    }

    public boolean eql_2(Term arg0, Term arg1){
        if(arg0.isGround() && arg1.isGround()){
            return (Number.createNumber(arg0.getTerm().toString())).floatValue()
                     == (Number.createNumber(arg1.getTerm().toString())).floatValue();
        }
        return false;
    }


    public boolean grt_2(Term arg0, Term arg1){
        if(arg0.isGround() && arg1.isGround()){
            return (Number.createNumber(arg0.getTerm().toString())).floatValue()
                    > (Number.createNumber(arg1.getTerm().toString())).floatValue();
        }
        return false;
    }


    public boolean les_2(Term arg0, Term arg1){
        if(arg0.isGround() && arg1.isGround()){
            return (Number.createNumber(arg0.getTerm().toString())).floatValue()
                    < (Number.createNumber(arg1.getTerm().toString())).floatValue();
        }
        return false;
    }


    public boolean geq_2(Term arg0, Term arg1){
        if(arg0.isGround() && arg1.isGround()){
            return (Number.createNumber(arg0.getTerm().toString())).floatValue()
                    >= (Number.createNumber(arg1.getTerm().toString())).floatValue();
        }
        return false;
    }


    public boolean leq_2(Term arg0, Term arg1){
        if(arg0.isGround() && arg1.isGround()){
            return (Number.createNumber(arg0.getTerm().toString())).floatValue()
                    <= (Number.createNumber(arg1.getTerm().toString())).floatValue();
        }
        return false;
    }

    public boolean abs_2(Term arg0, Term arg1){
        if(arg0.isGround()){
            float n0 = (Number.createNumber(arg0.getTerm().toString())).floatValue();
            if(((Number)arg0.getTerm()).isInteger()){
                return arg1.unify(getEngine(),new Int((int) (Math.abs(n0))));
            }
            return arg1.unify(getEngine(),new Float((float) Math.abs(n0)));
        }
        return false;
    }
}
