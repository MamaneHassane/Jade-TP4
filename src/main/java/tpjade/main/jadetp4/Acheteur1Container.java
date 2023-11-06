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

import java.util.List;
//Cette classe est le conteneur de l'acheteur 1
//Les autres conteneur des acheteurs ont le même code
public class Acheteur1Container extends Application{
    //Contient son acheteur
    protected Acheteur1 acheteur1;
    //La liste des messages reçus
    protected ObservableList<String> observableListData;
    @Override
    public void start(Stage stage) throws Exception {
        //Le code de l'interface graphique
        startContainer();
        stage.setTitle("Acheteur 1");
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
            acheteur1.onGuiEvent(guiEvent);
        });
        stage.show();
    }
    //La fonction pour afficher les messages
    public void afficherMessages(ACLMessage aclMessage){
        Platform.runLater(()->{
            if(aclMessage!=null)
                observableListData.add(aclMessage.getContent()+" reçu de la part de "+ aclMessage.getSender().getName());
        });
    }
    //Demarrer l'agent et son conteneur
    private void startContainer() {
        Runtime runtime = Runtime.instance();
        Profile profileImpl = new ProfileImpl();
        profileImpl.setParameter(ProfileImpl.MAIN_HOST, "localhost");
        AgentContainer container = runtime.createAgentContainer(profileImpl);
        try {
            AgentController agentController = container.createNewAgent("Acheteur 1", "tpjade.main.jadetp4.Acheteur1", new
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
