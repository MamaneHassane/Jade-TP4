package tpjade.main.jadetp4;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.gui.GuiEvent;
import jade.lang.acl.ACLMessage;
import tpjade.main.jadetp4.Acheteur1Container;

public class Acheteur2 extends Agent {
    protected Acheteur2Container acheteur2Container;
    @Override
    protected void setup() {
        acheteur2Container = (Acheteur2Container)getArguments()[0];
        acheteur2Container.acheteur1 = this;
        addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
                ACLMessage reply1 = new ACLMessage(ACLMessage.REQUEST);
                reply1 = receive();
                if(reply1!=null){
                    acheteur2Container.afficherMessages(reply1);
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
