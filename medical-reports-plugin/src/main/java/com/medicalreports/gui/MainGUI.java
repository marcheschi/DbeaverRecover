package com.medicalreports.gui;

import com.medicalreports.database.DatabaseManager;
import com.medicalreports.model.Referto;
import com.medicalreports.util.SecurityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Interfaccia grafica principale per la gestione dei referti medici
 */
public class MainGUI extends JFrame {
    
    private static final Logger logger = LoggerFactory.getLogger(MainGUI.class);
    
    private DatabaseManager databaseManager;
    private JTable tabellaReferti;
    private DefaultTableModel tableModel;
    private JTextField campoRicercaCognome;
    private JTextField campoRicercaCF;
    private JComboBox<String> comboStato;
    private JLabel lblStatus;
    
    public MainGUI() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            logger.warn("Impossibile impostare il look and feel di sistema", e);
        }
        
        databaseManager = new DatabaseManager();
        initializeGUI();
    }
    
    private void initializeGUI() {
        setTitle("Medical Reports Plugin - Gestione Referti");
        setSize(1200, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Pannello principale
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Pannello di ricerca
        JPanel searchPanel = createSearchPanel();
        mainPanel.add(searchPanel, BorderLayout.NORTH);
        
        // Tabella dei referti
        JPanel tablePanel = createTablePanel();
        mainPanel.add(tablePanel, BorderLayout.CENTER);
        
        // Pannello dei bottoni
        JPanel buttonPanel = createButtonPanel();
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        // Status bar
        lblStatus = new JLabel("Pronto");
        lblStatus.setBorder(BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        add(lblStatus, BorderLayout.SOUTH);
        
        add(mainPanel, BorderLayout.CENTER);
        
        // Carica i referti all'avvio
        caricaReferti();
    }
    
    private JPanel createSearchPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        panel.setBorder(BorderFactory.createTitledBorder("Ricerca"));
        
        panel.add(new JLabel("Cognome:"));
        campoRicercaCognome = new JTextField(15);
        panel.add(campoRicercaCognome);
        
        panel.add(new JLabel("Codice Fiscale:"));
        campoRicercaCF = new JTextField(15);
        panel.add(campoRicercaCF);
        
        panel.add(new JLabel("Stato:"));
        String[] stati = {"Tutti", "BOZZA", "COMPLETATO", "FIRMATO", "ARCHIVIATO"};
        comboStato = new JComboBox<>(stati);
        panel.add(comboStato);
        
        JButton btnCerca = new JButton("Cerca");
        btnCerca.addActionListener(e -> cercaReferti());
        panel.add(btnCerca);
        
        JButton btnReset = new JButton("Reset");
        btnReset.addActionListener(e -> resetRicerca());
        panel.add(btnReset);
        
        return panel;
    }
    
    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        String[] colonne = {"ID", "Paziente", "Codice Fiscale", "Tipo", "Specializzazione", 
                           "Medico", "Data", "Stato", "Priorità"};
        tableModel = new DefaultTableModel(colonne, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        tabellaReferti = new JTable(tableModel);
        tabellaReferti.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabellaReferti.setAutoCreateRowSorter(true);
        
        JScrollPane scrollPane = new JScrollPane(tabellaReferti);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        
        JButton btnNuovo = new JButton("Nuovo Referto");
        btnNuovo.addActionListener(e -> apriFormNuovoReferto());
        panel.add(btnNuovo);
        
        JButton btnModifica = new JButton("Modifica");
        btnModifica.addActionListener(e -> modificaRefertoSelezionato());
        panel.add(btnModifica);
        
        JButton btnVisualizza = new JButton("Visualizza");
        btnVisualizza.addActionListener(e -> visualizzaRefertoSelezionato());
        panel.add(btnVisualizza);
        
        JButton btnElimina = new JButton("Elimina");
        btnElimina.addActionListener(e -> eliminaRefertoSelezionato());
        panel.add(btnElimina);
        
        JButton btnExportPDF = new JButton("Esporta PDF");
        btnExportPDF.addActionListener(e -> esportaRefertoPDF());
        panel.add(btnExportPDF);
        
        JButton btnStampa = new JButton("Stampa");
        btnStampa.addActionListener(e -> stampaReferto());
        panel.add(btnStampa);
        
        return panel;
    }
    
    private void caricaReferti() {
        tableModel.setRowCount(0);
        List<Referto> referti = databaseManager.ottieniTuttiReferti(null, null, null);
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        
        for (Referto referto : referti) {
            Object[] riga = {
                referto.getId().substring(0, 8) + "...",
                referto.getCognomePaziente() + " " + referto.getNomePaziente(),
                SecurityUtil.offuscaCodiceFiscale(referto.getCodiceFiscalePaziente()),
                referto.getTipoReferto(),
                referto.getSpecializzazione(),
                referto.getMedicoCompilatore(),
                referto.getDataCreazione().format(formatter),
                referto.getStato(),
                referto.getPriorita()
            };
            tableModel.addRow(riga);
        }
        
        lblStatus.setText(referti.size() + " referti caricati");
    }
    
    private void cercaReferti() {
        tableModel.setRowCount(0);
        String cognome = campoRicercaCognome.getText().trim();
        String cf = campoRicercaCF.getText().trim();
        String stato = comboStato.getSelectedItem().toString();
        
        List<Referto> referti;
        
        if (!cf.isEmpty()) {
            referti = databaseManager.cercaRefertiPerCodiceFiscale(cf);
        } else if (!cognome.isEmpty()) {
            referti = databaseManager.cercaRefertiPerCognome(cognome);
        } else if (!"Tutti".equals(stato)) {
            referti = databaseManager.ottieniTuttiReferti(stato, null, null);
        } else {
            referti = databaseManager.ottieniTuttiReferti(null, null, null);
        }
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        
        for (Referto referto : referti) {
            Object[] riga = {
                referto.getId().substring(0, 8) + "...",
                referto.getCognomePaziente() + " " + referto.getNomePaziente(),
                SecurityUtil.offuscaCodiceFiscale(referto.getCodiceFiscalePaziente()),
                referto.getTipoReferto(),
                referto.getSpecializzazione(),
                referto.getMedicoCompilatore(),
                referto.getDataCreazione().format(formatter),
                referto.getStato(),
                referto.getPriorita()
            };
            tableModel.addRow(riga);
        }
        
        lblStatus.setText(referti.size() + " referti trovati");
    }
    
    private void resetRicerca() {
        campoRicercaCognome.setText("");
        campoRicercaCF.setText("");
        comboStato.setSelectedIndex(0);
        caricaReferti();
    }
    
    private void apriFormNuovoReferto() {
        RefertoForm form = new RefertoForm(this, databaseManager, null);
        form.setVisible(true);
        if (form.isRefertoSalvato()) {
            caricaReferti();
        }
    }
    
    private void modificaRefertoSelezionato() {
        int rigaSelezionata = tabellaReferti.getSelectedRow();
        if (rigaSelezionata == -1) {
            JOptionPane.showMessageDialog(this, 
                "Selezionare un referto da modificare", 
                "Nessuna selezione", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String idReferto = tableModel.getValueAt(rigaSelezionata, 0).toString();
        // Recupera l'ID completo dal database
        Referto referto = databaseManager.cercaRefertoPerId(idReferto + "...");
        
        if (referto != null) {
            RefertoForm form = new RefertoForm(this, databaseManager, referto);
            form.setVisible(true);
            if (form.isRefertoSalvato()) {
                caricaReferti();
            }
        } else {
            JOptionPane.showMessageDialog(this, 
                "Impossibile recuperare il referto", 
                "Errore", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void visualizzaRefertoSelezionato() {
        int rigaSelezionata = tabellaReferti.getSelectedRow();
        if (rigaSelezionata == -1) {
            JOptionPane.showMessageDialog(this, 
                "Selezionare un referto da visualizzare", 
                "Nessuna selezione", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        JOptionPane.showMessageDialog(this, 
            "Funzionalità di visualizzazione dettagliata in sviluppo", 
            "Info", 
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void eliminaRefertoSelezionato() {
        int rigaSelezionata = tabellaReferti.getSelectedRow();
        if (rigaSelezionata == -1) {
            JOptionPane.showMessageDialog(this, 
                "Selezionare un referto da eliminare", 
                "Nessuna selezione", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int conferma = JOptionPane.showConfirmDialog(this, 
            "Sei sicuro di voler eliminare questo referto?\nL'eliminazione è definitiva.", 
            "Conferma eliminazione", 
            JOptionPane.YES_NO_OPTION, 
            JOptionPane.WARNING_MESSAGE);
        
        if (conferma == JOptionPane.YES_OPTION) {
            String idReferto = tableModel.getValueAt(rigaSelezionata, 0).toString();
            if (databaseManager.eliminaReferto(idReferto + "...")) {
                JOptionPane.showMessageDialog(this, 
                    "Referto eliminato con successo", 
                    "Successo", 
                    JOptionPane.INFORMATION_MESSAGE);
                caricaReferti();
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Errore durante l'eliminazione del referto", 
                    "Errore", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void esportaRefertoPDF() {
        JOptionPane.showMessageDialog(this, 
            "Funzionalità di esportazione PDF in sviluppo", 
            "Info", 
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void stampaReferto() {
        JOptionPane.showMessageDialog(this, 
            "Funzionalità di stampa in sviluppo", 
            "Info", 
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    public void aggiornaStatus(String messaggio) {
        lblStatus.setText(messaggio);
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainGUI gui = new MainGUI();
            gui.setVisible(true);
        });
    }
}
