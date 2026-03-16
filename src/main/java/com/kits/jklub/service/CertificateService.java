package com.kits.jklub.service;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.pdf.*;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.io.font.constants.StandardFonts;
import org.springframework.stereotype.Service;
import java.io.*;

@Service
public class CertificateService {

    public byte[] generateCertificate(String userName) throws Exception {

        try (InputStream templateStream = getClass().getResourceAsStream("/templates/certificate_template.pdf");
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            if (templateStream == null) throw new FileNotFoundException("Template PDF not found!");

            PdfReader reader = new PdfReader(templateStream);
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(reader, writer);

            Document document = new Document(pdfDoc);

            // -------- AUTO FIT NAME --------

            float pageWidth = pdfDoc.getDefaultPageSize().getWidth();
            float maxWidth = pageWidth - 200;

            PdfFont font = PdfFontFactory.createFont(StandardFonts.TIMES_BOLDITALIC);

            int fontSize = 40;

            while (font.getWidth(userName, fontSize) > maxWidth) {
                fontSize--;
            }

            Paragraph name = new Paragraph(userName)
                    .setFont(font)
                    .setFontSize(fontSize)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFixedPosition(1, 30, 310, pageWidth);

            document.add(name);

            document.close();

            return baos.toByteArray();
        }
    }
}