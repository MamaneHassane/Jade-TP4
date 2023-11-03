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
public class Acheteur1 extends GuiAgent {
    protected Acheteur1Container acheteur1Container;
    @Override
    protected void setup() {
        acheteur1Container = (Acheteur1Container) getArguments()[0];
        acheteur1Container.acheteur1 = this;

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
                ACLMessage reply1 = receive();
                if (reply1 != null) {
                    acheteur1Container.afficherMessages(reply1);
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
            ACLMessage message = new ACLMessage(ACLMessage.INFORM);
            message.addReceiver(new AID("Commissaire_priseur", AID.ISLOCALNAME));
            message.setContent(monPrix);
            send(message);
        }
    }
}
