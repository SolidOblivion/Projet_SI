
import java.awt.EventQueue;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.DefaultCaret;
import java.io.File;
import java.io.IOException;

public class SteganographieVuePrincipale extends JFrame {

	private JPanel panneauPrincipal;
	private JTextField txtCheminImagePng;
	private JTextField txtCheminNouvelleImage;
	private JButton btnParcourirImage;
	private JButton btnParcourirDestination;
	private JTextField txtCheminImageRecuperation;
	private JButton btnParcourirImageRecuperation;
	private JPasswordField champMotDePasse;
	private JPasswordField champMotDePasseRecuperation;
	private JComboBox<String> comboNombreBits;
	private JProgressBar barreCapacite;

	public static void main(String[] args) {
		EventQueue.invokeLater(() -> {
			try {
				SteganographieVuePrincipale frame = new SteganographieVuePrincipale();
				frame.setVisible(true);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}

	public SteganographieVuePrincipale() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 650, 480);
		setResizable(false);
		setLocationRelativeTo(null);
		setTitle("Projet Stéganographie");

		panneauPrincipal = new JPanel();
		panneauPrincipal.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(panneauPrincipal);
		panneauPrincipal.setLayout(null);

		JTabbedPane panneauOnglets = new JTabbedPane(JTabbedPane.TOP);
		panneauOnglets.setBounds(12, 12, 610, 420);
		panneauPrincipal.add(panneauOnglets);

		JPanel panneauCacher = new JPanel();
		panneauCacher.setLayout(null);
		panneauOnglets.addTab("Cacher Texte", null, panneauCacher, null);

		JLabel lblCheminImage = new JLabel("Image source :");
		lblCheminImage.setBounds(20, 20, 150, 20);
		panneauCacher.add(lblCheminImage);

		txtCheminImagePng = new JTextField();
		txtCheminImagePng.setBounds(180, 20, 300, 25);
		txtCheminImagePng.setEditable(false);
		panneauCacher.add(txtCheminImagePng);

		btnParcourirImage = new JButton("Parcourir");
		btnParcourirImage.setBounds(490, 20, 100, 25);
		panneauCacher.add(btnParcourirImage);

		btnParcourirImage.addActionListener(e -> {
			JFileChooser fc = new JFileChooser();
			fc.setDialogTitle("Choisissez l'image source");
			if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
				txtCheminImagePng.setText(fc.getSelectedFile().getAbsolutePath());
			}
		});

		JLabel lblCheminNouvelleImage = new JLabel("Image destination :");
		lblCheminNouvelleImage.setBounds(20, 60, 150, 20);
		panneauCacher.add(lblCheminNouvelleImage);

		txtCheminNouvelleImage = new JTextField();
		txtCheminNouvelleImage.setBounds(180, 60, 300, 25);
		txtCheminNouvelleImage.setEditable(false);
		panneauCacher.add(txtCheminNouvelleImage);

		btnParcourirDestination = new JButton("Parcourir");
		btnParcourirDestination.setBounds(490, 60, 100, 25);
		panneauCacher.add(btnParcourirDestination);

		btnParcourirDestination.addActionListener(e -> {
			JFileChooser fc = new JFileChooser();
			fc.setDialogTitle("Choisissez où enregistrer l'image modifiée");
			if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
				txtCheminNouvelleImage.setText(fc.getSelectedFile().getAbsolutePath());
			}
		});

		JLabel lblMotDePasse = new JLabel("Mot de passe :");
		lblMotDePasse.setBounds(20, 100, 150, 20);
		panneauCacher.add(lblMotDePasse);

		champMotDePasse = new JPasswordField();
		champMotDePasse.setBounds(180, 100, 300, 25);
		panneauCacher.add(champMotDePasse);

		JLabel lblNombreBits = new JLabel("Bits à utiliser :");
		lblNombreBits.setBounds(490, 100, 100, 20);
		panneauCacher.add(lblNombreBits);

		comboNombreBits = new JComboBox<>(new String[]{"1", "2", "3"});
		comboNombreBits.setBounds(490, 120, 100, 25);
		panneauCacher.add(comboNombreBits);

		JLabel lblInformationACacher = new JLabel("- Information à cacher -");
		lblInformationACacher.setBounds(220, 140, 200, 20);
		panneauCacher.add(lblInformationACacher);

		JTextArea zoneTexte = new JTextArea();
		zoneTexte.setLineWrap(true);
		zoneTexte.setWrapStyleWord(true);
		JScrollPane defilementTexte = new JScrollPane(zoneTexte);
		defilementTexte.setBounds(20, 170, 570, 130);
		panneauCacher.add(defilementTexte);

		DefaultCaret caret = (DefaultCaret) zoneTexte.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

		barreCapacite = new JProgressBar(0, 100);
		barreCapacite.setBounds(20, 305, 570, 20);
		barreCapacite.setStringPainted(true);
		panneauCacher.add(barreCapacite);

		zoneTexte.getDocument().addDocumentListener(new DocumentListener() {
			public void insertUpdate(DocumentEvent e) {
				mettreAJourBarre();
			}
			public void removeUpdate(DocumentEvent e) {
				mettreAJourBarre();
			}
			public void changedUpdate(DocumentEvent e) {
				mettreAJourBarre();
			}

			public void mettreAJourBarre() {
				String cheminImage = txtCheminImagePng.getText();
				if (cheminImage.isEmpty()) return;

				int nombreBits = Integer.parseInt((String) comboNombreBits.getSelectedItem());
				try {
					BufferedImage image = ImageIO.read(new File(cheminImage));
					int largeur = image.getWidth();
					int hauteur = image.getHeight();
					int bitsDisponibles = largeur * hauteur * 3 * nombreBits;
					int caracteresMax = bitsDisponibles / 8;
					int longueurMessage = zoneTexte.getText().length();
					int pourcentage = (int) (((double) longueurMessage / caracteresMax) * 100);
					barreCapacite.setValue(Math.min(pourcentage, 100));
					barreCapacite.setString("Capacité utilisée : " + longueurMessage + "/" + caracteresMax + " (" + Math.min(pourcentage, 100) + "%)");
				} catch (IOException ex) {
					barreCapacite.setValue(0);
					barreCapacite.setString("Erreur de lecture de l'image");
				} catch (Exception ex) {
					barreCapacite.setValue(0);
					barreCapacite.setString("Erreur inconnue");
				}
			}
		});

		JButton btnCacher = new JButton("Cacher le texte");
		btnCacher.setBounds(20, 340, 570, 30);
		panneauCacher.add(btnCacher);
		btnCacher.addActionListener(e -> {
			if (txtCheminImagePng.getText().isEmpty() ||
					txtCheminNouvelleImage.getText().isEmpty() ||
					champMotDePasse.getPassword().length == 0 ||
					zoneTexte.getText().isEmpty()) {
				JOptionPane.showMessageDialog(this,
						"Veuillez remplir tous les champs",
						"Champs manquants",
						JOptionPane.WARNING_MESSAGE);
				return;
			}
			try {
				String motDePasse = new String(champMotDePasse.getPassword());
				int nombreBits = Integer.parseInt((String) comboNombreBits.getSelectedItem());
				SteganographieProgramme.getSteganographieProgramme()
						.cacherTexteAvecMotDePasse(
								txtCheminImagePng.getText(),
								zoneTexte.getText(),
								txtCheminNouvelleImage.getText(),
								motDePasse,
								nombreBits);
				JOptionPane.showMessageDialog(this,
						"Message caché avec succès !",
						"Succès",
						JOptionPane.INFORMATION_MESSAGE);
			} catch (IOException e1) {
				JOptionPane.showMessageDialog(this,
						"Erreur : " + e1.getMessage(),
						"Erreur",
						JOptionPane.ERROR_MESSAGE);
				e1.printStackTrace();
			}
		});

	// --- Onglet 2 : Récupérer message
		JPanel panneauRecuperer = new JPanel();
		panneauRecuperer.setLayout(null);
		panneauOnglets.addTab("Récupérer Texte", null, panneauRecuperer, null);

		JLabel lblImageRecuperation = new JLabel("Image modifiée :");
		lblImageRecuperation.setBounds(20, 20, 150, 20);
		panneauRecuperer.add(lblImageRecuperation);

		txtCheminImageRecuperation = new JTextField();
		txtCheminImageRecuperation.setBounds(180, 20, 300, 25);
		txtCheminImageRecuperation.setEditable(false);
		panneauRecuperer.add(txtCheminImageRecuperation);

		btnParcourirImageRecuperation = new JButton("Parcourir");
		btnParcourirImageRecuperation.setBounds(490, 20, 100, 25);
		panneauRecuperer.add(btnParcourirImageRecuperation);

		btnParcourirImageRecuperation.addActionListener(e -> {
			JFileChooser fc = new JFileChooser();
			fc.setDialogTitle("Choisissez l'image modifiée");
			if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
				txtCheminImageRecuperation.setText(fc.getSelectedFile().getAbsolutePath());
			}
		});

		// --- Nouveau: Mot de passe pour récupération
		JLabel lblMotDePasseRecuperation = new JLabel("Mot de passe :");
		lblMotDePasseRecuperation.setBounds(20, 60, 150, 20);
		panneauRecuperer.add(lblMotDePasseRecuperation);

		champMotDePasseRecuperation = new JPasswordField();
		champMotDePasseRecuperation.setBounds(180, 60, 300, 25);
		panneauRecuperer.add(champMotDePasseRecuperation);

		// Pas besoin de menu déroulant pour la récupération
		// Le nombre de bits est stocké dans le message et sera détecté automatiquement

		JLabel lblTexteRecupere = new JLabel("- Information cachée -");
		lblTexteRecupere.setBounds(230, 100, 200, 20);
		panneauRecuperer.add(lblTexteRecupere);

		// MODIFICATION: Utiliser un JTextArea avec retour à la ligne automatique pour la récupération
		JTextArea zoneTexteRecupere = new JTextArea();
		zoneTexteRecupere.setLineWrap(true);         // Active le retour à la ligne automatique
		zoneTexteRecupere.setWrapStyleWord(true);    
		zoneTexteRecupere.setEditable(false);

		// Configuration du défilement
		JScrollPane defilementTexteRecupere = new JScrollPane(zoneTexteRecupere);
		defilementTexteRecupere.setBounds(20, 130, 570, 180);
		panneauRecuperer.add(defilementTexteRecupere);

		// Auto-scroll à la fin du texte
		DefaultCaret caretRecupere = (DefaultCaret) zoneTexteRecupere.getCaret();
		caretRecupere.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

		JButton btnRecuperer = new JButton("Récupérer le texte");
		btnRecuperer.setBounds(20, 320, 570, 30);
		panneauRecuperer.add(btnRecuperer);

		btnRecuperer.addActionListener(e -> {
			// Vérification des champs
			if (txtCheminImageRecuperation.getText().isEmpty() ||
					champMotDePasseRecuperation.getPassword().length == 0) {

				JOptionPane.showMessageDialog(this,
						"Veuillez remplir tous les champs",
						"Champs manquants",
						JOptionPane.WARNING_MESSAGE);
				return;
			}

			try {
				String motDePasse = new String(champMotDePasseRecuperation.getPassword());

				String texte = SteganographieProgramme.getSteganographieProgramme()
						.extraireTexteAvecMotDePasse(
								txtCheminImageRecuperation.getText(),
								motDePasse);

				if (texte != null) {
					zoneTexteRecupere.setText(texte);
				} else {
					zoneTexteRecupere.setText("");
					JOptionPane.showMessageDialog(this,
							"Mot de passe incorrect ou message corrompu.",
							"Erreur",
							JOptionPane.ERROR_MESSAGE);
				}
			} catch (IOException e1) {
				JOptionPane.showMessageDialog(this,
						"Erreur : " + e1.getMessage(),
						"Erreur",
						JOptionPane.ERROR_MESSAGE);
				e1.printStackTrace();
			}
		});
	}
}