package com.garemobilegb.booking.controller;

import com.garemobilegb.shared.config.PaymentGatewayProperties;
import com.garemobilegb.shared.security.JwtService;
import io.jsonwebtoken.Claims;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Page HTML minimale pour simuler un paiement (sandbox). Désactivée si {@code app.payment.mode}
 * n’est pas {@code sandbox} — en prod les passerelles redirigent vers leurs URLs.
 */
@RestController
public class PaymentSandboxCheckoutController {

  private final JwtService jwtService;
  private final PaymentGatewayProperties paymentGatewayProperties;

  public PaymentSandboxCheckoutController(
      JwtService jwtService, PaymentGatewayProperties paymentGatewayProperties) {
    this.jwtService = jwtService;
    this.paymentGatewayProperties = paymentGatewayProperties;
  }

  @GetMapping(value = "/api/payments/sandbox/checkout", produces = MediaType.TEXT_HTML_VALUE)
  public String checkout(@RequestParam String token) {
    if (!paymentGatewayProperties.sandbox()) {
      return sandboxDisabledPage();
    }
    Claims claims = jwtService.parsePaymentCheckoutToken(token);
    long bookingId = claims.get(JwtService.CLAIM_BOOKING_ID, Long.class);
    String provider = claims.get(JwtService.CLAIM_PAYMENT_PROVIDER, String.class);
    String secret =
        paymentGatewayProperties.webhookSecret() != null
            ? paymentGatewayProperties.webhookSecret()
            : "";
    String secretJs = escapeForJsString(secret);
    String base = paymentGatewayProperties.publicBaseUrl().replaceAll("/$", "");
    String baseJs = escapeForJsString(base);

    return "<!DOCTYPE html><html><head><meta charset=\"utf-8\"/><meta name=\"viewport\" content=\"width=device-width, initial-scale=1\"/>"
        + "<title>Paiement sandbox</title>"
        + "<style>body{font-family:system-ui,sans-serif;max-width:420px;margin:2rem auto;padding:1rem}"
        + "button{background:#1e3a8a;color:#fff;border:none;padding:12px 20px;border-radius:8px;"
        + "cursor:pointer;font-size:16px;width:100%}.muted{color:#666;font-size:14px}.err{color:#b91c1c;font-size:14px}</style></head><body>"
        + "<h1>Paiement (sandbox)</h1>"
        + "<p class=\"muted\">Réservation #"
        + bookingId
        + " · "
        + escapeHtml(provider)
        + "</p>"
        + "<p>Simule la confirmation côté passerelle (webhook → réservation PAID).</p>"
        + "<button type=\"button\" id=\"go\">Confirmer le paiement</button>"
        + "<p id=\"err\" class=\"err\" style=\"display:none\"></p>"
        + "<script>(function(){"
        + "var secret='"
        + secretJs
        + "';"
        + "var base='"
        + baseJs
        + "';"
        + "var webhookUrl=base+'/api/webhooks/payments/sandbox';"
        + "document.getElementById('go').onclick=function(){"
        + "document.getElementById('err').style.display='none';"
        + "fetch(webhookUrl,{method:'POST',"
        + "headers:{'Content-Type':'application/json','X-Sandbox-Secret':secret},"
        + "body:JSON.stringify({bookingId:"
        + bookingId
        + ",externalTransactionId:'sandbox-tx-'+Date.now(),status:'SUCCESS',provider:'"
        + escapeForJsString(provider)
        + "'})"
        + "}).then(function(r){return r.text().then(function(t){return {ok:r.ok,status:r.status,body:t};});})"
        + ".then(function(x){"
        + "if(x.ok){document.body.innerHTML='<p>Paiement enregistré. Retournez à l’application.</p>';}"
        + "else{var e=document.getElementById('err');e.style.display='block';e.textContent='Erreur '+x.status+': '+x.body;}"
        + "}).catch(function(err){"
        + "var e=document.getElementById('err');e.style.display='block';"
        + "e.textContent=err&&err.message?err.message:'Échec réseau (vérifiez l’URL publique du backend).';"
        + "});"
        + "};"
        + "})();</script>"
        + "</body></html>";
  }

  private static String sandboxDisabledPage() {
    return "<!DOCTYPE html><html><head><meta charset=\"utf-8\"/><title>Sandbox</title></head><body>"
        + "<p>Le simulateur de paiement n’est disponible que si <code>app.payment.mode=sandbox</code>.</p>"
        + "</body></html>";
  }

  private static String escapeForJsString(String s) {
    if (s == null) {
      return "";
    }
    return s.replace("\\", "\\\\").replace("'", "\\'").replace("\n", "").replace("\r", "");
  }

  private static String escapeHtml(String s) {
    if (s == null) {
      return "";
    }
    return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
  }
}
