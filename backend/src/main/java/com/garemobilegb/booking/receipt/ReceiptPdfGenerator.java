package com.garemobilegb.booking.receipt;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.RoundingMode;
import org.springframework.stereotype.Component;

@Component
public class ReceiptPdfGenerator {

  public byte[] generate(ReceiptPdfData d) {
    try {
      return generateInternal(d);
    } catch (DocumentException | IOException e) {
      throw new IllegalStateException("Échec génération PDF reçu", e);
    }
  }

  private byte[] generateInternal(ReceiptPdfData d) throws DocumentException, IOException {
    BaseFont bf = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
    Font titleFont = new Font(bf, 16, Font.BOLD);
    Font bodyFont = new Font(bf, 11, Font.NORMAL);
    Font smallFont = new Font(bf, 9, Font.NORMAL);

    Document document = new Document();
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    PdfWriter.getInstance(document, out);
    document.open();

    document.add(new Paragraph("eGare Mobile GB — Reçu de paiement", titleFont));
    document.add(new Paragraph(" ", bodyFont));
    document.add(new Paragraph("N° réservation : " + d.bookingId(), bodyFont));
    document.add(new Paragraph("Émis le : " + d.issuedAtText(), bodyFont));
    document.add(new Paragraph("Réservation créée : " + d.reservationCreatedAtText(), bodyFont));
    document.add(new Paragraph(" ", bodyFont));
    document.add(new Paragraph("Gare : " + nullSafe(d.stationName()), bodyFont));
    document.add(new Paragraph("Ligne / destination : " + nullSafe(d.routeLabel()), bodyFont));
    document.add(new Paragraph("Véhicule : " + nullSafe(d.registrationCode()), bodyFont));
    document.add(
        new Paragraph(
            "Siège : " + (d.seatNumber() != null ? d.seatNumber() : "—"), bodyFont));
    document.add(new Paragraph(" ", bodyFont));
    String amountLine =
        "Montant : "
            + formatAmount(d.amount())
            + " "
            + nullSafe(d.currency())
            + " — Statut paiement : "
            + nullSafe(d.paymentStatus());
    document.add(new Paragraph(amountLine, bodyFont));
    document.add(new Paragraph("Fournisseur : " + nullSafe(d.paymentProvider()), bodyFont));
    if (d.providerRef() != null && !d.providerRef().isBlank()) {
      document.add(new Paragraph("Réf. fournisseur : " + d.providerRef(), bodyFont));
    }
    document.add(new Paragraph("Statut réservation : " + nullSafe(d.bookingStatus()), bodyFont));
    document.add(new Paragraph(" ", bodyFont));
    document.add(
        new Paragraph(
            "Conservez ce document. Pour toute question, contactez le support de la gare.",
            smallFont));
    document.add(
        new Paragraph("Document informatif — eGare Mobile GB.", smallFont));

    document.close();
    return out.toByteArray();
  }

  private static String nullSafe(String s) {
    return s != null && !s.isBlank() ? s : "—";
  }

  private static String formatAmount(java.math.BigDecimal amount) {
    if (amount == null) {
      return "—";
    }
    return amount.setScale(0, RoundingMode.HALF_UP).toPlainString();
  }
}
