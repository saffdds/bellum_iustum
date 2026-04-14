import javax.swing.*;
import javax.swing.event.*;
import javax.swing.undo.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.URI;

public class Blocchino extends JFrame {
    private JTextArea textArea;
    private JLabel labelStato, labelCaratteri;
    private int fontSize = 12;
    private File fileCorrente;
    private String cartellaSafe;
    private boolean autoSaveAttivo = false;
    private UndoManager undoManager = new UndoManager();

    public Blocchino() {
        ottieniPercorsoIniziale();
        fileCorrente = new File(cartellaSafe, "Blocchino_AutoSave.txt");

        setTitle("Blocchino");
        setSize(700, 600);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) { confermaUscita(); }
        });

        textArea = new JTextArea();
        textArea.setFont(new Font("Arial", Font.PLAIN, fontSize));
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        
        // --- UNDO/REDO LISTENER ---
        textArea.getDocument().addUndoableEditListener(e -> undoManager.addEdit(e.getEdit()));
        
        add(new JScrollPane(textArea), BorderLayout.CENTER);

        // Barra di Stato
        JPanel frameStato = new JPanel(new BorderLayout());
        labelStato = new JLabel(" Pronto");
        labelCaratteri = new JLabel("Caratteri: 0  ");
        frameStato.add(labelStato, BorderLayout.WEST);
        frameStato.add(labelCaratteri, BorderLayout.EAST);
        add(frameStato, BorderLayout.SOUTH);

        creaMenuCompleto();
        configuraScorciatoie();

        // Conteggio caratteri
        textArea.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { aggiornaConteggio(); }
            public void removeUpdate(DocumentEvent e) { aggiornaConteggio(); }
            public void changedUpdate(DocumentEvent e) { aggiornaConteggio(); }
        });

        new Thread(this::autoSaveSentinel).start();
        setVisible(true);
    }

    // --- FUNZIONI LOGICHE ---
    private void ottieniPercorsoIniziale() {
        String p = "/mnt/c/Users/aless/OneDrive/Desktop/IFV/Documenti";
        if (new File(p).exists()) cartellaSafe = p;
        else {
            cartellaSafe = System.getProperty("user.home") + File.separator + "Documents";
            new File(cartellaSafe).mkdirs();
        }
    }

    private void autoSaveSentinel() {
        while (true) {
            try {
                Thread.sleep(30000);
                if (autoSaveAttivo && fileCorrente != null) {
                    salvaSuFile(fileCorrente);
                    SwingUtilities.invokeLater(() -> aggiornaStato("✅ Backup automatico eseguito"));
                }
            } catch (Exception e) { }
        }
    }

    private void aggiornaStato(String msg) {
        labelStato.setText(" " + msg);
        new Timer(5000, e -> labelStato.setText(" Pronto")).start();
    }

    private void aggiornaConteggio() {
        labelCaratteri.setText("Caratteri: " + textArea.getText().length() + "  ");
    }

    private void salvaSuFile(File f) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(f))) {
            pw.print(textArea.getText());
        } catch (IOException e) { aggiornaStato("❌ Errore"); }
    }

    // --- AZIONI MENU ---
    private void nuovoFile() {
        if (!textArea.getText().trim().isEmpty()) {
            int r = JOptionPane.showConfirmDialog(this, "Vuoi salvare prima?");
            if (r == JOptionPane.YES_OPTION) salvaFile(); else if (r == JOptionPane.CANCEL_OPTION) return;
        }
        textArea.setText("");
        fileCorrente = new File(cartellaSafe, "Blocchino_AutoSave.txt");
        aggiornaStato("Nuovo documento creato");
    }

    private void apriFile() {
        JFileChooser jfc = new JFileChooser(cartellaSafe);
        if (jfc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try (BufferedReader br = new BufferedReader(new FileReader(jfc.getSelectedFile()))) {
                textArea.read(br, null);
                fileCorrente = jfc.getSelectedFile();
                aggiornaStato("Aperto: " + fileCorrente.getName());
            } catch (IOException e) { aggiornaStato("❌ Errore"); }
        }
    }

    private void salvaFile() {
        if (fileCorrente.getName().equals("Blocchino_AutoSave.txt")) {
            JFileChooser jfc = new JFileChooser(cartellaSafe);
            if (jfc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) fileCorrente = jfc.getSelectedFile();
            else return;
        }
        salvaSuFile(fileCorrente);
        aggiornaStato("💾 Salvataggio completato");
    }

    private void confermaUscita() {
        if (!textArea.getText().trim().isEmpty()) {
            int r = JOptionPane.showConfirmDialog(this, "Vuoi salvare prima di uscire?");
            if (r == JOptionPane.YES_OPTION) { salvaFile(); System.exit(0); }
            else if (r == JOptionPane.NO_OPTION) System.exit(0);
        } else System.exit(0);
    }

    private void zoom(int d) {
        fontSize += d; if (fontSize < 6) fontSize = 6;
        textArea.setFont(new Font("Arial", Font.PLAIN, fontSize));
    }

    private void apriLink(String url) {
        try { Desktop.getDesktop().browse(new URI(url)); } catch (Exception e) { }
    }

    // --- COSTRUZIONE MENU ---
    private void creaMenuCompleto() {
        JMenuBar bar = new JMenuBar();

        // FILE
        JMenu fileM = new JMenu("File");
        addItem(fileM, "Nuovo (Ctrl+N)", e -> nuovoFile());
        addItem(fileM, "Apri (Ctrl+O)", e -> apriFile());
        addItem(fileM, "Salva (Ctrl+S)", e -> salvaFile());
        fileM.addSeparator();
        JCheckBoxMenuItem auto = new JCheckBoxMenuItem("Salvataggio automatico");
        auto.addActionListener(e -> autoSaveAttivo = auto.isSelected());
        fileM.add(auto);
        fileM.addSeparator();
        addItem(fileM, "Esci (Esc)", e -> confermaUscita());

        // MODIFICA
        JMenu editM = new JMenu("Modifica");
        addItem(editM, "Annulla (Ctrl+Z)", e -> { if(undoManager.canUndo()) undoManager.undo(); });
        addItem(editM, "Ripristina (Ctrl+Y)", e -> { if(undoManager.canRedo()) undoManager.redo(); });
        editM.addSeparator();
        addItem(editM, "Taglia (Ctrl+X)", e -> textArea.cut());
        addItem(editM, "Copia (Ctrl+C)", e -> textArea.copy());
        addItem(editM, "Incolla (Ctrl+V)", e -> textArea.paste());
        editM.addSeparator();
        addItem(editM, "Impostazioni ⚙️", e -> {
            String input = JOptionPane.showInputDialog(this, "Nuovo percorso:", cartellaSafe);
            if (input != null && new File(input).isDirectory()) { cartellaSafe = input; aggiornaStato("📍 Percorso aggiornato!"); }
        });

        // FORMATO
        JMenu formatM = new JMenu("Formato");
        addItem(formatM, "MAIUSCOLO", e -> { String s = textArea.getSelectedText(); if(s!=null) textArea.replaceSelection(s.toUpperCase()); });
        addItem(formatM, "minuscolo", e -> { String s = textArea.getSelectedText(); if(s!=null) textArea.replaceSelection(s.toLowerCase()); });

        // VISUALIZZA
        JMenu viewM = new JMenu("Visualizza");
        addItem(viewM, "Tema Chiaro", e -> { textArea.setBackground(Color.WHITE); textArea.setForeground(Color.BLACK); textArea.setCaretColor(Color.BLACK); });
        addItem(viewM, "Tema Scuro", e -> { textArea.setBackground(new Color(44, 44, 44)); textArea.setForeground(Color.WHITE); textArea.setCaretColor(Color.WHITE); });
        viewM.addSeparator();
        addItem(viewM, "Zoom +", e -> zoom(2));
        addItem(viewM, "Zoom -", e -> zoom(-2));
        addItem(viewM, "Zoom Reset", e -> { fontSize = 12; zoom(0); });

        // AIUTO (CON LA FUNZIONE RIPRISTINATA!)
        JMenu helpM = new JMenu("Aiuto");
        addItem(helpM, "Guida all'uso", e -> JOptionPane.showMessageDialog(this, "Ctrl+N: Nuovo\nCtrl+O: Apri\nCtrl+S: Salva\nZoom: Ctrl e +/-"));
        addItem(helpM, "Sito di Supporto", e -> apriLink("https://saffdds.github.io/bellum_iustum/aiuto_blocchino.html"));
        helpM.addSeparator();
        // --- ECCOLA QUI! ---
        addItem(helpM, "Informazioni su blocchino", e -> JOptionPane.showMessageDialog(this, "Blocchino v2.1.0 for Java\nAutore: Saffdds\nStato: Operativo", "Informazioni", JOptionPane.INFORMATION_MESSAGE));

        bar.add(fileM); bar.add(editM); bar.add(formatM); bar.add(viewM); bar.add(helpM);
        setJMenuBar(bar);
    }

    private void addItem(JMenu m, String label, ActionListener al) {
        JMenuItem item = new JMenuItem(label);
        item.addActionListener(al);
        m.add(item);
    }

    private void configuraScorciatoie() {
        InputMap im = textArea.getInputMap(JComponent.WHEN_FOCUSED);
        ActionMap am = textArea.getActionMap();
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK), "Undo");
        am.put("Undo", new AbstractAction() { public void actionPerformed(ActionEvent e) { if(undoManager.canUndo()) undoManager.undo(); } });
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_DOWN_MASK), "Redo");
        am.put("Redo", new AbstractAction() { public void actionPerformed(ActionEvent e) { if(undoManager.canRedo()) undoManager.redo(); } });
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK), "N");
        am.put("N", new AbstractAction() { public void actionPerformed(ActionEvent e) { nuovoFile(); } });
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK), "S");
        am.put("S", new AbstractAction() { public void actionPerformed(ActionEvent e) { salvaFile(); } });
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK), "O");
        am.put("O", new AbstractAction() { public void actionPerformed(ActionEvent e) { apriFile(); } });
    }

    public static void main(String[] args) { SwingUtilities.invokeLater(Blocchino::new); }
}