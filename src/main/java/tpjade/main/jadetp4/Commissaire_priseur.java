package tpjade.main.jadetp4;

import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.gui.GuiAgent;
import jade.gui.GuiEvent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class Commissaire_priseur extends GuiAgent {
    public double getMeilleureOffre() {
        return meilleureOffre;
    }

    public void setMeilleureOffre(double meilleureOffre) {
        this.meilleureOffre = meilleureOffre;
    }

    public AID getMeilleurOffreur() {
        return meilleurOffreur;
    }

    public void setMeilleurOffreur(AID meilleurOffreur) {
        this.meilleurOffreur = meilleurOffreur;
    }

    protected Commissaire_priseur_Container commissairePriseurContainer;
    String article = "";
    private double meilleureOffre;
    private AID meilleurOffreur;
    public AID[] acheteurs=new AID[0];
    Map <AID,Double> les_prix = new HashMap<AID, Double>();
    public static Entry<AID, Double> trouverMeilleurOffreur(Map<AID, Double> map) {
        Entry<AID, Double> maxEntry = null;
        Double maxValue = Double.MIN_VALUE;
        for (Entry<AID, Double> entry : map.entrySet()) {
            if (entry.getValue() > maxValue) {
                maxValue = entry.getValue();
                maxEntry = entry;
            }
        }
        return maxEntry;
    }
    private void prochainTour(double grandeOffre) {
        ACLMessage tour = new ACLMessage(ACLMessage.CFP);
        for (int i =0;i<acheteurs.length;i++) {
            tour.addReceiver(acheteurs[i]);
            tour.setContent(String.valueOf(grandeOffre));
            send(tour);
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            tour.clearAllReceiver();
        }
    }
    public ACLMessage informerVendeur(){
        ACLMessage infosVente = new ACLMessage(ACLMessage.INFORM);
        infosVente.setContent("La meilleure offre est "+
                Double.toString(getMeilleureOffre())+
                " de la part de "+
                getMeilleurOffreur());
        infosVente.addReceiver(new AID("Vendeur",AID.ISLOCALNAME));
        return infosVente;
    }

    @Override
    protected void setup() {
        commissairePriseurContainer = (Commissaire_priseur_Container) getArguments()[0];
        commissairePriseurContainer.commissaire_priseur = this;
        meilleureOffre = 0;
        addBehaviour(new TickerBehaviour(this, 2000) {
            protected void onTick() {
                DFAgentDescription template = new DFAgentDescription();
                ServiceDescription sd = new ServiceDescription();
                sd.setType("tableau");
                sd.setName("vente-aux-enchères");
                System.out.println(acheteurs.length);
                template.addServices(sd);
                try {
                    DFAgentDescription[] result = DFService.search(myAgent, template);
                    acheteurs = new AID[result.length];
                    for (int i = 0; i < result.length; ++i) {
                        acheteurs[i] = result[i].getName();
                    }
                } catch (FIPAException fe) {
                     fe.printStackTrace();
                }
            }
        });
        MessageTemplate templateReponse= MessageTemplate.or(
                MessageTemplate.MatchPerformative(ACLMessage.PROPOSE),
                MessageTemplate.or(MessageTemplate.MatchPerformative(ACLMessage.AGREE),
                                    MessageTemplate.MatchPerformative(ACLMessage.REQUEST))
        );
        addBehaviour(new CyclicBehaviour() {
            ACLMessage message;
            ACLMessage reponse;
            Entry<AID,Double> meilleureEntree;
            @Override
            public void action() {
                message = new ACLMessage(ACLMessage.CFP);
                reponse = receive(templateReponse);
                commissairePriseurContainer.afficherMessages(reponse);
                if(reponse!=null){
                    switch (reponse.getPerformative()){
                        case ACLMessage.REQUEST -> {
                            article = message.getContent();
                        }
                        case ACLMessage.PROPOSE -> {
                            les_prix.put(reponse.getSender(),Double.parseDouble(reponse.getContent()));
                            meilleureEntree = trouverMeilleurOffreur(les_prix);
                            setMeilleureOffre(meilleureEntree.getValue());
                            System.out.println("Meilleure offre "+getMeilleureOffre());
                            System.out.println("Meilleur offreur "+getMeilleurOffreur());
                            setMeilleurOffreur(meilleureEntree.getKey());
                            if(getMeilleureOffre()>=30000){ //On fait autant de tours jusqu'à atteindre notre objectif
                                ACLMessage accept = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
                                accept.addReceiver(getMeilleurOffreur());
                                accept.setContent("Adjugé vendu");
                                send(accept);
                            }
                            else {
                                System.out.println(acheteurs.length);
                                message = new ACLMessage(ACLMessage.CFP);
                                message.setContent(Double.toString(getMeilleureOffre()));
                                for (int i =0; i<acheteurs.length;i++) message.addReceiver(acheteurs[i]);
                                send(message);
                            }
                        }
                        case ACLMessage.AGREE -> {
                            ACLMessage messageFinal = informerVendeur();
                            send(messageFinal);
                            System.out.println("La vente aux enchères est terminée");
                            myAgent.doDelete();
                        }
                    }
                } else block();
            }
        });
    }

    @Override
    protected void onGuiEvent(GuiEvent guiEvent) {
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
