package com.bank.core.notification.dto;

import java.util.List;
import java.util.Map;

public record NotificationHistoryLabelsResponse(
        String locale,
        Map<String, String> labels,
        List<EntryPointLink> entryPoints) {

    public record EntryPointLink(String entryPoint, String href, String label) {
    }
}
