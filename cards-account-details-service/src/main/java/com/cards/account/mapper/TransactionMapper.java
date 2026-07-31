package com.cards.account.mapper;

import com.cards.account.domain.Transaction;
import com.cards.account.dto.TransactionResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper from transaction entities to API response DTOs.
 * Copies the related account id into the response.
 */
@Mapper(componentModel = "spring")
public interface TransactionMapper {

    /**
     * Maps a transaction entity to the API response DTO.
     *
     * @param transaction persisted transaction entity
     * @return transaction response for API clients
     */
    @Mapping(target = "accountId", source = "account.id")
    TransactionResponse toResponse(Transaction transaction);
}
