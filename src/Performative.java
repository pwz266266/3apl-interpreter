public enum Performative{
    QUERY,
    INFORM,
    PROPOSE,
    CALL_FOR_PROPOSAL,
    SUBSCRIBE,
    UNSUBSCRIBE;

    public static Performative translate(String name){
        switch (name){
            case("Query"):
                return QUERY;
            case("Inform"):
                return INFORM;
            case("Propose"):
                return PROPOSE;
            case("Call_for_proposal"):
                return CALL_FOR_PROPOSAL;
        }
        return null;
    }
}