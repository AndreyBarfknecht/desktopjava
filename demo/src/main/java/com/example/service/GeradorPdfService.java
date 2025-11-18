package com.example.service;

import com.example.model.Matricula;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle; // Import para o Retângulo
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
// IMPORT CORRIGIDO (sem o .standard no final)
import org.apache.pdfbox.pdmodel.font.Standard14Fonts; 
// IMPORT ADICIONADO (para usar o FontName diretamente)
import org.apache.pdfbox.pdmodel.font.Standard14Fonts.FontName; 

import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class GeradorPdfService {

    public void gerarCertificado(Matricula matricula, int cargaHorariaTotal, String codigoVerificacao) throws IOException {
        
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Salvar Certificado");
        fileChooser.setInitialFileName("Certificado_" + matricula.getNomeAluno().replace(" ", "_") + ".pdf");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        
        File file = fileChooser.showSaveDialog(new Stage());
        if (file == null) {
            return; 
        }

        try (PDDocument document = new PDDocument()) {
            
            // --- CORREÇÃO PAISAGEM ---
            // Criamos um novo retângulo baseado no A4, mas com altura e largura trocadas
            PDRectangle landscapeA4 = new PDRectangle(PDRectangle.A4.getHeight(), PDRectangle.A4.getWidth());
            PDPage page = new PDPage(landscapeA4); // Usa o novo retângulo
            document.addPage(page);

            PDPageContentStream contentStream = new PDPageContentStream(document, page);

            // --- CORREÇÃO FONTES ---
            // Usamos a enumeração FontName que importámos
            PDType1Font fontBold = new PDType1Font(FontName.HELVETICA_BOLD);
            PDType1Font fontRegular = new PDType1Font(FontName.HELVETICA);
            PDType1Font fontItalic = new PDType1Font(FontName.HELVETICA_OBLIQUE);

            // ----- Título (Coordenadas Ajustadas para Paisagem) -----
            contentStream.beginText();
            contentStream.setFont(fontBold, 24);
            contentStream.newLineAtOffset(220, 500); // (X, Y)
            contentStream.showText("CERTIFICADO DE CONCLUSÃO");
            contentStream.endText();

            // ----- Texto Principal (Coordenadas Ajustadas) -----
            contentStream.beginText();
            contentStream.setFont(fontRegular, 14);
            contentStream.newLineAtOffset(100, 400); 
            contentStream.showText("Certificamos que ");
            contentStream.setFont(fontBold, 14);
            contentStream.showText(matricula.getNomeAluno());
            contentStream.setFont(fontRegular, 14);
            contentStream.showText(", portador(a) do CPF nº ");
            contentStream.setFont(fontBold, 14);
            contentStream.showText(matricula.getCpfAluno());
            contentStream.setFont(fontRegular, 14);
            contentStream.showText(",");
            contentStream.newLineAtOffset(0, -20); // Nova linha
            contentStream.showText("concluiu com sucesso o curso de ");
            contentStream.setFont(fontBold, 14);
            contentStream.showText(matricula.getTurma().getCurso().getNomeCurso());
            contentStream.setFont(fontRegular, 14);
            contentStream.showText(",");
            contentStream.newLineAtOffset(0, -20); // Nova linha
            contentStream.showText(String.format("com carga horária total de %d horas.", cargaHorariaTotal));
            contentStream.endText();
            
            // ----- Data de Emissão (Coordenadas Ajustadas) -----
            LocalDate hoje = LocalDate.now();
            DateTimeFormatter formatter = DateTimeFormatter
                .ofPattern("dd 'de' MMMM 'de' yyyy", Locale.of("pt", "BR")); 
            String dataPorExtenso = "Getúlio Vargas, " + hoje.format(formatter);

            contentStream.beginText();
            contentStream.setFont(fontRegular, 12);
            contentStream.newLineAtOffset(500, 250); 
            contentStream.showText(dataPorExtenso);
            contentStream.endText();

            // ----- Assinatura (Coordenadas Ajustadas) -----
            contentStream.beginText();
            contentStream.setFont(fontItalic, 12);
            contentStream.newLineAtOffset(530, 180); 
            contentStream.showText("_________________________");
            contentStream.newLineAtOffset(25, -15);
            contentStream.showText("Assinatura do Diretor");
            contentStream.endText();

            // ----- Código de Verificação (Coordenadas Ajustadas) -----
            contentStream.beginText();
            contentStream.setFont(fontRegular, 9);
            contentStream.newLineAtOffset(100, 80); 
            contentStream.showText("Código de Verificação: " + codigoVerificacao);
            contentStream.endText();

            contentStream.close();
            document.save(file);
        }
    }
}