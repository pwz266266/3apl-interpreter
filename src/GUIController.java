import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

public class GUIController implements Initializable {
    @FXML private TableView serverTable;
    @FXML private TableView containerTable;
    @FXML private TableView agentTable;
    @FXML private TableColumn server_name;
    @FXML private TableColumn server_env;
    @FXML private TableColumn server_stat;
    @FXML private TableColumn containers;
    @FXML private TableColumn agents;
    @FXML private Pane pane;

    private ServerView selectedServer = null;
    private ContainerView selectedContainer = null;
    private AgentView selectedAgent = null;
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        server_env.setEditable(false);
        server_name.setEditable(false);
        server_stat.setEditable(false);
        containers.setEditable(false);
        agents.setEditable(false);
        serverTable.setEditable(false);
        agentTable.setEditable(false);
        containerTable.setEditable(false);
        server_env.setCellValueFactory(new PropertyValueFactory<ServerView, String>("environmentName"));
        server_name.setCellValueFactory(new PropertyValueFactory<ServerView, String>("serverName"));
        server_stat.setCellValueFactory(new PropertyValueFactory<ServerView, String>("status"));
        containers.setCellValueFactory(new PropertyValueFactory<ServerView, String>("containerName"));
        agents.setCellValueFactory(new PropertyValueFactory<ServerView, String>("agentName"));
        serverTable.setOnMouseClicked((MouseEvent event) -> {
            if(event.getButton().equals(MouseButton.PRIMARY)){
                selectedServer = (ServerView) serverTable.getSelectionModel().getSelectedItem();
                selectedContainer = null;
                selectedAgent = null;
                refreshContainerTable();
                refreshAgentTable();
            }
        });
        containerTable.setOnMouseClicked((MouseEvent event) -> {
            if(event.getButton().equals(MouseButton.PRIMARY)){
                selectedContainer = (ContainerView) containerTable.getSelectionModel().getSelectedItem();
                selectedAgent = null;
                refreshAgentTable();
            }
        });
        agentTable.setOnMouseClicked((MouseEvent event) -> {
            if(event.getButton().equals(MouseButton.PRIMARY)){
                selectedAgent = (AgentView) agentTable.getSelectionModel().getSelectedItem();
            }
        });
    }

    public void addServer(){
        Server server = ServerFactory.createServer();
        ServerView currentView = new ServerView(server);
        serverTable.getItems().add(currentView);
    }

    public void addContainer() throws IOException {
        if(selectedServer!=null&&!selectedServer.getStatus().equals("Running")) {
            Container container = ContainerFactory.createContainer();
            ContainerView currentView = new ContainerView(container);
            selectedServer.getServer().addContainer(container);
            selectedServer.getContainers().add(currentView);
            containerTable.getItems().add(currentView);
        }
    }

    public void addAgent() throws IOException, ParseException {
        if(selectedContainer!=null&&!selectedServer.getStatus().equals("Running")) {
            Agent agent = TripAPL_parser.compile(new FileChooser().showOpenDialog(pane.getScene().getWindow()));
            AgentView currentView = new AgentView(agent);
            selectedContainer.getContainer().addAgent(agent);
            selectedContainer.getAgents().add(currentView);
            agentTable.getItems().add(currentView);
        }
    }

    public void deleteAgent(){
        if(selectedAgent!=null&&!selectedServer.getStatus().equals("Running")) {
            selectedContainer.getAgents().remove(selectedAgent);
            agentTable.getItems().remove(selectedAgent);
            selectedContainer.getContainer().removeAgent(selectedAgent.getAgent());
            selectedAgent = null;
        }
    }
    public void deleteContainer(){
        if(selectedContainer!=null&&!selectedServer.getStatus().equals("Running")) {
            selectedServer.getContainers().remove(selectedContainer);
            containerTable.getItems().remove(selectedContainer);
            selectedServer.getServer().removeContainer(selectedContainer.getContainer());
            selectedContainer = null;
            refreshAgentTable();
        }
    }
    public void deleteServer(){
        if(selectedServer!=null&&!selectedServer.getStatus().equals("Running")) {
            serverTable.getItems().remove(selectedServer);
            selectedServer = null;
            refreshContainerTable();
            refreshAgentTable();
        }
    }
    private void refreshServerTable(){
        ArrayList<ServerView> buffer = new ArrayList<>(serverTable.getItems());
        serverTable.getItems().clear();
        serverTable.getItems().addAll(buffer);
    }
    private void refreshContainerTable(){
        containerTable.getItems().clear();
        if(selectedServer!=null){
            containerTable.getItems().addAll(selectedServer.getContainers());
        }
    }

    private void refreshAgentTable(){
        agentTable.getItems().clear();
        if(selectedContainer!=null){
            agentTable.getItems().addAll(selectedContainer.getAgents());
        }
    }

    public void startOrstop(){
        if(selectedServer!=null) {
            if (selectedServer.status.get().equals("Suspend")) {
                selectedServer.setStatus("Running");
                selectedServer.getServer().restart();
                selectedServer.getServer().showEnv();
                new Thread(selectedServer.getServer()).start();
            } else {
                selectedServer.setStatus("Suspend");
                selectedServer.getServer().terminate();
            }
            refreshServerTable();
        }
    }

    public void setEnvironment() throws MalformedURLException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        if(selectedServer!=null&&!selectedServer.getStatus().equals("Running")){
            URL url = new FileChooser().showOpenDialog(pane.getScene().getWindow()).toURI().toURL();
            URL[] urls = new URL[]{url};
            String[] strs = url.toString().split("/");
            ClassLoader cl = new URLClassLoader(urls);
            String name = strs[strs.length-1].substring(0,strs[strs.length-1].length()-6);
            Class envClass = cl.loadClass(name);
            selectedServer.setEnvironmentName(name);
            Environment env = (Environment) envClass.getConstructor().newInstance();
            selectedServer.getServer().setEnvironment(env);
            refreshServerTable();
        }
    }

    public void checkStatus() throws IOException {
        if(selectedServer!=null) {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("./Status.fxml"));
            Pane pane = loader.load();
            StatusController controller = loader.getController();
            controller.setServer(selectedServer.getServer());
            Scene scene = new Scene(pane);
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.show();
        }
    }

    public void createEntity(){
        if(selectedServer!=null && selectedServer.getServer().hasEnv()&&!selectedServer.getStatus().equals("Running")){
            Server server = selectedServer.getServer();
            HashMap<String, ArrayList<String>> creatableEntities = server.CreatableEntity();
            Group group = new Group();
            Scene scene = new Scene(group);
            Stage stage = new Stage();
            stage.setScene(scene);
            int y = 5;
            Set<String> entities = creatableEntities.keySet();
            for(String entity : entities){
                int x = 10;
                Text text = new Text(x,y+20,entity);
                x+=text.getScaleX()+160;
                ArrayList<String> variables = creatableEntities.get(entity);
                ArrayList<TextField> fields = new ArrayList<TextField>();
                for(String var : variables){
                    TextField field = new TextField(var);
                    fields.add(field);
                    field.setPrefSize(60,20);
                    field.setLayoutX(x);
                    field.setLayoutY(y);
                    x+=90;
                    group.getChildren().add(field);
                }
                Button button = new Button("Create");
                group.getChildren().add(button);
                button.setLayoutX(x);
                button.setLayoutY(y);
                button.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        ArrayList<String> vars = new ArrayList<>();
                        for(TextField field : fields){
                            vars.add(field.getText());
                        }
                        int id = server.createEntity(entity,vars);
                        if(id>=0 && selectedAgent!=null){
                            server.linkAgentEntity(selectedAgent.getAgent().getFullID(),id);
                        }
                    }
                });
                group.getChildren().add(text);
                y+=30;
            }
            stage.show();
        }
    }

    public void shutdown() {
        System.out.println("Stop");
        for(Object server : this.serverTable.getItems()){
            ((ServerView) server).getServer().terminate();
        }
        Platform.exit();
    }

    public class ServerView{
        private final SimpleStringProperty serverName;
        private final SimpleStringProperty environmentName;
        private final SimpleStringProperty status;
        private final ArrayList<ContainerView> containers;

        private final Server server;

        public ServerView(Server server){
            this.server = server;
            this.serverName = new SimpleStringProperty("Server "+server.getID());
            this.environmentName = new SimpleStringProperty("None");
            this.status = new SimpleStringProperty("Suspend");
            this.containers = new ArrayList<>();
        }

        public String getServerName() {
            return serverName.get();
        }

        public void setServerName(String serverName) {
            this.serverName.set(serverName);
        }

        public String getEnvironmentName() {
            return environmentName.get();
        }


        public void setEnvironmentName(String environmentName) {
            this.environmentName.set(environmentName);
        }

        public String getStatus() {
            return status.get();
        }


        public void setStatus(String status) {
            this.status.set(status);
        }

        public ArrayList<ContainerView> getContainers() {
            return containers;
        }

        public Server getServer() {
            return server;
        }
    }

    public class ContainerView{
        private final ArrayList<AgentView> agents;
        private final SimpleStringProperty containerName;

        private final Container container;

        public ContainerView(Container container){
            this.container = container;
            this.containerName = new SimpleStringProperty("Container "+container.getID());
            this.agents = new ArrayList<>();
        }

        public ArrayList<AgentView> getAgents() {
            return agents;
        }

        public String getContainerName() {
            return containerName.get();
        }

        public void setContainerName(String containerName) {
            this.containerName.set(containerName);
        }

        public Container getContainer() {
            return container;
        }
    }

    public class AgentView{
        private final SimpleStringProperty AgentName;

        private final Agent agent;

        public AgentView(Agent agent){
            this.agent = agent;
            this.AgentName = new SimpleStringProperty("Agent "+agent.getID()+" ("+agent.getName()+")");
        }
        public String getAgentName() {
            return AgentName.get();
        }

        public void setAgentName(String agentName) {
            this.AgentName.set(agentName);
        }

        public Agent getAgent() {
            return agent;
        }
    }
}