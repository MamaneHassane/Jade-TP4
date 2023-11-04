package tpjade.main.jadetp4;

public class Article {
    String nom;
    StatusArticle statusArticle;
    Double premierPrix;
    public Article(String nom, StatusArticle statusArticle, Double premierPrix) {
        this.nom = nom;
        this.statusArticle = statusArticle;
        this.premierPrix = premierPrix;
    }
    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public StatusArticle getStatusArticle() {
        return statusArticle;
    }

    public void setStatusArticle(StatusArticle statusArticle) {
        this.statusArticle = statusArticle;
    }

    public Double getPremierPrix() {
        return premierPrix;
    }

    public void setPremierPrix(Double premierPrix) {
        this.premierPrix = premierPrix;
    }
}
