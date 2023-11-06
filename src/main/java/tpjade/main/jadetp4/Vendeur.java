package tpjade.main.jadetp4;

import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.gui.GuiAgent;
import jade.gui.GuiEvent;
import jade.lang.acl.ACLMessage;
// Cette classe est la classe du vendeur
public class Vendeur extends GuiAgent {
    // Le conteneur du vendeur
    protected VendeurContainer vendeurContainer;
    @Override
    protected void setup() {
        vendeurContainer = (VendeurContainer) getArguments()[0];
        vendeurContainer.vendeur = this;
        // L'affichage des messages
        addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
                ACLMessage replyVendeur;
                replyVendeur= receive();
                if (replyVendeur != null) {
                    vendeurContainer.afficherMessages(replyVendeur);
                } else {
                    block();
                }
            }
        });
    }

    @Override
    protected void onGuiEvent(GuiEvent guiEvent) {
        // L'envoi du nom du produit à vendre
        if (guiEvent.getType() == 1) {
            String monProduit = (String) guiEvent.getParameter(0);
            System.out.println("Agent => " + getAID().getName() + "| mon produit => " + monProduit);
            ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
            message.addReceiver(new AID("Commissaire_priseur", AID.ISLOCALNAME));
            message.setContent(monProduit);
            send(message);
        }
    }
}
