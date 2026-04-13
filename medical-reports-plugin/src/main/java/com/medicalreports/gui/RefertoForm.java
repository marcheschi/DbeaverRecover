package com.medicalreports.gui;

import com.medicalreports.database.DatabaseManager;
import com.medicalreports.model.Referto;
import com.medicalreports.util.SecurityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.time.LocalDateTime;

/**
 * Form per la creazione e modifica di un referto medico
 */
public class RefertoForm extends JDialog {
    
    private static final Logger logger = LoggerFactory.getLogger(RefertoForm.class);
    
    private DatabaseManager databaseManager;
    private Referto referto;
    private boolean refertoSalvato = false;
    
    // Campi del form
    private JTextField txtNome;
    private JTextField txtCognome;
    private JTextField txtCodiceFiscale;
    private JTextField txtDataNascita;
    private JComboBox<String> comboSesso;
    private JTextField txtTipoReferto;
    private JTextField txtSpecializzazione;
    private JTextField txtMedicoRichiedente;
    private JTextField txtMedicoCompilatore;
    private JTextField txtStrutturaSanitaria;
    private JTextArea areaTestoReferto;
    private JTextArea areaDiagnosi;
    private JTextArea areaTerapia;
    private JTextArea areaNote;
    private JComboBox<String> comboPriorita;
    private JComboBox<String> comboStato;
    
    public RefertoForm(Frame owner, DatabaseManager databaseManager, Referto referto) {
        super(owner, referto == null ? "Nuovo Referto" : "Modifica Referto", true);
        this.databaseManager = databaseManager;
        this.referto = referto != null ? referto : new Referto();
        
        initializeForm();
        if (referto != null) {
            caricaDatiReferto();
        }
    }
    
    private void initializeForm() {
        setSize(900, 700);
        setLocationRelativeTo(getOwner());
        setLayout(new BorderLayout(10, 10));
        
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        int row = 0;
        
        // Sezione Dati Paziente
        row = aggiungiSezione(mainPanel, "Dati Paziente", gbc, row);
        
        row = aggiungiCampo(mainPanel, gbc, row, "Nome:", txtNome = new JTextField(20));
        row = aggiungiCampo(mainPanel, gbc, row, "Cognome:", txtCognome = new JTextField(20));
        row = aggiungiCampo(mainPanel, gbc, row, "Codice Fiscale:", txtCodiceFiscale = new JTextField(16), 16);
        
        JPanel dataSessoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        txtDataNascita = new JTextField(12);
        txtDataNascita.setToolTipText("Formato: GG/MM/AAAA");
        dataSessoPanel.add(new JLabel("Data Nascita:"));
        dataSessoPanel.add(txtDataNascita);
        dataSessoPanel.add(Box.createHorizontalStrut(20));
        dataSessoPanel.add(new JLabel("Sesso:"));
        String[] sessi = {"M", "F", "ALTRO"};
        comboSesso = new JComboBox<>(sessi);
        dataSessoPanel.add(comboSesso);
        
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        mainPanel.add(dataSessoPanel, gbc);
        row++;
        
        // Sezione Informazioni Referto
        row = aggiungiSezione(mainPanel, "Informazioni Referto", gbc, row);
        
        row = aggiungiCampo(mainPanel, gbc, row, "Tipo Referto:", txtTipoReferto = new JTextField(30));
        row = aggiungiCampo(mainPanel, gbc, row, "Specializzazione:", txtSpecializzazione = new JTextField(30));
        row = aggiungiCampo(mainPanel, gbc, row, "Medico Richiedente:", txtMedicoRichiedente = new JTextField(30));
        row = aggiungiCampo(mainPanel, gbc, row, "Medico Compilatore:", txtMedicoCompilatore = new JTextField(30));
        row = aggiungiCampo(mainPanel, gbc, row, "Struttura Sanitaria:", txtStrutturaSanitaria = new JTextField(30));
        
        // Sezione Contenuto Referto
        row = aggiungiSezione(mainPanel, "Contenuto Referto", gbc, row);
        
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        mainPanel.add(new JLabel("Testo Referto:"), gbc);
        
        gbc.gridx = 1;
        areaTestoReferto = new JTextArea(8, 40);
        areaTestoReferto.setLineWrap(true);
        areaTestoReferto.setWrapStyleWord(true);
        JScrollPane scrollTesto = new JScrollPane(areaTestoReferto);
        mainPanel.add(scrollTesto, gbc);
        row++;
        
        gbc.gridx = 0;
        gbc.gridy = row;
        mainPanel.add(new JLabel("Diagnosi:"), gbc);
        
        gbc.gridx = 1;
        areaDiagnosi = new JTextArea(4, 40);
        areaDiagnosi.setLineWrap(true);
        areaDiagnosi.setWrapStyleWord(true);
        JScrollPane scrollDiagnosi = new JScrollPane(areaDiagnosi);
        mainPanel.add(scrollDiagnosi, gbc);
        row++;
        
        gbc.gridx = 0;
        gbc.gridy = row;
        mainPanel.add(new JLabel("Terapia:"), gbc);
        
        gbc.gridx = 1;
        areaTerapia = new JTextArea(4, 40);
        areaTerapia.setLineWrap(true);
        areaTerapia.setWrapStyleWord(true);
        JScrollPane scrollTerapia = new JScrollPane(areaTerapia);
        mainPanel.add(scrollTerapia, gbc);
        row++;
        
        gbc.gridx = 0;
        gbc.gridy = row;
        mainPanel.add(new JLabel("Note:"), gbc);
        
        gbc.gridx = 1;
        areaNote = new JTextArea(3, 40);
        areaNote.setLineWrap(true);
        areaNote.setWrapStyleWord(true);
        JScrollPane scrollNote = new JScrollPane(areaNote);
        mainPanel.add(scrollNote, gbc);
        row++;
        
        // Sezione Stato e Priorità
        row = aggiungiSezione(mainPanel, "Stato e Priorità", gbc, row);
        
        JPanel statoPrioritaPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        String[] stati = {"BOZZA", "COMPLETATO", "FIRMATO", "ARCHIVIATO"};
        comboStato = new JComboBox<>(stati);
        statoPrioritaPanel.add(new JLabel("Stato:"));
        statoPrioritaPanel.add(comboStato);
        statoPrioritaPanel.add(Box.createHorizontalStrut(20));
        
        String[] priorita = {"NORMALE", "URGENTE", "PRIORITARIO"};
        comboPriorita = new JComboBox<>(priorita);
        statoPrioritaPanel.add(new JLabel("Priorità:"));
        statoPrioritaPanel.add(comboPriorita);
        
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        mainPanel.add(statoPrioritaPanel, gbc);
        row++;
        
        // Pannello bottoni
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        
        JButton btnSalva = new JButton("Salva");
        btnSalva.addActionListener(this::salvaReferto);
        buttonPanel.add(btnSalva);
        
        JButton btnGeneraHash = new JButton("Genera Hash Integrità");
        btnGeneraHash.addActionListener(e -> generaHashIntegrita());
        buttonPanel.add(btnGeneraHash);
        
        JButton btnAnnulla = new JButton("Annulla");
        btnAnnulla.addActionListener(e -> chiudiForm());
        buttonPanel.add(btnAnnulla);
        
        add(mainPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private int aggiungiSezione(JPanel panel, String titolo, GridBagConstraints gbc, int row) {
        JLabel lblTitolo = new JLabel(titolo);
        lblTitolo.setFont(new Font("Arial", Font.BOLD, 14));
        lblTitolo.setForeground(Color.BLUE);
        
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        panel.add(lblTitolo, gbc);
        
        return row + 1;
    }
    
    private int aggiungiCampo(JPanel panel, GridBagConstraints gbc, int row, String label, JTextField textField) {
        return aggiungiCampo(panel, gbc, row, label, textField, 30);
    }
    
    private int aggiungiCampo(JPanel panel, GridBagConstraints gbc, int row, String label, JTextField textField, int columns) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        panel.add(new JLabel(label), gbc);
        
        gbc.gridx = 1;
        textField.setColumns(columns);
        panel.add(textField, gbc);
        
        return row + 1;
    }
    
    private void caricaDatiReferto() {
        txtNome.setText(referto.getNomePaziente());
        txtCognome.setText(referto.getCognomePaziente());
        txtCodiceFiscale.setText(referto.getCodiceFiscalePaziente());
        txtDataNascita.setText(referto.getDataNascita());
        comboSesso.setSelectedItem(referto.getSesso());
        txtTipoReferto.setText(referto.getTipoReferto());
        txtSpecializzazione.setText(referto.getSpecializzazione());
        txtMedicoRichiedente.setText(referto.getMedicoRichiedente());
        txtMedicoCompilatore.setText(referto.getMedicoCompilatore());
        txtStrutturaSanitaria.setText(referto.getStrutturaSanitaria());
        areaTestoReferto.setText(referto.getTestoReferto());
        areaDiagnosi.setText(referto.getDiagnosi());
        areaTerapia.setText(referto.getTerapia());
        areaNote.setText(referto.getNote());
        comboStato.setSelectedItem(referto.getStato());
        comboPriorita.setSelectedItem(referto.getPriorita());
    }
    
    private void salvaReferto(ActionEvent e) {
        // Validazione campi obbligatori
        if (txtCognome.getText().trim().isEmpty() || 
            txtNome.getText().trim().isEmpty() || 
            txtCodiceFiscale.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Inserire almeno Cognome, Nome e Codice Fiscale", 
                "Validazione", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Aggiorna i dati del referto
        referto.setNomePaziente(txtNome.getText().trim());
        referto.setCognomePaziente(txtCognome.getText().trim());
        referto.setCodiceFiscalePaziente(txtCodiceFiscale.getText().trim().toUpperCase());
        referto.setDataNascita(txtDataNascita.getText().trim());
        referto.setSesso(comboSesso.getSelectedItem().toString());
        referto.setTipoReferto(txtTipoReferto.getText().trim());
        referto.setSpecializzazione(txtSpecializzazione.getText().trim());
        referto.setMedicoRichiedente(txtMedicoRichiedente.getText().trim());
        referto.setMedicoCompilatore(txtMedicoCompilatore.getText().trim());
        referto.setStrutturaSanitaria(txtStrutturaSanitaria.getText().trim());
        referto.setTestoReferto(areaTestoReferto.getText().trim());
        referto.setDiagnosi(areaDiagnosi.getText().trim());
        referto.setTerapia(areaTerapia.getText().trim());
        referto.setNote(areaNote.getText().trim());
        referto.setStato(comboStato.getSelectedItem().toString());
        referto.setPriorita(comboPriorita.getSelectedItem().toString());
        
        // Genera hash di integrità se non presente
        if (referto.getHashIntegrita() == null || referto.getHashIntegrita().isEmpty()) {
            generaHashIntegrita();
        }
        
        // Salva nel database
        boolean salvataggioRiuscito;
        if (referto.getId() == null) {
            // Nuovo referto
            salvataggioRiuscito = databaseManager.salvaReferto(referto);
        } else {
            // Aggiornamento referto esistente
            referto.setDataModifica(LocalDateTime.now());
            salvataggioRiuscito = databaseManager.aggiornaReferto(referto);
        }
        
        if (salvataggioRiuscito) {
            JOptionPane.showMessageDialog(this, 
                "Referto salvato con successo!", 
                "Successo", 
                JOptionPane.INFORMATION_MESSAGE);
            refertoSalvato = true;
            chiudiForm();
        } else {
            JOptionPane.showMessageDialog(this, 
                "Errore durante il salvataggio del referto", 
                "Errore", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void generaHashIntegrita() {
        String contenuto = areaTestoReferto.getText() + areaDiagnosi.getText() + areaTerapia.getText();
        String hash = SecurityUtil.generaHash(contenuto);
        referto.setHashIntegrita(hash);
        JOptionPane.showMessageDialog(this, 
            "Hash di integrità generato: " + hash.substring(0, 16) + "...", 
            "Hash Generato", 
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void chiudiForm() {
        dispose();
    }
    
    public boolean isRefertoSalvato() {
        return refertoSalvato;
    }
}
