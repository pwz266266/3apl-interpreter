public abstract class ControllableEntity {
    private Environment env;
    public ControllableEntity(Environment env){
        this.env = env;
        System.out.println("Create an Entity!!!");
    }

    abstract public EnvironmentRespond takeAction(EnvironmentAction action);
}
