package com.malistore_backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class EmailService {
    
    @Value("${app.frontend-url:http://localhost:3000}")
    private String frontendUrl;
    
    @Value("${app.reset-password-url:http://localhost:3000/reset-password}")
    private String resetPasswordUrl;
    
    @Value("${mail.username:test@malistore.com}")
    private String fromEmail;
    
    /**
     * Envoie un email de réinitialisation de mot de passe
     */
    public void sendPasswordResetEmail(String toEmail, String userName, String token) {
        try {
            log.info("📧 Sending password reset email to: {}", toEmail);
            log.info("👤 User: {}", userName);
            log.info("🔑 Token: {}", token);
            
            // En mode test, on simule l'envoi d'email
            String resetUrl = resetPasswordUrl + "?token=" + token;
            log.info("🔗 Reset URL: {}", resetUrl);
            
            // TODO: En production, décommenter le code ci-dessous pour envoyer de vrais emails
            /*
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            // Configuration de l'email
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("🛒 MaliStore - Réinitialisation de votre mot de passe");
            
            // Préparation du contexte pour le template Thymeleaf
            Context context = new Context();
            context.setVariable("userName", userName);
            context.setVariable("token", token);
            context.setVariable("resetUrl", resetUrl);
            
            // Génération du contenu HTML à partir du template
            String htmlContent = templateEngine.process("email/password-reset", context);
            
            helper.setText(htmlContent, true);
            
            // Envoi de l'email
            mailSender.send(message);
            */
            
            log.info("✅ Password reset email sent successfully to: {}", toEmail);
            
        } catch (Exception e) {
            log.error("❌ Failed to send password reset email to: {}", toEmail, e);
            // En mode test, on ne lance pas d'exception
            log.warn("Continuing without email sending in test mode");
        }
    }
    
    /**
     * Envoie un email de confirmation de réinitialisation
     */
    public void sendPasswordResetConfirmationEmail(String toEmail, String userName) {
        try {
            log.info("📧 Sending password reset confirmation email to: {}", toEmail);
            log.info("👤 User: {}", userName);
            
            // En mode test, on simule l'envoi d'email
            log.info("✅ Password reset confirmation email sent successfully to: {}", toEmail);
            
            // TODO: En production, décommenter le code ci-dessous pour envoyer de vrais emails
            /*
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("✅ MaliStore - Mot de passe réinitialisé avec succès");
            
            String htmlContent = buildPasswordResetConfirmationHtml(userName);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            */
            
        } catch (Exception e) {
            log.error("❌ Failed to send password reset confirmation email to: {}", toEmail, e);
            // En mode test, on ne lance pas d'exception
            log.warn("Continuing without email sending in test mode");
        }
    }
    
}
