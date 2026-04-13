package com.medicalreports.service;

import com.medicalreports.model.Referto;
import org.apache.pdfbox.pddocument.PDDocument;
import org.apache.pdfbox.pddocument.PDPage;
import org.apache.pdfbox.pddocument.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.format.DateTimeFormatter;

/**
 * Servizio per l'esportazione dei referti in formato PDF
 */
public class PDFExportService {
    
    private static final Logger logger = LoggerFactory.getLogger(PDFExportService.class);
    
    /**
     * Esporta un referto in formato PDF
     * 
     * @param referto Il referto da esportare
     * @param percorsoFile Il percorso del file PDF da creare
     * @return true se l'esportazione è riuscita, false altrimenti
     */
    public boolean esportaReferto(Referto referto, String percorsoFile) {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);
            
            PDPageContentStream contentStream = new PDPageContentStream(document, page);
            
            // Intestazione
            scriviIntestazione(contentStream, referto);
            
            // Dati paziente
            scriviDatiPaziente(contentStream, referto);
            
            // Contenuto referto
            scriviContenutoReferto(contentStream, referto);
            
            // Firma e hash
            scriviFirmaEHash(contentStream, referto);
            
            contentStream.close();
            
            // Salva il documento
            document.save(percorsoFile);
            
            logger.info("Referto esportato in PDF: {}", percorsoFile);
            return true;
            
        } catch (IOException e) {
            logger.error("Errore durante l'esportazione del PDF", e);
            return false;
        }
    }
    
    private void scriviIntestazione(PDPageContentStream contentStream, Referto referto) throws IOException {
        PDFont fontBold = PDType1Font.HELVETICA_BOLD;
        PDFont fontNormal = PDType1Font.HELVETICA;
        
        float yPosition = 750;
        float leftMargin = 50;
        
        // Titolo
        contentStream.beginText();
        contentStream.setFont(fontBold, 18);
        contentStream.newLineAtOffset(leftMargin, yPosition);
        contentStream.showText("REFERTO MEDICO");
        contentStream.endText();
        
        yPosition -= 40;
        
        // Tipo di referto
        contentStream.beginText();
        contentStream.setFont(fontNormal, 12);
        contentStream.newLineAtOffset(leftMargin, yPosition);
        contentStream.showText("Tipo: " + referto.getTipoReferto());
        contentStream.endText();
        
        yPosition -= 20;
        
        // Specializzazione
        contentStream.beginText();
        contentStream.newLineAtOffset(leftMargin, yPosition);
        contentStream.showText("Specializzazione: " + referto.getSpecializzazione());
        contentStream.endText();
        
        yPosition -= 20;
        
        // Struttura sanitaria
        contentStream.beginText();
        contentStream.newLineAtOffset(leftMargin, yPosition);
        contentStream.showText("Struttura: " + referto.getStrutturaSanitaria());
        contentStream.endText();
        
        yPosition -= 30;
        
        // Linea separatrice
        contentStream.moveTo(leftMargin, yPosition);
        contentStream.lineTo(550, yPosition);
        contentStream.stroke();
        
        yPosition -= 20;
    }
    
    private void scriviDatiPaziente(PDPageContentStream contentStream, Referto referto) throws IOException {
        PDFont fontBold = PDType1Font.HELVETICA_BOLD;
        PDFont fontNormal = PDType1Font.HELVETICA;
        
        float yPosition = 580;
        float leftMargin = 50;
        
        // Sezione dati paziente
        contentStream.beginText();
        contentStream.setFont(fontBold, 14);
        contentStream.newLineAtOffset(leftMargin, yPosition);
        contentStream.showText("DATI PAZIENTE");
        contentStream.endText();
        
        yPosition -= 25;
        
        contentStream.beginText();
        contentStream.setFont(fontNormal, 11);
        contentStream.newLineAtOffset(leftMargin, yPosition);
        contentStream.showText("Cognome e Nome: " + referto.getCognomePaziente() + " " + referto.getNomePaziente());
        contentStream.endText();
        
        yPosition -= 18;
        
        contentStream.beginText();
        contentStream.newLineAtOffset(leftMargin, yPosition);
        contentStream.showText("Codice Fiscale: " + referto.getCodiceFiscalePaziente());
        contentStream.endText();
        
        yPosition -= 18;
        
        contentStream.beginText();
        contentStream.newLineAtOffset(leftMargin, yPosition);
        contentStream.showText("Data di Nascita: " + referto.getDataNascita() + " | Sesso: " + referto.getSesso());
        contentStream.endText();
        
        yPosition -= 30;
        
        // Linea separatrice
        contentStream.moveTo(leftMargin, yPosition);
        contentStream.lineTo(550, yPosition);
        contentStream.stroke();
        
        yPosition -= 20;
    }
    
    private void scriviContenutoReferto(PDPageContentStream contentStream, Referto referto) throws IOException {
        PDFont fontBold = PDType1Font.HELVETICA_BOLD;
        PDFont fontNormal = PDType1Font.HELVETICA;
        
        float yPosition = 450;
        float leftMargin = 50;
        float rightMargin = 50;
        float maxWidth = 500;
        
        // Testo referto
        contentStream.beginText();
        contentStream.setFont(fontBold, 12);
        contentStream.newLineAtOffset(leftMargin, yPosition);
        contentStream.showText("DESCRIZIONE DEL REFERTO:");
        contentStream.endText();
        
        yPosition -= 20;
        
        if (referto.getTestoReferto() != null && !referto.getTestoReferto().isEmpty()) {
            yPosition = scriviTestoMultiriga(contentStream, referto.getTestoReferto(), leftMargin, yPosition, maxWidth, fontNormal, 10);
        }
        
        yPosition -= 15;
        
        // Diagnosi
        contentStream.beginText();
        contentStream.setFont(fontBold, 12);
        contentStream.newLineAtOffset(leftMargin, yPosition);
        contentStream.showText("DIAGNOSI:");
        contentStream.endText();
        
        yPosition -= 18;
        
        if (referto.getDiagnosi() != null && !referto.getDiagnosi().isEmpty()) {
            yPosition = scriviTestoMultiriga(contentStream, referto.getDiagnosi(), leftMargin, yPosition, maxWidth, fontNormal, 10);
        }
        
        yPosition -= 15;
        
        // Terapia
        contentStream.beginText();
        contentStream.setFont(fontBold, 12);
        contentStream.newLineAtOffset(leftMargin, yPosition);
        contentStream.showText("TERAPIA INDICATA:");
        contentStream.endText();
        
        yPosition -= 18;
        
        if (referto.getTerapia() != null && !referto.getTerapia().isEmpty()) {
            yPosition = scriviTestoMultiriga(contentStream, referto.getTerapia(), leftMargin, yPosition, maxWidth, fontNormal, 10);
        }
        
        yPosition -= 20;
    }
    
    private float scriviTestoMultiriga(PDPageContentStream contentStream, String testo, 
                                       float startX, float startY, float maxWidth, 
                                       PDFont font, int fontSize) throws IOException {
        String[] parole = testo.split("\\s+");
        StringBuilder linea = new StringBuilder();
        float yPosition = startY;
        
        for (String parola : parole) {
            String testLinea = linea.length() > 0 ? linea + " " + parola : parola;
            float larghezzaTesto = font.getStringWidth(testLinea) / 1000 * fontSize;
            
            if (larghezzaTesto > maxWidth) {
                contentStream.beginText();
                contentStream.setFont(font, fontSize);
                contentStream.newLineAtOffset(startX, yPosition);
                contentStream.showText(linea.toString());
                contentStream.endText();
                
                linea = new StringBuilder(parola);
                yPosition -= 14;
                
                if (yPosition < 100) {
                    // Nuova pagina se si raggiunge il fondo
                    PDPage nuovaPagina = new PDPage();
                    contentStream.getDocument().addPage(nuovaPagina);
                    contentStream.close();
                    contentStream = new PDPageContentStream(contentStream.getDocument(), nuovaPagina);
                    yPosition = 750;
                }
            } else {
                linea.append(parola).append(" ");
            }
        }
        
        if (linea.length() > 0) {
            contentStream.beginText();
            contentStream.setFont(font, fontSize);
            contentStream.newLineAtOffset(startX, yPosition);
            contentStream.showText(linea.toString().trim());
            contentStream.endText();
            yPosition -= 14;
        }
        
        return yPosition;
    }
    
    private void scriviFirmaEHash(PDPageContentStream contentStream, Referto referto) throws IOException {
        PDFont fontBold = PDType1Font.HELVETICA_BOLD;
        PDFont fontNormal = PDType1Font.HELVETICA;
        
        float yPosition = 150;
        float leftMargin = 50;
        
        // Linea separatrice
        contentStream.moveTo(leftMargin, yPosition + 10);
        contentStream.lineTo(550, yPosition + 10);
        contentStream.stroke();
        
        yPosition -= 20;
        
        // Medico compilatore
        contentStream.beginText();
        contentStream.setFont(fontNormal, 10);
        contentStream.newLineAtOffset(leftMargin, yPosition);
        contentStream.showText("Medico Compilatore: " + referto.getMedicoCompilatore());
        contentStream.endText();
        
        yPosition -= 18;
        
        // Medico richiedente
        contentStream.beginText();
        contentStream.newLineAtOffset(leftMargin, yPosition);
        contentStream.showText("Medico Richiedente: " + referto.getMedicoRichiedente());
        contentStream.endText();
        
        yPosition -= 25;
        
        // Data creazione
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        contentStream.beginText();
        contentStream.setFont(fontBold, 9);
        contentStream.newLineAtOffset(leftMargin, yPosition);
        contentStream.showText("Data Creazione: " + referto.getDataCreazione().format(formatter));
        contentStream.endText();
        
        yPosition -= 15;
        
        // Stato e priorità
        contentStream.beginText();
        contentStream.setFont(fontNormal, 9);
        contentStream.newLineAtOffset(leftMargin, yPosition);
        contentStream.showText("Stato: " + referto.getStato() + " | Priorità: " + referto.getPriorita());
        contentStream.endText();
        
        yPosition -= 20;
        
        // Hash di integrità
        if (referto.getHashIntegrita() != null && !referto.getHashIntegrita().isEmpty()) {
            contentStream.beginText();
            contentStream.setFont(fontBold, 8);
            contentStream.newLineAtOffset(leftMargin, yPosition);
            contentStream.showText("Hash Integrità: " + referto.getHashIntegrita());
            contentStream.endText();
        }
        
        // Avviso legale
        yPosition -= 30;
        contentStream.beginText();
        contentStream.setFont(fontNormal, 7);
        contentStream.newLineAtOffset(leftMargin, yPosition);
        contentStream.showText("Questo documento è generato automaticamente. Per verificare l'integrità, controllare l'hash.");
        contentStream.endText();
    }
}
