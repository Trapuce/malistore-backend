package com.malistore_backend.service;

import com.malistore_backend.data.entity.Product;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class StockAlertService {

    private final InventoryService inventoryService;

    @Value("${app.stock.alert-threshold:5}")
    private Integer stockAlertThreshold;

    @Value("${app.stock.alert-email:admin@malistore.com}")
    private String alertEmail;

    /**
     * Vérifie les stocks bas et envoie des alertes si nécessaire
     * Cette méthode est exécutée toutes les heures
     */
    @Scheduled(fixedRate = 3600000) // 1 heure = 3600000 ms
    public void checkLowStockAndSendAlerts() {
        log.info("Starting scheduled stock check with threshold: {}", stockAlertThreshold);
        
        try {
            List<Product> lowStockProducts = inventoryService.getProductsWithLowStock(stockAlertThreshold);
            
            if (!lowStockProducts.isEmpty()) {
                log.warn("Found {} products with low stock (threshold: {})", lowStockProducts.size(), stockAlertThreshold);
                
                // Log the low stock products
                for (Product product : lowStockProducts) {
                    log.warn("Low stock alert - Product: {} (ID: {}), Stock: {}", 
                            product.getName(), product.getId(), product.getStock());
                }
                
                // Send email alert (simulated for now)
                sendLowStockEmailAlert(lowStockProducts);
            } else {
                log.info("No products with low stock found");
            }
        } catch (Exception e) {
            log.error("Error during scheduled stock check: {}", e.getMessage(), e);
        }
    }

    /**
     * Envoie un email d'alerte pour les stocks bas
     */
    private void sendLowStockEmailAlert(List<Product> lowStockProducts) {
        try {
            StringBuilder message = new StringBuilder();
            message.append("ALERTE STOCK BAS\n\n");
            message.append("Les produits suivants ont un stock faible (seuil: ").append(stockAlertThreshold).append("):\n\n");
            
            for (Product product : lowStockProducts) {
                message.append("- ").append(product.getName())
                       .append(" (ID: ").append(product.getId())
                       .append(") - Stock: ").append(product.getStock())
                       .append("\n");
            }
            
            message.append("\nVeuillez vérifier et réapprovisionner ces produits.");
            
            // Simuler l'envoi d'email (pour les tests)
            log.info("Sending low stock alert email to: {}", alertEmail);
            log.info("Email content:\n{}", message.toString());
            
            // Dans un vrai environnement, vous utiliseriez:
            // emailService.sendLowStockAlert(alertEmail, message.toString());
            
        } catch (Exception e) {
            log.error("Error sending low stock email alert: {}", e.getMessage(), e);
        }
    }

    /**
     * Vérifie manuellement les stocks bas
     */
    public List<Product> getLowStockProducts() {
        return inventoryService.getProductsWithLowStock(stockAlertThreshold);
    }

    /**
     * Vérifie les stocks bas avec un seuil personnalisé
     */
    public List<Product> getLowStockProducts(Integer customThreshold) {
        return inventoryService.getProductsWithLowStock(customThreshold);
    }

    /**
     * Obtient le seuil d'alerte configuré
     */
    public Integer getStockAlertThreshold() {
        return stockAlertThreshold;
    }
}
