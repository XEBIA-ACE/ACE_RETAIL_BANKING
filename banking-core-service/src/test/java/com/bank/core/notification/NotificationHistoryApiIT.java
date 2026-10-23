package com.bank.core.notification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bank.core.notification.domain.NotificationLog;
import com.bank.core.notification.domain.NotificationType;
import com.bank.core.notification.repository.NotificationHistoryRepository;
import com.bank.core.notification.service.NotificationRetentionJob;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

/** End-to-end US-028 flow: signed JWT -> security -> controller -> service -> JPA -> Flyway schema. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class NotificationHistoryApiIT {

    private static final String URL = "/api/v1/notifications/history";
    private static final long ALICE = 1001L;
    private static final long BOB = 1002L;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private NotificationHistoryRepository repository;

    @Autowired
    private NotificationRetentionJob retentionJob;

    @Autowired
    private JdbcTemplate jdbc;

    @Value("${security.jwt.secret}")
    private String secret;

    private String token(String signingSecret, long customerId) {
        SecretKeySpec key = new SecretKeySpec(signingSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder().subject("customer-" + customerId)
                .claim("customer_id", customerId).issuedAt(now).expiresAt(now.plusSeconds(300)).build();
        return new NimbusJwtEncoder(new ImmutableSecret<>(key))
                .encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();
    }

    private MockHttpServletRequestBuilder history(long customerId) {
        return get(URL).header(HttpHeaders.AUTHORIZATION, "Bearer " + token(secret, customerId));
    }

    private void notify(long customerId, NotificationType type, String description, Duration age) {
        repository.save(new NotificationLog(customerId, type, description, Instant.now().minus(age), "en"));
    }

    private void customer(long id) {
        jdbc.update("INSERT INTO customers (id, external_id, first_name, last_name, email) VALUES (?, ?, 'T', 'T', ?)",
                id, "ext-" + id, id + "@example.com");
    }

    @BeforeEach
    void setUp() {
        jdbc.update("DELETE FROM notification_log");
        jdbc.update("DELETE FROM customers WHERE id IN (?, ?)", ALICE, BOB);
        customer(ALICE);
        customer(BOB);
        notify(ALICE, NotificationType.LARGE_TRANSACTION, "Card payment CHF 2,500.00", Duration.ofHours(2));
        notify(ALICE, NotificationType.LOW_BALANCE, "Balance below CHF 100.00", Duration.ofDays(5));
        notify(ALICE, NotificationType.BILL_REMINDER, "Electricity bill due 30.10.", Duration.ofDays(30));
        notify(ALICE, NotificationType.LOW_BALANCE, "Expired alert", Duration.ofDays(120));
        notify(BOB, NotificationType.LARGE_TRANSACTION, "Bob's private transfer", Duration.ofHours(1));
    }

    @Test
    void us028Ac1_customerSeesOwnHistoryWithTypeDescriptionAndLocalizedTime() throws Exception {
        mvc.perform(history(ALICE).param("from", "security-center").param("lang", "en").param("tz", "Europe/Zurich"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(3)))
                .andExpect(jsonPath("$.items[0].type").value("LARGE_TRANSACTION"))
                .andExpect(jsonPath("$.items[0].typeLabel").value("Large transaction alert"))
                .andExpect(jsonPath("$.items[0].description").value("Card payment CHF 2,500.00"))
                .andExpect(jsonPath("$.items[0].occurredAtFormatted").value(containsString("CE")))
                .andExpect(jsonPath("$.entryPoint").value("security-center"));
    }

    @Test
    void us028Ac2_typesAreReturnedWithDistinctCategories() throws Exception {
        mvc.perform(history(ALICE))
                .andExpect(jsonPath("$.items[*].category").value(org.hamcrest.Matchers.contains("TRANSACTION", "BALANCE", "BILL")));
        mvc.perform(history(ALICE).param("category", "BALANCE"))
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].type").value("LOW_BALANCE"));
    }

    @Test
    void us028Ac3_expiredNotificationsAreNeitherShownNorKept() throws Exception {
        mvc.perform(history(ALICE))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.retentionDays").value(90))
                .andExpect(jsonPath("$.items[*].description", everyItem(not("Expired alert"))));

        assertThat(retentionJob.purgeExpired()).isEqualTo(1);
        assertThat(repository.count()).isEqualTo(4);
    }

    @Test
    void us028Ac4_customersOnlySeeTheirOwnNotifications() throws Exception {
        mvc.perform(history(BOB))
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].description").value("Bob's private transfer"));
        mvc.perform(history(ALICE).param("customerId", String.valueOf(BOB)))
                .andExpect(jsonPath("$.items[*].description", everyItem(not("Bob's private transfer"))));
    }

    @Test
    void us028Ac4_tokensNotSignedByTheBankAreRejected() throws Exception {
        String forged = token("an-attacker-controlled-secret-of-32-bytes!!", BOB);
        mvc.perform(get(URL).header(HttpHeaders.AUTHORIZATION, "Bearer " + forged)).andExpect(status().isUnauthorized());
        mvc.perform(get(URL)).andExpect(status().isUnauthorized());
    }

    @Test
    void us028Ac5_frenchLabelsAndParisTime() throws Exception {
        mvc.perform(history(ALICE).param("lang", "fr").param("tz", "Europe/Paris").param("category", "BILL"))
                .andExpect(jsonPath("$.locale").value("fr"))
                .andExpect(jsonPath("$.timeZone").value("Europe/Paris"))
                .andExpect(jsonPath("$.items[0].typeLabel").value("Rappel de facture"))
                .andExpect(jsonPath("$.summary").value(containsString("1")));
        mvc.perform(get(URL + "/labels").param("lang", "it"))
                .andExpect(jsonPath("$.labels['page.heading']").value("Cronologia delle notifiche"))
                .andExpect(jsonPath("$.entryPoints", hasSize(3)));
    }

    @Test
    void us028Ac6_screenIsServedWithSecurityHeaders() throws Exception {
        mvc.perform(get("/notification-history.html"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("<main")))
                .andExpect(header().string("Content-Security-Policy", containsString("script-src 'self'")));
    }
}
