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
//Cette classe est un acheteur
//Les autres acheteurs ont le même code, c'est le nom de la classe, du conteneur
//des messages et le montant qu'il ajoute à chaque tour qui changent
public class Acheteur1 extends GuiAgent {
    //Le conteneur application
    protected Acheteur1Container acheteur1Container;
    @Override
    //La méthode setup, elle contient les comportements
    protected void setup() {
        acheteur1Container = (Acheteur1Container) getArguments()[0];
        acheteur1Container.acheteur1 = this;
        //S'enregistrer dans le directory facilitator
        addBehaviour(new OneShotBehaviour() {
            @Override
            public void action() {
                DFAgentDescription dfd = new DFAgentDescription();
                dfd.setName(getAID());
                ServiceDescription sd = new ServiceDescription();
                //Le type et le nom de la description du service
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
        //Recevoir les messages et repondre

        addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
                //Reception du message
                ACLMessage reply1 = receive();
                if (reply1 != null) {
                    acheteur1Container.afficherMessages(reply1);
                    switch(reply1.getPerformative()) {
                        //S'il recoit un cfp
                        case ACLMessage.CFP -> {
                            ACLMessage surenchere = new ACLMessage(ACLMessage.PROPOSE);
                            surenchere.addReceiver(reply1.getSender());
                            //Ajouter une certaine somme sur la plus grosse enchère
                            surenchere.setContent(String.valueOf((Double.parseDouble((reply1.getContent())))+500));
                            send(surenchere);
                        }
                        //S'il gagne
                        case ACLMessage.ACCEPT_PROPOSAL -> {
                            ACLMessage fin = new ACLMessage(ACLMessage.AGREE); //Accepter
                            //Remercier le commissaire priseur
                            fin.setContent("Merci pour cette offre");
                            fin.addReceiver(reply1.getSender());
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
            //Envoyer son prix initial si on clique sur le bouton
            String monPrix = (String) guiEvent.getParameter(0);
            System.out.println("Agent => " + getAID().getName() + "| mon prix => " + monPrix);
            ACLMessage message = new ACLMessage(ACLMessage.PROPOSE);
            //Vers le commissaire priseur
            message.addReceiver(new AID("Commissaire_priseur", AID.ISLOCALNAME));
            message.setContent(monPrix);
            send(message);
        }
    }
    //Se désenregistrer du DF
    @Override
    protected void takeDown() {
        try {
            DFService.deregister(this);
        } catch (FIPAException e) {
            e.printStackTrace();
        }
    }
}
