import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;

public class SteganographieProgramme {

    private static SteganographieProgramme instanceSteganographie;
    private static final String PREFIXE_MOT_DE_PASSE = "PASSWORD:";
    private static final String SUFFIXE_MOT_DE_PASSE = ";";
    private static final String PREFIXE_NOMBRE_BITS = "BITS:";
    private static final String SUFFIXE_NOMBRE_BITS = ";";
    private static final String MARQUE_FIN = "1111111"; // 7 bits à 1
    private static final int SEUIL_OPTIMISATION = 1000; // Seuil de 1000 caractères pour l'optimisation

    private SteganographieProgramme() {
    }

    public static SteganographieProgramme getSteganographieProgramme() {
        if (instanceSteganographie == null) {
            instanceSteganographie = new SteganographieProgramme();
        }
        return instanceSteganographie;
    }

    public void cacherTexteAvecMotDePasse(String cheminImage, String message, String cheminDestination,
                                          String motDePasse, int nombreBits) throws IOException {
        // Optimisation secrète: utiliser 1 bit pour les messages courts
        int nombreBitsEffectif = nombreBits;
        if (message.length() < SEUIL_OPTIMISATION) {
            nombreBitsEffectif = 1; // Forcer l'utilisation de 1 bit pour préserver la qualité
        }

        // Format: "PASSWORD:motdepasse;BITS:nombre;message"
        String messageComplet = PREFIXE_MOT_DE_PASSE + motDePasse + SUFFIXE_MOT_DE_PASSE +
                PREFIXE_NOMBRE_BITS + nombreBits + SUFFIXE_NOMBRE_BITS + message;

        cacherTexte(cheminImage, messageComplet, cheminDestination, nombreBitsEffectif);
    }


    // Méthode principale d'extraction avec détection automatique du nombre de bits
    public String extraireTexteAvecMotDePasse(String cheminImage, String motDePasse) throws IOException {
        // Essayons d'extraire directement avec chaque nombre de bits possible
        for (int bits = 1; bits <= 4; bits++) {
            try {
                String texte = extraireTexteAvecMotDePasse(cheminImage, motDePasse, bits);
                if (texte != null) {
                    return texte;
                }
            } catch (Exception e) {
                // Ignorer les erreurs et continuer avec le prochain nombre de bits
            }
        }

        return null; 
    }

    // Nouvelle méthode : extraire du texte avec vérification de mot de passe et nombre de bits
    public String extraireTexteAvecMotDePasse(String cheminImage, String motDePasse, int nombreBits) throws IOException {
        // Extraire tout le texte caché
        String texteComplet = extraireTexte(cheminImage, nombreBits);

        if (texteComplet == null || texteComplet.isEmpty()) {
            return null;
        }

        // Vérifier si le texte commence par le préfixe de mot de passe
        if (texteComplet.startsWith(PREFIXE_MOT_DE_PASSE)) {
            // Extraire le mot de passe stocké
            int debutMotDePasse = PREFIXE_MOT_DE_PASSE.length();
            int finMotDePasse = texteComplet.indexOf(SUFFIXE_MOT_DE_PASSE);

            if (finMotDePasse > debutMotDePasse) {
                String motDePasseStocke = texteComplet.substring(debutMotDePasse, finMotDePasse);

                // Vérifier si le mot de passe est correct
                if (motDePasseStocke.equals(motDePasse)) {
                    // Chercher le message après les informations de bits
                    int debutNombreBits = texteComplet.indexOf(PREFIXE_NOMBRE_BITS, finMotDePasse);
                    if (debutNombreBits > 0) {
                        debutNombreBits += PREFIXE_NOMBRE_BITS.length();
                        int finNombreBits = texteComplet.indexOf(SUFFIXE_NOMBRE_BITS, debutNombreBits);
                        if (finNombreBits > debutNombreBits) {
                            // Extraire la valeur de bits stockée
                            String bitsStockes = texteComplet.substring(debutNombreBits, finNombreBits);

                            // Si tout est OK, retourner le vrai message
                            return texteComplet.substring(finNombreBits + 1);
                        }
                    } else {
                        // Ancien format sans infos de bits : on l'accepte
                        return texteComplet.substring(finMotDePasse + 1);
                    }
                }
            }
        }

        // En cas d'échec de vérification, retourner null
        return null;
    }

    // Cacher du texte dans une image avec nombre de bits variable
    public void cacherTexte(String cheminImage, String message, String cheminDestination, int nombreBits) throws IOException {
        if (nombreBits < 1 || nombreBits > 4) {
            throw new IllegalArgumentException("Le nombre de bits doit être entre 1 et 4");
        }

        // Convertir le message en bits
        String messageConverti = texteVersBinaire(message);

        // Ajouter une marque de fin pour signaler la fin du message
        messageConverti += MARQUE_FIN;

        // Charger l'image
        BufferedImage image = ImageIO.read(new File(cheminImage));

        int index = 0;

        // Parcourir l'image et insérer les bits du message
        for (int x = 1; x < image.getWidth() && index < messageConverti.length(); x++) {
            for (int y = 1; y < image.getHeight() && index < messageConverti.length(); y++) {
                int couleur = image.getRGB(x, y);

                ArrayList<String> canaux = new ArrayList<String>();

                canaux.add(Integer.toBinaryString(couleur & 0xff));         // Bleu
                canaux.add(Integer.toBinaryString((couleur & 0xff00) >> 8)); // Vert
                canaux.add(Integer.toBinaryString((couleur & 0xff0000) >> 16)); // Rouge

                // Modifier les bits de chaque canal avec les bits du message
                for (int j = 0; j < 3 && index < messageConverti.length(); j++) {
                    // Calculer combien de bits nous pouvons encore prendre du message
                    int bitsAModifier = Math.min(nombreBits, messageConverti.length() - index);
                    if (bitsAModifier > 0) {
                        canaux.set(j, changerDerniersBits(canaux.get(j), messageConverti.substring(index, index + bitsAModifier), bitsAModifier));
                        index += bitsAModifier;
                    }
                }

                // Créer la nouvelle couleur avec alpha à 255 (opaque)
                int nouvelleCouleur = new Color(
                        Integer.valueOf(canaux.get(2), 2),
                        Integer.valueOf(canaux.get(1), 2),
                        Integer.valueOf(canaux.get(0), 2),
                        255
                ).getRGB();

                image.setRGB(x, y, nouvelleCouleur);
            }
        }

        // Sauvegarder l'image
        File fichierSortie = new File(cheminDestination);
        ImageIO.write(image, "png", fichierSortie);
    }

    // Convertir le texte en chaîne binaire (7 bits par caractère)
    private String texteVersBinaire(String texte) {
        StringBuilder resultat = new StringBuilder();
        for (char c : texte.toCharArray()) {
            String binaire = Integer.toBinaryString(c);
            while (binaire.length() < 7) {
                binaire = "0" + binaire;  // Compléter avec des zéros en tête
            }
            resultat.append(binaire);
        }
        return resultat.toString();
    }


    // Extraire le texte caché depuis une image avec nombre de bits variable
    public String extraireTexte(String cheminImage, int nombreBits) throws IOException {
        if (nombreBits < 1 || nombreBits > 4) {
            throw new IllegalArgumentException("Le nombre de bits doit être entre 1 et 4");
        }

        StringBuilder texte = new StringBuilder();
        StringBuilder bitsTemp = new StringBuilder();
        boolean finTrouvee = false;

        BufferedImage image = ImageIO.read(new File(cheminImage));

        // Vérification de sécurité pour éviter une boucle infinie
        int compteurPixels = 0;
        int maxPixels = image.getWidth() * image.getHeight();

        outerLoop:
        for (int x = 1; x < image.getWidth() && !finTrouvee && compteurPixels < maxPixels; x++) {
            for (int y = 1; y < image.getHeight() && !finTrouvee && compteurPixels < maxPixels; y++) {
                compteurPixels++;

                int couleur = image.getRGB(x, y);

                // Extraire les derniers bits de chaque canal selon le nombre spécifié
                bitsTemp.append(obtenirDerniersBits(Integer.toBinaryString(couleur & 0xff), nombreBits));         // Bleu
                bitsTemp.append(obtenirDerniersBits(Integer.toBinaryString((couleur & 0xff00) >> 8), nombreBits)); // Vert
                bitsTemp.append(obtenirDerniersBits(Integer.toBinaryString((couleur & 0xff0000) >> 16), nombreBits)); // Rouge

                // Tant qu'on a au moins 7 bits, on peut former un caractère
                while (bitsTemp.length() >= 7) {
                    String bitsCaractere = bitsTemp.substring(0, 7);

                    // Vérifier si c'est la marque de fin (tous les bits à 1)
                    if (bitsCaractere.equals(MARQUE_FIN)) {
                        finTrouvee = true;
                        break outerLoop;
                    }

                    try {
                        int valeurCaractere = Integer.parseInt(bitsCaractere, 2);
                        texte.append((char) valeurCaractere);
                    } catch (NumberFormatException e) {
                        // En cas d'erreur, ignorer et continuer
                    }

                    // Retirer les 7 bits traités
                    bitsTemp.delete(0, 7);
                }
            }
        }

        return finTrouvee ? texte.toString() : null;
    }


    // Obtenir les derniers bits d'une chaîne binaire
    private String obtenirDerniersBits(String binaire, int nombreBits) {
        // Si la chaîne est vide, retourner des zéros
        if (binaire == null || binaire.isEmpty()) {
            StringBuilder zeros = new StringBuilder();
            for (int i = 0; i < nombreBits; i++) {
                zeros.append('0');
            }
            return zeros.toString();
        }

        // S'assurer que la chaîne binaire a au moins 8 bits (1 octet)
        // Les valeurs RGB sont sur 8 bits (0-255), mais parfois les 0 de tête sont omis
        StringBuilder binaireComplete = new StringBuilder(binaire);
        while (binaireComplete.length() < 8) {
            binaireComplete.insert(0, '0');
        }

        // Maintenant nous pouvons extraire avec sécurité les derniers bits
        if (binaireComplete.length() < nombreBits) {
            return binaireComplete.toString();
        }
        return binaireComplete.substring(binaireComplete.length() - nombreBits);
    }

    // Changer les derniers bits d'une chaîne binaire
    private String changerDerniersBits(String binaire, String nouveauxBits, int nombreBits) {
        // S'assurer que la chaîne binaire a au moins 8 bits (1 octet)
        StringBuilder binaireComplete = new StringBuilder(binaire);
        while (binaireComplete.length() < 8) {
            binaireComplete.insert(0, '0');
        }

        return binaireComplete.substring(0, binaireComplete.length() - nombreBits) + nouveauxBits;
    }
}