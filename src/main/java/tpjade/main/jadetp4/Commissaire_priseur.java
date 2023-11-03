package tpjade.main.jadetp4;

import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.gui.GuiAgent;
import jade.gui.GuiEvent;
import jade.lang.acl.ACLMessage;
import jade.proto.ContractNetInitiator;
import jade.util.leap.Iterator;

import java.util.Vector;

public class Commissaire_priseur extends GuiAgent {
    protected Commissaire_priseur_Container commissairePriseurContainer;
    private String productName;
    private double bestPrice;
    private AID bestSeller;

    @Override
    protected void setup() {
        commissairePriseurContainer = (Commissaire_priseur_Container) getArguments()[0];
        commissairePriseurContainer.commissaire_priseur = this;

        productName = "Nom du produit à acheter"; // Récupérez le nom du produit passé en argument
        bestPrice = Double.MAX_VALUE; // Initialisation du meilleur prix

        // Comportement pour rechercher les agents vendeurs
        addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
                DFAgentDescription template = new DFAgentDescription();
                ServiceDescription sd = new ServiceDescription();
                sd.setType("Achat-de-produits"); // Remplacez par le type de service des agents vendeurs
                template.addServices(sd);

                try {
                    DFAgentDescription[] agents = DFService.search(myAgent, template);

                    // Envoi de messages de type CFP à tous les agents vendeurs
                    ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
                    cfp.setOntology(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
                    cfp.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
                    cfp.setContent(productName);
                    for (DFAgentDescription agent : agents) {
                        cfp.addReceiver(agent.getName());
                    }
                    addBehaviour(new ContractNetInitiator(myAgent, cfp) {
                        protected void handlePropose(ACLMessage propose, Vector v) {
                            double price = Double.parseDouble(propose.getContent());
                            if (price < bestPrice) {
                                bestPrice = price;
                                bestSeller = propose.getSender();
                            }
                        }
                        protected void handleAllResponses(Vector responses, Vector acceptances) {
                            // Gestion des réponses reçues de tous les agents vendeurs
                            for (Iterator it = (Iterator) responses.iterator(); it.hasNext(); ) {
                                ACLMessage response = (ACLMessage) it.next();
                                if (response.getPerformative() == ACLMessage.PROPOSE) {
                                    // Acceptez la proposition du meilleur agent vendeur
                                    ACLMessage accept = response.createReply();
                                    accept.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                                    accept.setContent(String.valueOf(bestPrice));
                                    acceptances.addElement(accept);
                                }
                            }
                            informAllAgents("L'enchère est remportée par " + bestSeller.getName() + " au prix de " + bestPrice, agents);
                        }
                    });
                } catch (FIPAException fe) {
                    fe.printStackTrace();
                }
            }
        });
    }

    @Override
    protected void onGuiEvent(GuiEvent guiEvent) {
    }

    public void informAllAgents(String result, DFAgentDescription[] agents) {
        ACLMessage inform = new ACLMessage(ACLMessage.INFORM);
        inform.setContent(result);
        for (DFAgentDescription agent : agents) {
            inform.addReceiver(agent.getName());
        }
        send(inform);
    }
}
