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
// Cette classe est le commissare priseur
// C'est lui qui gère la vente aux enchères
public class Commissaire_priseur extends GuiAgent {
    //Les getters et les setters pour connaître à
    //chaque instant la meilleur offre et le meilleur offreur
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
    // Le conteneur du commissaire priseur
    protected Commissaire_priseur_Container commissairePriseurContainer;
    // Une variable qui stockera le nom de l'article à vendre ,
    // il sera envoyé par le vendeur
    String article = "";
    // Le montant de la meilleure offre
    private double meilleureOffre;
    // L'AID du meilleur acheteur
    private AID meilleurOffreur;
    // La liste des acheteurs dans le Directory Facilitator
    public AID[] acheteurs=new AID[0];
    // Les AID des acheteurs et le prix qu'ils proposent
    Map <AID,Double> les_prix = new HashMap<AID, Double>();
    // Une fonction qui parcour la map pour trouver la meilleure entrée,
    // c'est à dire le couple <AID,Double> correspondant au meilleur prix,
    // et à l'AID de l'agent acheteur qui le propose
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
    // Une fonction pour informer le vendeur de la meilleure offre
    // et du nom de l'agent acheteur qui la propose
    // la fonction retourne le message à envoyé, il sera envoyé plus bas
    public ACLMessage informerVendeur(){
        ACLMessage infosVente = new ACLMessage(ACLMessage.INFORM);
        infosVente.setContent("La meilleure offre est "+
                Double.toString(getMeilleureOffre())+
                " de la part de "+
                getMeilleurOffreur());
        infosVente.addReceiver(new AID("Vendeur",AID.ISLOCALNAME));
        return infosVente;
    }

    // La fonction setup du commissaire priseur
    @Override
    protected void setup() {
        // Son conteneur
        commissairePriseurContainer = (Commissaire_priseur_Container) getArguments()[0];
        commissairePriseurContainer.commissaire_priseur = this;
        setMeilleureOffre(0); // La meilleure offre est mise à 0 au début
        // Un ticker behaviour pour retrouver les acheteurs depuis le DF
        // chaque 2 secondes, au cas où un acheteur rejoint la vente
        addBehaviour(new TickerBehaviour(this, 2000) {
            protected void onTick() {
                DFAgentDescription template = new DFAgentDescription();
                ServiceDescription sd = new ServiceDescription();
                // Le type est le nom de l'article envoyé par le vendeur
                sd.setType(article);
                sd.setName("vente-aux-enchères");
                System.out.println(acheteurs.length);
                template.addServices(sd);
                try {
                    // Lancer la recherche
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
        // Template de reception de messages du commissaire priseur
        MessageTemplate templateReponse= MessageTemplate.or(
                MessageTemplate.MatchPerformative(ACLMessage.PROPOSE),
                MessageTemplate.or(MessageTemplate.MatchPerformative(ACLMessage.AGREE),
                                    MessageTemplate.MatchPerformative(ACLMessage.REQUEST))
        );
        addBehaviour(new CyclicBehaviour() {
            ACLMessage message; // Le message qu'on enverra
            ACLMessage reponse; // La reponse qu'on reçoit
            Entry<AID,Double> meilleureEntree; // La meilleure entrée, expliquée plus haut
            @Override
            public void action() {
                message = new ACLMessage(ACLMessage.CFP); // Le message CFP
                reponse = receive(templateReponse); // La reponse reçu selon le template
                commissairePriseurContainer.afficherMessages(reponse);
                if(reponse!=null){ // Si la réponse existe
                    switch (reponse.getPerformative()){
                        case ACLMessage.REQUEST -> {
                            // Si c'est une requête donc elle vient du venduer
                            article = message.getContent(); // Stocker le nom de l'article dans article
                        }
                        case ACLMessage.PROPOSE -> {
                            // Si c'est une proposition
                            // Stocker l'entrée dans la map des prix
                            les_prix.put(reponse.getSender(),Double.parseDouble(reponse.getContent()));
                            // Trouver le meilleur prix jusqu'à maintenant
                            meilleureEntree = trouverMeilleurOffreur(les_prix);
                            setMeilleureOffre(meilleureEntree.getValue()); // Mettre à jour le meilleur prix
                            setMeilleurOffreur(meilleureEntree.getKey()); // Et le nom du meilleur acheteur
                            System.out.println("Meilleure offre "+getMeilleureOffre());
                            System.out.println("Meilleur offreur "+getMeilleurOffreur());
                            if(getMeilleureOffre()>=5000){
                                // On fait autant de tours jusqu'à atteindre notre objectif
                                // On suppose que l'agent qui offre cette somme ou plus à gagné
                                // et les autres agents ne peuvent pas surencherir plus que lui
                                // Accepter l'offre
                                ACLMessage accept = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
                                accept.addReceiver(getMeilleurOffreur());
                                accept.setContent("Adjugé vendu"); // Le contenu
                                send(accept); // Lui dire qu'il a gagné
                            }
                            else {
                                // Si la somme ne nous convient pas la vente aux enchère continue
                                System.out.println(acheteurs.length);
                                message = new ACLMessage(ACLMessage.CFP);
                                // On prend la meilleur offre
                                message.setContent(Double.toString(getMeilleureOffre()));
                                for (int i =0; i<acheteurs.length;i++) {
                                    // On informe les acheteurs un à un de la meilleure offre
                                    // afin qu'ils fassent d'autres offres
                                    message.addReceiver(acheteurs[i]);
                                    send(message);
                                    message.clearAllReceiver();
                                }

                            }
                        }
                        case ACLMessage.AGREE -> {
                            // Si c'est une acceptation du gagnant
                            ACLMessage messageFinal = informerVendeur(); // Informer le vendeur
                            // La fonction est expliquée plus haut
                            send(messageFinal); // Envoyer le message au vendeur
                            System.out.println("La vente aux enchères est terminée");
                            myAgent.doDelete(); 
                        }
                    }
                } else block();
            }
        });
    }
    // Fonction pour réagir aux événements sur l'interface graphique
    @Override
    protected void onGuiEvent(GuiEvent guiEvent) {
    }
    // Désenregistrer l'agent du DF
    @Override
    protected void takeDown() {
        try {
            DFService.deregister(this);
        } catch (FIPAException e) {
            e.printStackTrace();
        }
    }
}
