package tpjade.main.jadetp4;

import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.gui.GuiAgent;
import jade.gui.GuiEvent;
import jade.lang.acl.ACLMessage;

public class Vendeur extends GuiAgent {
    protected VendeurContainer vendeurContainer;
    @Override
    protected void setup() {
        vendeurContainer = (VendeurContainer) getArguments()[0];
        vendeurContainer.vendeur = this;
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
        if (guiEvent.getType() == 1) {
            String monPrix = (String) guiEvent.getParameter(0);
            System.out.println("Agent => " + getAID().getName() + "| mon prix => " + monPrix);
            ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
            message.addReceiver(new AID("Commissaire_priseur", AID.ISLOCALNAME));
            message.setContent(monPrix);
            send(message);
        }
    }
}
