import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Timer;
import java.util.TimerTask;

public class Blocchino {

    // Variabili di stato globali
    private static int fontSize = 12;
    private static final String[] cartellaSafe = new String[1];
    private static final String[] fileCorrente = new String[1];
    
    private static JFrame finestra;
    private static JTextPane textArea;
    private static JLabel labelStato;
    private static JLabel labelCaratteri;
    private static JCheckBoxMenuItem autoSaveMenuItem;
    private static Timer autoSaveTimer;

    public static void main(String[] args) {
        // Inizializzazione percorsi
        cartellaSafe[0] = ottieniPercorsoIniziale();
        fileCorrente[0] = Paths.get(cartellaSafe[0], "Blocchino_AutoSave.txt").toString();

        // Configurazione interfaccia Swing
        SwingUtilities.invokeLater(() -> {
            finestra = new JFrame("Blocchino");
            finestra.setSize(700, 600);
            finestra.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            finestra.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    confermaUscita();
                }
            });

            // Area di testo
            textArea = new JTextPane();
            textArea.setFont(new Font("Arial", Font.PLAIN, fontSize));
            JScrollPane scrollPane = new JScrollPane(textArea);
            finestra.add(scrollPane, BorderLayout.CENTER);

            // Barra di stato
            JPanel frameStato = new JPanel(new BorderLayout());
            frameStato.setBorder(BorderFactory.createEtchedBorder());
            labelStato = new JLabel(" Pronto");
            labelCaratteri = new JLabel("Caratteri: 0   ");
            frameStato.add(labelStato, BorderLayout.WEST);
            frameStato.add(labelCaratteri, BorderLayout.EAST);
            finestra.add(frameStato, BorderLayout.SOUTH);

            // Costruzione Menu
            costruisciMenu();

            // Listener per il conteggio caratteri in tempo reale
            textArea.addKeyListener(new KeyAdapter() {
                @Override
                public void keyReleased(KeyEvent e) {
                    aggiornaConteggio();
                }
            });

            // Avvio del timer per il salvataggio automatico (ogni 30 secondi)
            avviaAutoSaveTimer();

            aggiornaConteggio();
            finestra.setLocationRelativeTo(null);
            finestra.setVisible(true);
        });
    }

    // ============================================================
    // --- LOGICA PERCORSO ---
    // ============================================================
    private static String ottieniPercorsoIniziale() {
        String p = "/mnt/c/Users/aless/OneDrive/Desktop/IFV/Documenti";
        if (Files.exists(Paths.get(p))) {
            return p;
        }
        String home = System.getProperty("user.home");
        Path docLinux = Paths.get(home, "Documents");
        try {
            Files.createDirectories(docLinux);
        } catch (IOException ignored) {}
        return docLinux.toString();
    }

    // ============================================================
    // --- LOGICA AUTO-SAVE ---
    // ============================================================
    private static void avviaAutoSaveTimer() {
        autoSaveTimer = new Timer(true);
        autoSaveTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (autoSaveMenuItem != null && autoSaveMenuItem.isSelected() && fileCorrente[0] != null) {
                    try {
                        String contenuto = textArea.getText();
                        Files.write(Paths.get(fileCorrente[0]), contenuto.getBytes(StandardCharsets.UTF_8));
                        aggiornaStato("✅ Backup automatico eseguito");
                    } catch (Exception e) {
                        aggiornaStato("❌ Errore Auto-Save");
                    }
                }
            }
        }, 30000, 30000);
    }

    private static void aggiornaStato(String messaggio) {
        SwingUtilities.invokeLater(() -> {
            labelStato.setText(" " + messaggio);
            Timer t = new Timer(true);
            t.schedule(new TimerTask() {
                @Override
                public void run() {
                    SwingUtilities.invokeLater(() -> labelStato.setText(" Pronto"));
                }
            }, 5000);
        });
    }

    private static void aggiornaConteggio() {
        String contenuto = textArea.getText();
        labelCaratteri.setText("Caratteri: " + contenuto.length() + "   ");
    }

    // ============================================================
    // --- FINESTRA IMPOSTAZIONI ---
    // ============================================================
    private static void apriImpostazioni() {
        JDialog winSettings = new JDialog(finestra, "Impostazioni", true);
        winSettings.setSize(400, 250);
        winSettings.setResizable(false);
        winSettings.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 15));

        JLabel titolo = new JLabel("Configurazione Blocchino");
        titolo.setFont(new Font("Arial", Font.BOLD, 12));
        winSettings.add(titolo);

        JLabel lblPercorso = new JLabel("<html><center>Attuale: " + cartellaSafe[0] + "</center></html>");
        lblPercorso.setForeground(Color.BLUE);
        winSettings.add(lblPercorso);

        JButton btnCambia = new JButton("Cambia Cartella");
        btnCambia.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            if (chooser.showOpenDialog(winSettings) == JFileChooser.APPROVE_OPTION) {
                cartellaSafe[0] = chooser.getSelectedFile().getAbsolutePath();
                fileCorrente[0] = Paths.get(cartellaSafe[0], "Blocchino_AutoSave.txt").toString();
                lblPercorso.setText("<html><center>Attuale: " + cartellaSafe[0] + "</center></html>");
                aggiornaStato("📍 Percorso aggiornato!");
            }
        });
        winSettings.add(btnCambia);

        JButton btnChiudi = new JButton("Chiudi");
        btnChiudi.addActionListener(e -> winSettings.dispose());
        winSettings.add(btnChiudi);

        winSettings.setLocationRelativeTo(finestra);
        winSettings.setVisible(true);
    }

    // ============================================================
    // --- TROVA E SOSTITUISCI ---
    // ============================================================
    private static void apriTrovaSostituisci() {
        JDialog winFind = new JDialog(finestra, "Trova e Sostituisci", false);
        winFind.setSize(440, 200);
        winFind.setResizable(false);
        winFind.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0;
        winFind.add(new JLabel("Trova:"), gbc);

        JTextField entryTrova = new JTextField(25);
        gbc.gridx = 1; gbc.gridy = 0;
        winFind.add(entryTrova, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        winFind.add(new JLabel("Sostituisci con:"), gbc);

        JTextField entrySostituisci = new JTextField(25);
        gbc.gridx = 1; gbc.gridy = 1;
        winFind.add(entrySostituisci, gbc);

        JPanel frameBtn = new JPanel();
        JButton btnTrova = new JButton("Trova");
        JButton btnSostituisci = new JButton("Sostituisci");
        JButton btnSostituisciTutto = new JButton("Sostituisci Tutto");

        // Azione Trova
        btnTrova.addActionListener(e -> {
            String parola = entryTrova.getText();
            if (parola.isEmpty()) {
                aggiornaStato("⚠️ Inserisci una parola da cercare!");
                return;
            }
            String testo = textArea.getText();
            int index = testo.indexOf(parola);
            if (index >= 0) {
                textArea.setCaretPosition(index);
                textArea.select(index, index + parola.length());
                aggiornaStato("🔍 Trovato riscontro per '" + parola + "'");
            } else {
                aggiornaStato("⚠️ Parola non trovata!");
            }
        });

        // Azione Sostituisci
        btnSostituisci.addActionListener(e -> {
            String parola = entryTrova.getText();
            String nuovaParola = entrySostituisci.getText();
            if (parola.isEmpty()) return;

            String selectedText = textArea.getSelectedText();
            if (selectedText != null && selectedText.equals(parola)) {
                textArea.replaceSelection(nuovaParola);
                aggiornaConteggio();
                aggiornaStato("✅ Sostituzione eseguita");
            } else {
                btnTrova.getActionListeners()[0].actionPerformed(e);
                String sel = textArea.getSelectedText();
                if (sel != null && sel.equals(parola)) {
                    textArea.replaceSelection(nuovaParola);
                    aggiornaConteggio();
                    aggiornaStato("✅ Sostituzione eseguita");
                }
            }
        });

        // Azione Sostituisci Tutto
        btnSostituisciTutto.addActionListener(e -> {
            String parola = entryTrova.getText();
            String nuovaParola = entrySostituisci.getText();
            if (parola.isEmpty()) return;

            String contenuto = textArea.getText();
            if (contenuto.contains(parola)) {
                String nuovoContenuto = contenuto.replace(parola, nuovaParola);
                textArea.setText(nuovoContenuto);
                aggiornaConteggio();
                aggiornaStato("⚡ Sostituzione completata!");
            } else {
                aggiornaStato("⚠️ Parola non trovata!");
            }
        });

        frameBtn.add(btnTrova);
        frameBtn.add(btnSostituisci);
        frameBtn.add(btnSostituisciTutto);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        winFind.add(frameBtn, gbc);

        winFind.setLocationRelativeTo(finestra);
        winFind.setVisible(true);
    }

    // ============================================================
    // --- FUNZIONI FILE ---
    // ============================================================
    private static void nuovoFile() {
        if (!textArea.getText().trim().isEmpty()) {
            int risposta = JOptionPane.showConfirmDialog(finestra, "Vuoi salvare prima?", "Nuovo File", JOptionPane.YES_NO_CANCEL_OPTION);
            if (risposta == JOptionPane.YES_OPTION) {
                salvaFile();
            } else if (risposta == JOptionPane.CANCEL_OPTION || risposta == JOptionPane.CLOSED_OPTION) {
                return;
            }
        }
        textArea.setText("");
        fileCorrente[0] = Paths.get(cartellaSafe[0], "Blocchino_AutoSave.txt").toString();
        aggiornaConteggio();
        aggiornaStato("Nuovo documento creato");
    }

    private static void apriFile() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(finestra) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            try {
                byte[] encoded = Files.readAllBytes(file.toPath());
                String contenuto = new String(encoded, StandardCharsets.UTF_8);
                textArea.setText(contenuto);
                fileCorrente[0] = file.getAbsolutePath();
                aggiornaConteggio();
                aggiornaStato("Aperto: " + file.getName());
            } catch (Exception e) {
                JOptionPane.showMessageDialog(finestra, "Impossibile aprire il file:\n\n" + e.getMessage(), "Errore apertura", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private static void salvaFile() {
        if (fileCorrente[0] == null || fileCorrente[0].contains("Blocchino_AutoSave.txt")) {
            JFileChooser chooser = new JFileChooser();
            if (chooser.showSaveDialog(finestra) == JFileChooser.APPROVE_OPTION) {
                fileCorrente[0] = chooser.getSelectedFile().getAbsolutePath();
            } else {
                return;
            }
        }
        try {
            Files.write(Paths.get(fileCorrente[0]), textArea.getText().getBytes(StandardCharsets.UTF_8));
            aggiornaStato("💾 Salvataggio completato");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(finestra, "Impossibile salvare il file:\n\n" + e.getMessage(), "Errore salvataggio", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ============================================================
    // --- APERTURA HTML NEL BROWSER ---
    // ============================================================
    private static void apriHtmlNelBrowser() {
        String contenuto = textArea.getText();
        if (contenuto.trim().isEmpty()) {
            aggiornaStato("⚠️ Il documento è vuoto!");
            return;
        }
        try {
            File tempFile = File.createTempFile("preview_", ".html");
            tempFile.deleteOnExit();
            Files.write(tempFile.toPath(), contenuto.getBytes(StandardCharsets.UTF_8));
            Desktop.getDesktop().browse(tempFile.toURI());
            aggiornaStato("🌐 Anteprima HTML aperta nel browser");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(finestra, "Impossibile aprire l'HTML:\n\n" + e.getMessage(), "Errore HTML", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ============================================================
    // --- USCITA ---
    // ============================================================
    private static void confermaUscita() {
        if (!textArea.getText().trim().isEmpty()) {
            int risposta = JOptionPane.showConfirmDialog(finestra, "Vuoi salvare prima di uscire?", "Esci", JOptionPane.YES_NO_CANCEL_OPTION);
            if (risposta == JOptionPane.YES_OPTION) {
                salvaFile();
                System.exit(0);
            } else if (risposta == JOptionPane.NO_OPTION) {
                System.exit(0);
            }
        } else {
            System.exit(0);
        }
    }

    // ============================================================
    // --- FORMATO E ZOOM ---
    // ============================================================
    private static void zoomIn() {
        fontSize += 2;
        aggiornaFontTotale();
    }

    private static void zoomOut() {
        if (fontSize > 6) {
            fontSize -= 2;
            aggiornaFontTotale();
        }
    }

    private static void resetZoom() {
        fontSize = 12;
        aggiornaFontTotale();
    }

    private static void aggiornaFontTotale() {
        Font f = textArea.getFont();
        textArea.setFont(new Font(f.getFamily(), f.getStyle(), fontSize));
    }

    private static void applicaStileTesto(int stile) {
        StyledDocument doc = textArea.getStyledDocument();
        int start = textArea.getSelectionStart();
        int end = textArea.getSelectionEnd();
        if (start == end) {
            aggiornaStato("⚠️ Seleziona prima del testo!");
            return;
        }
        AttributeSet attr = doc.getCharacterElement(start).getAttributes();
        MutableAttributeSet newAttr = new SimpleAttributeSet(attr);

        if (stile == Font.BOLD) {
            boolean isBold = StyleConstants.isBold(attr);
            StyleConstants.setBold(newAttr, !isBold);
        } else if (stile == Font.ITALIC) {
            boolean isItalic = StyleConstants.isItalic(attr);
            StyleConstants.setItalic(newAttr, !isItalic);
        } else if (stile == 100) { // Sottolineato
            boolean isUnder = StyleConstants.isUnderline(attr);
            StyleConstants.setUnderline(newAttr, !isUnder);
        }
        doc.setCharacterAttributes(start, end - start, newAttr, false);
        aggiornaStato("✨ Formattazione applicata");
    }

    private static void testoMaiuscolo() {
        String sel = textArea.getSelectedText();
        if (sel != null) {
            textArea.replaceSelection(sel.toUpperCase());
            aggiornaConteggio();
        } else {
            aggiornaStato("⚠️ Seleziona testo!");
        }
    }

    private static void testoMinuscolo() {
        String sel = textArea.getSelectedText();
        if (sel != null) {
            textArea.replaceSelection(sel.toLowerCase());
            aggiornaConteggio();
        } else {
            aggiornaStato("⚠️ Seleziona testo!");
        }
    }

    // ============================================================
    // --- SUPPORTO E GUIDA ---
    // ============================================================
    private static void mostraGuida() {
        String guida = "Comandi Rapidi di Blocchino:\n" +
                "• Ctrl+N : Nuovo Documento\n" +
                "• Ctrl+O : Apri File esistente\n" +
                "• Ctrl+S : Salva Manuale\n" +
                "• Ctrl+B : Grassetto\n" +
                "• Ctrl+I : Corsivo\n" +
                "• Ctrl+U : Sottolineato\n" +
                "• F4 o Ctrl+F : Trova e Sostituisci\n" +
                "• Ctrl+Plus/Minus : Regola lo Zoom\n\n" +
                "Percorso salvataggio automatico attuale:\n" + cartellaSafe[0];
        JOptionPane.showMessageDialog(finestra, guida, "Guida all'uso", JOptionPane.INFORMATION_MESSAGE);
    }

    private static void vaiAlSito() {
        try {
            Desktop.getDesktop().browse(new URI("https://saffdds.github.io/bellum_iustum/aiuto_blocchino.html"));
        } catch (Exception ignored) {}
    }

    private static void informazioniSoftware() {
        JOptionPane.showMessageDialog(finestra, "Blocchino v3.0.0 Beta 1 (Java 8 Port)\nAutore: Saffdds\nSupporto: TXT + HTML + Markdown", "Informazioni", JOptionPane.INFORMATION_MESSAGE);
    }

    // ============================================================
    // --- COSTRUZIONE MENU E SCORCIATOIE ---
    // ============================================================
    private static void costruisciMenu() {
        JMenuBar menuBar = new JMenuBar();

        // Menu File
        JMenu fileMenu = new JMenu("File");
        JMenuItem mNuovo = new JMenuItem("Nuovo");
        mNuovo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK));
        mNuovo.addActionListener(e -> nuovoFile());
        fileMenu.add(mNuovo);

        JMenuItem mApri = new JMenuItem("Apri");
        mApri.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));
        mApri.addActionListener(e -> apriFile());
        fileMenu.add(mApri);

        JMenuItem mSalva = new JMenuItem("Salva");
        mSalva.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
        mSalva.addActionListener(e -> salvaFile());
        fileMenu.add(mSalva);

        fileMenu.addSeparator();
        JMenuItem mHtml = new JMenuItem("Apri HTML nel browser 🌐");
        mHtml.addActionListener(e -> apriHtmlNelBrowser());
        fileMenu.add(mHtml);

        fileMenu.addSeparator();
        autoSaveMenuItem = new JCheckBoxMenuItem("Salvataggio automatico");
        fileMenu.add(autoSaveMenuItem);

        fileMenu.addSeparator();
        JMenuItem mEsci = new JMenuItem("Esci");
        mEsci.addActionListener(e -> confermaUscita());
        fileMenu.add(mEsci);

        menuBar.add(fileMenu);

        // Menu Modifica
        JMenu editMenu = new JMenu("Modifica");
        JMenuItem mTrova = new JMenuItem("Trova e Sostituisci");
        mTrova.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, 0));
        mTrova.addActionListener(e -> apriTrovaSostituisci());
        editMenu.add(mTrova);

        JMenuItem mTrovaCtrlF = new JMenuItem("Trova (Ctrl+F)");
        mTrovaCtrlF.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK));
        mTrovaCtrlF.addActionListener(e -> apriTrovaSostituisci());
        editMenu.add(mTrovaCtrlF);

        editMenu.addSeparator();
        JMenuItem mImpostazioni = new JMenuItem("Impostazioni ⚙️");
        mImpostazioni.addActionListener(e -> apriImpostazioni());
        editMenu.add(mImpostazioni);
        menuBar.add(editMenu);

        // Menu Formato
        JMenu formatMenu = new JMenu("Formato");
        JMenuItem mBold = new JMenuItem("Grassetto");
        mBold.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B, InputEvent.CTRL_DOWN_MASK));
        mBold.addActionListener(e -> applicaStileTesto(Font.BOLD));
        formatMenu.add(mBold);

        JMenuItem mItalic = new JMenuItem("Corsivo");
        mItalic.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, InputEvent.CTRL_DOWN_MASK));
        mItalic.addActionListener(e -> applicaStileTesto(Font.ITALIC));
        formatMenu.add(mItalic);

        JMenuItem mUnder = new JMenuItem("Sottolineato");
        mUnder.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_U, InputEvent.CTRL_DOWN_MASK));
        mUnder.addActionListener(e -> applicaStileTesto(100));
        formatMenu.add(mUnder);

        formatMenu.addSeparator();
        JMenuItem mMaiusc = new JMenuItem("MAIUSCOLO");
        mMaiusc.addActionListener(e -> testoMaiuscolo());
        formatMenu.add(mMaiusc);

        JMenuItem mMinusc = new JMenuItem("minuscolo");
        mMinusc.addActionListener(e -> testoMinuscolo());
        formatMenu.add(mMinusc);
        menuBar.add(formatMenu);

        // Menu Visualizza
        JMenu viewMenu = new JMenu("Visualizza");
        JMenuItem mChiaro = new JMenuItem("Tema Chiaro");
        mChiaro.addActionListener(e -> {
            textArea.setBackground(Color.WHITE);
            textArea.setForeground(Color.BLACK);
        });
        viewMenu.add(mChiaro);

        JMenuItem mScuro = new JMenuItem("Tema Scuro");
        mScuro.addActionListener(e -> {
            textArea.setBackground(new Color(44, 44, 44));
            textArea.setForeground(Color.WHITE);
        });
        viewMenu.add(mScuro);

        viewMenu.addSeparator();
        JMenuItem mZoomIn = new JMenuItem("Zoom +");
        mZoomIn.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, InputEvent.CTRL_DOWN_MASK));
        mZoomIn.addActionListener(e -> zoomIn());
        viewMenu.add(mZoomIn);

        JMenuItem mZoomOut = new JMenuItem("Zoom -");
        mZoomOut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, InputEvent.CTRL_DOWN_MASK));
        mZoomOut.addActionListener(e -> zoomOut());
        viewMenu.add(mZoomOut);

        JMenuItem mZoomReset = new JMenuItem("Zoom Reset");
        mZoomReset.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_0, InputEvent.CTRL_DOWN_MASK));
        mZoomReset.addActionListener(e -> resetZoom());
        viewMenu.add(mZoomReset);
        menuBar.add(viewMenu);

        // Menu Aiuto
        JMenu helpMenu = new JMenu("Aiuto");
        JMenuItem mGuida = new JMenuItem("Guida all'uso");
        mGuida.addActionListener(e -> mostraGuida());
        helpMenu.add(mGuida);

        JMenuItem mSito = new JMenuItem("Sito di Supporto");
        mSito.addActionListener(e -> vaiAlSito());
        helpMenu.add(mSito);

        helpMenu.addSeparator();
        JMenuItem mInfo = new JMenuItem("Informazioni su blocchino");
        mInfo.addActionListener(e -> informazioniSoftware());
        helpMenu.add(mInfo);
        menuBar.add(helpMenu);

        finestra.setJMenuBar(menuBar);
    }
}