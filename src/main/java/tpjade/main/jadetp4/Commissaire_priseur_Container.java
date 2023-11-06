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
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

// Cette classe est le conteur du commissaire priseur
public class Commissaire_priseur_Container extends Application {
    // Le commissaire priseur
    protected Commissaire_priseur commissaire_priseur;
    // La liste des messages
    protected ObservableList<String> observableListData;

    @Override
    public void start(Stage stage) throws Exception {
        // Le code de l'interface graphique
        startContainer();
        stage.setTitle("Commissaire Priseur");
        BorderPane borderPane = new BorderPane();
        HBox hBox1 = new HBox();
        hBox1.setPadding(new Insets(10));
        hBox1.setSpacing(10);
        Label label = new Label("Nom du produit");
        Label productName = new Label("tableau");
        hBox1.getChildren().addAll(label, productName);
        borderPane.setTop(hBox1);
        observableListData = FXCollections.observableArrayList();
        ListView<String> listView = new ListView<String>(observableListData);
        VBox vbox2 = new VBox();
        vbox2.setPadding(new Insets(10));
        vbox2.setSpacing(10);
        vbox2.getChildren().addAll(listView);
        borderPane.setCenter(vbox2);
        Scene scene = new Scene(borderPane, 400, 300);
        stage.setScene(scene);
        stage.show();
    }
    public void afficherMessages(ACLMessage aclMessage){
        // La fonction d'affichage des messages
        Platform.runLater(()->{
            if(aclMessage!=null)
            observableListData.add(aclMessage.getContent()+" reçu de la part de "+ aclMessage.getSender().getName());
        });
    }
    private void startContainer() {
        // Demarrer le conteneur
        Runtime runtime = Runtime.instance();
        Profile profileImpl = new ProfileImpl();
        profileImpl.setParameter(ProfileImpl.MAIN_HOST, "localhost");
        AgentContainer container = runtime.createAgentContainer(profileImpl);
        try {
            AgentController agentController = container.createNewAgent("Commissaire_priseur", "tpjade.main.jadetp4.Commissaire_priseur", new Object[]{this});
            agentController.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
