package com.example.anvisos.qr.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.springframework.stereotype.Service;

@Service
public class QrCodeService {
    private static final int QR_SIZE = 360;

    public byte[] generatePng(String content) {
        try {
            BitMatrix matrix = new QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, QR_SIZE, QR_SIZE);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matrix, "PNG", outputStream);
            return outputStream.toByteArray();
        } catch (WriterException | IOException ex) {
            throw new IllegalStateException("Failed to generate QR PNG", ex);
        }
    }

    public byte[] generateSvg(String content) {
        try {
            BitMatrix matrix = new QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, QR_SIZE, QR_SIZE);
            StringBuilder svg = new StringBuilder();
            svg.append("<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"")
                    .append(QR_SIZE)
                    .append("\" height=\"")
                    .append(QR_SIZE)
                    .append("\" shape-rendering=\"crispEdges\">");

            for (int y = 0; y < QR_SIZE; y++) {
                for (int x = 0; x < QR_SIZE; x++) {
                    if (matrix.get(x, y)) {
                        svg.append("<rect x=\"")
                                .append(x)
                                .append("\" y=\"")
                                .append(y)
                                .append("\" width=\"1\" height=\"1\" fill=\"#000\"/>");
                    }
                }
            }
            svg.append("</svg>");
            return svg.toString().getBytes(StandardCharsets.UTF_8);
        } catch (WriterException ex) {
            throw new IllegalStateException("Failed to generate QR SVG", ex);
        }
    }
    public byte[] generateLockscreen(String content) {
        try {
            BitMatrix matrix = new QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, 400, 400);
            java.awt.image.BufferedImage qrImage = MatrixToImageWriter.toBufferedImage(matrix);

            java.awt.image.BufferedImage lockscreen = new java.awt.image.BufferedImage(1080, 1920, java.awt.image.BufferedImage.TYPE_INT_RGB);
            java.awt.Graphics2D g = lockscreen.createGraphics();

            g.setColor(new java.awt.Color(30, 41, 59));
            g.fillRect(0, 0, 1080, 1920);

            g.setColor(new java.awt.Color(239, 68, 68));
            g.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 80));
            String title = "EMERGENCY / KHẨN CẤP";
            java.awt.FontMetrics fm = g.getFontMetrics();
            int titleX = (1080 - fm.stringWidth(title)) / 2;
            g.drawString(title, titleX, 600);

            g.setColor(java.awt.Color.WHITE);
            g.setFont(new java.awt.Font("Arial", java.awt.Font.PLAIN, 40));
            String subTitle = "Quét mã để xem hồ sơ và liên hệ người thân";
            java.awt.FontMetrics fmSub = g.getFontMetrics();
            int subX = (1080 - fmSub.stringWidth(subTitle)) / 2;
            g.drawString(subTitle, subX, 700);

            int qrX = (1080 - 400) / 2;
            g.drawImage(qrImage, qrX, 800, null);

            g.dispose();

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            javax.imageio.ImageIO.write(lockscreen, "PNG", outputStream);
            return outputStream.toByteArray();
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to generate lockscreen", ex);
        }
    }
}

