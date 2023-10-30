package tpjade.main.jadetp4;

import jade.core.Agent;
public class Commissaire_priseur extends Agent{
    protected Commissaire_priseur_Container commissairePriseurContainer;
    @Override
    protected void setup() {
        commissairePriseurContainer = (Commissaire_priseur_Container) getArguments()[0];
        commissairePriseurContainer.commissaire_priseur = this;

    }
}
