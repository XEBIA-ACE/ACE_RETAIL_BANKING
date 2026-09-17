package com.bank.core.config;

import com.bank.core.account.security.AccountOwnershipChecker;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
public class WebSocketSecurityConfig implements ChannelInterceptor {

    private static final Pattern BALANCE_TOPIC =
            Pattern.compile("^/topic/accounts/([^/]+)/balance$");

    private final JwtDecoder jwtDecoder;
    private final AccountOwnershipChecker ownershipChecker;
    private final ConcurrentMap<String, Authentication> sessionUsers = new ConcurrentHashMap<>();

    public WebSocketSecurityConfig(JwtDecoder jwtDecoder, AccountOwnershipChecker ownershipChecker) {
        this.jwtDecoder = jwtDecoder;
        this.ownershipChecker = ownershipChecker;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            Authentication authentication = readAuthentication(accessor);
            accessor.setUser(authentication);
            if (accessor.getSessionId() != null) {
                sessionUsers.put(accessor.getSessionId(), authentication);
            }
        } else if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            Authentication authentication = accessor.getUser() instanceof Authentication value
                    ? value : sessionUsers.get(accessor.getSessionId());
            Matcher matcher = BALANCE_TOPIC.matcher(accessor.getDestination() == null
                    ? "" : accessor.getDestination());
            if (authentication == null || !matcher.matches()
                    || !ownershipChecker.check(matcher.group(1), authentication)) {
                throw new AccessDeniedException("Subscription is not permitted");
            }
        } else if (StompCommand.DISCONNECT.equals(accessor.getCommand())
                && accessor.getSessionId() != null) {
            sessionUsers.remove(accessor.getSessionId());
        }
        return message;
    }

    private Authentication readAuthentication(StompHeaderAccessor accessor) {
        String header = accessor.getFirstNativeHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            throw new MessageDeliveryException("Missing STOMP Authorization header");
        }
        try {
            Jwt jwt = jwtDecoder.decode(header.substring("Bearer ".length()));
            return new JwtAuthenticationToken(jwt);
        } catch (Exception exception) {
            throw new MessageDeliveryException("Invalid STOMP bearer token");
        }
    }
}
