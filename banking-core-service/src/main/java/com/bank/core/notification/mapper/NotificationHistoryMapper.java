package com.bank.core.notification.mapper;

import com.bank.core.notification.domain.NotificationHistoryRecord;
import com.bank.core.notification.dto.NotificationHistoryItemDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface NotificationHistoryMapper {

    @Mapping(target = "notificationId", source = "externalId")
    @Mapping(target = "customerId", source = "customerExternalId")
    NotificationHistoryItemDto toDto(NotificationHistoryRecord record);
}
