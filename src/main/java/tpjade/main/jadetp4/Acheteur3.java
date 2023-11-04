package tpjade.main.jadetp4;

import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
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
        addBehaviour(new OneShotBehaviour() {
            @Override
            public void action() {
                DFAgentDescription dfd = new DFAgentDescription();
                dfd.setName(getAID());
                ServiceDescription sd = new ServiceDescription();
                sd.setType("tableau");
                sd.setName("vente-aux-enchères");
                dfd.addServices(sd);
                try {
                    DFService.register(myAgent, dfd);
                }
                catch (FIPAException fe) {
                    fe.printStackTrace();
                }
            }
        });
        addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
                ACLMessage reply3 = receive();
                if (reply3 != null) {
                    acheteur3Container.afficherMessages(reply3);
                    switch(reply3.getPerformative()) {
                        case ACLMessage.CFP -> {
                            ACLMessage surenchere = new ACLMessage(ACLMessage.PROPOSE);
                            surenchere.addReceiver(reply3.getSender());
                            surenchere.setContent(String.valueOf((Double.parseDouble((reply3.getContent())))+800));
                            send(surenchere);
                        }
                        case ACLMessage.ACCEPT_PROPOSAL -> {
                            ACLMessage fin = new ACLMessage(ACLMessage.AGREE);
                            fin.setContent("Merci pour cette offre");
                            fin.addReceiver(reply3.getSender());
                            send(fin);
                        }
                    }
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
            ACLMessage message = new ACLMessage(ACLMessage.PROPOSE);
            message.addReceiver(new AID("Commissaire_priseur", AID.ISLOCALNAME));
            message.setContent(monPrix);
            send(message);
        }
    }
    @Override
    protected void takeDown() {
        try {
            DFService.deregister(this);
        } catch (FIPAException e) {
            e.printStackTrace();
        }
    }
}
