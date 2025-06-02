package com.aranthalion.presupuesto.presentation.email

data class EmailProvider(
    val name: String,
    val imapServer: String,
    val imapPort: Int,
    val imapSecurity: String, // e.g., "SSL/TLS", "STARTTLS"
    val pop3Server: String,
    val pop3Port: Int,
    val pop3Security: String, // e.g., "SSL/TLS", "STARTTLS"
    val smtpServer: String,
    val smtpPort: Int,
    val smtpSecurity: String // e.g., "SSL/TLS", "STARTTLS"
)

val popularEmailProviders = listOf(
    EmailProvider(
        name = "Disroot.org",
        imapServer = "disroot.org",
        imapPort = 993,
        imapSecurity = "SSL/TLS",
        pop3Server = "disroot.org",
        pop3Port = 995,
        pop3Security = "SSL/TLS",
        smtpServer = "disroot.org",
        smtpPort = 587,
        smtpSecurity = "STARTTLS"
    ),
    EmailProvider(
        name = "Cock.li",
        imapServer = "mail.cock.li",
        imapPort = 993,
        imapSecurity = "SSL/TLS",
        pop3Server = "mail.cock.li",
        pop3Port = 995,
        pop3Security = "SSL/TLS",
        smtpServer = "mail.cock.li",
        smtpPort = 465, // Alternativa común: puerto 587 con STARTTLS
        smtpSecurity = "SSL/TLS"
    ),
    EmailProvider(
        name = "Posteo.de (De pago)",
        imapServer = "posteo.de",
        imapPort = 993,
        imapSecurity = "SSL/TLS",
        pop3Server = "posteo.de",
        pop3Port = 995,
        pop3Security = "SSL/TLS",
        smtpServer = "posteo.de",
        smtpPort = 465, // Alternativa común: puerto 587 con STARTTLS
        smtpSecurity = "SSL/TLS"
    ),
    EmailProvider(
        name = "GMX.com / .net / .de",
        imapServer = "imap.gmx.com",
        imapPort = 993,
        imapSecurity = "SSL/TLS",
        pop3Server = "pop.gmx.com",
        pop3Port = 995,
        pop3Security = "SSL/TLS",
        smtpServer = "mail.gmx.com",
        smtpPort = 587, // Alternativa: puerto 465 con SSL
        smtpSecurity = "STARTTLS"
        // Nota: Requiere activación manual de POP3/IMAP en la configuración web de GMX.
        // El acceso puede desactivarse automáticamente si no se usa por un tiempo.
    ),
    EmailProvider(
        name = "WEB.DE",
        imapServer = "imap.web.de",
        imapPort = 993,
        imapSecurity = "SSL/TLS",
        pop3Server = "pop3.web.de",
        pop3Port = 995,
        pop3Security = "SSL/TLS",
        smtpServer = "smtp.web.de",
        smtpPort = 587, // Alternativa: puerto 465 con SSL
        smtpSecurity = "STARTTLS"
        // Nota: Requiere activación manual de POP3/IMAP en la configuración web de WEB.DE.
        // El acceso puede desactivarse automáticamente si no se usa por un tiempo.
        // Si 2FA está activo, podría requerir una contraseña de aplicación.
    ),
    EmailProvider(
        name = "mail.de",
        imapServer = "imap.mail.de",
        imapPort = 993,
        imapSecurity = "SSL/TLS",
        pop3Server = "pop.mail.de",
        pop3Port = 995,
        pop3Security = "SSL/TLS",
        smtpServer = "smtp.mail.de",
        smtpPort = 587, // Alternativa: puerto 465 con SSL/TLS
        smtpSecurity = "STARTTLS"
        // Nota: Acceso POP3/IMAP disponible para FreeMail.
        // Verificar comportamiento si 2FA está activado (podría requerir contraseña de aplicación).
    )
    // Se pueden agregar más proveedores aquí si se encuentran más que cumplan los criterios.
) 