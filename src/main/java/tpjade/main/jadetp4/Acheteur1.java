package tpjade.main.jadetp4;

import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.gui.GuiAgent;
import jade.gui.GuiEvent;
import jade.lang.acl.ACLMessage;

public class Acheteur1 extends GuiAgent {
    protected Acheteur1Container acheteur1Container;
    @Override
    protected void setup() {
        acheteur1Container = (Acheteur1Container)getArguments()[0];
        acheteur1Container.acheteur1 = this;
        addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
                ACLMessage reply1 = new ACLMessage(ACLMessage.REQUEST);
                reply1 = receive();
                if(reply1!=null){
                    acheteur1Container.afficherMessages(reply1);
                }
                else block();
            }
        });
    }

    @Override
    protected void onGuiEvent(GuiEvent guiEvent) {
        if (guiEvent.getType()==1){
            String productName= (String) guiEvent.getParameter(0);
            System.out.println("Agent => "+getAID().getName()+"| mon message => "+productName);
            ACLMessage message = new ACLMessage(ACLMessage.CONFIRM);
            message.addReceiver(new AID("Vendeur",AID.ISLOCALNAME));
            message.setContent(productName);
            send(message);
        }
    }
}