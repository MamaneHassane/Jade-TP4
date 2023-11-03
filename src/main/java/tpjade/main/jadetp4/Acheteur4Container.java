package tpjade.main.jadetp4;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.gui.GuiEvent;
import jade.lang.acl.ACLMessage;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import javafx.application.Application;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;


public class Acheteur4Container extends Application{
    protected Acheteur4 acheteur4;
    protected ObservableList<String> observableListData;
    @Override
    public void start(Stage stage) throws Exception {
        startContainer();
        stage.setTitle("Acheteur 4");
        BorderPane borderPane = new BorderPane();
        HBox hBox1 = new HBox();
        hBox1.setPadding(new Insets(10));
        hBox1.setSpacing(10);
        Label label = new Label("Mon offre");
        TextField monOffre = new TextField();
        Button buttonOk = new Button("OK");
        hBox1.getChildren().addAll(label,monOffre,buttonOk);
        borderPane.setTop(hBox1);
        observableListData= FXCollections.observableArrayList();
        ListView<String> listView = new ListView<String>(observableListData);
        VBox vbox2 = new VBox();
        vbox2.setPadding(new Insets(10));
        vbox2.setSpacing(10);
        vbox2.getChildren().addAll(listView);
        borderPane.setCenter(vbox2);
        Scene scene = new Scene(borderPane,400,300);
        stage.setScene(scene);
        buttonOk.setOnAction(evt->{
            String prdName = monOffre.getText();
            GuiEvent guiEvent = new GuiEvent(this,1);
            guiEvent.addParameter(prdName);
            acheteur4.onGuiEvent(guiEvent);
        });
        stage.show();
    }
    public void afficherMessages(ACLMessage aclMessage){
        Platform.runLater(()->{
            observableListData.add(aclMessage.getContent()+" reçu de la part de "+ aclMessage.getSender().getName());
        });
    }
    private void startContainer() {
        Runtime runtime = Runtime.instance();
        Profile profileImpl = new ProfileImpl();
        profileImpl.setParameter(ProfileImpl.MAIN_HOST, "localhost");
        AgentContainer container = runtime.createAgentContainer(profileImpl);
        try {
            AgentController agentController = container.createNewAgent("Acheteur 2", "tpjade.main.jadetp4.Acheteur2", new
                    Object[]{this});
            agentController.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void main(String[] args) {
        launch(args);
    }
}
