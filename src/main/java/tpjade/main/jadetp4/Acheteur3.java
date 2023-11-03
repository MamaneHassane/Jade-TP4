package tpjade.main.jadetp4;

import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.gui.GuiAgent;
import jade.gui.GuiEvent;
import jade.lang.acl.ACLMessage;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.DFService;
import jade.domain.FIPAException;
public class Acheteur3 extends GuiAgent {
    protected Acheteur3Container acheteur3Container;
    @Override
    protected void setup() {
        acheteur3Container = (Acheteur3Container)getArguments()[0];
        acheteur3Container.acheteur3 = this;
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("Artisanal-Product");
        sd.setName("e-commerce");
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        }
        catch (FIPAException fe) {
            fe.printStackTrace();
        }
        addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
                ACLMessage reply3 = new ACLMessage(ACLMessage.REQUEST);
                reply3 = receive();
                if(reply3!=null){
                    acheteur3Container.afficherMessages(reply3);
                }
                else block();
            }
        });
    }

    @Override
    protected void onGuiEvent(GuiEvent guiEvent) {
        if (guiEvent.getType() == 1) {
            String monPrix = (String) guiEvent.getParameter(0);
            System.out.println("Agent => " + getAID().getName() + "| mon prix => " + monPrix);
            ACLMessage message = new ACLMessage(ACLMessage.INFORM);
            message.addReceiver(new AID("Commissaire_priseur", AID.ISLOCALNAME));
            message.setContent(monPrix);
            send(message);
        }
    }
}
