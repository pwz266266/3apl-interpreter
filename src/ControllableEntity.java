public abstract class ControllableEntity {
    static int ID = 1;
    Environment env;
    int thisID = ID++;
    public ControllableEntity(Environment env){
        this.env = env;
        System.out.println("Create an Entity!!!");
    }
}
