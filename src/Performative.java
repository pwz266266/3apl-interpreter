public enum Performative{
    QUERY,
    INFORM,
    SENDGOAL,
    REQUESTGOAL;

    public static Performative translate(String name){
        switch (name){
            case("query"):
                return QUERY;
            case("inform"):
                return INFORM;
            case("send_goal"):
                return SENDGOAL;
            case("request_goal"):
                return REQUESTGOAL;
        }
        return null;
    }

    public static String toString(Performative performative){
        if(performative == QUERY){
            return "query";
        }
        if(performative == INFORM){
            return "inform";
        }
        if(performative == SENDGOAL){
            return "send_goal";
        }
        if(performative == REQUESTGOAL){
            return "request_goal";
        }
        return null;
    }
}