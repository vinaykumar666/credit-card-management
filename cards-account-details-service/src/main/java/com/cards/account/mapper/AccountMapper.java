package com.cards.account.mapper;

import com.cards.account.domain.Account;
import com.cards.account.dto.AccountResponse;
import com.cards.account.dto.CreateAccountRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper between account entities and API DTOs.
 * Converts create requests into new entities with ACTIVE status and available credit equal to the limit.
 */
@Mapper(componentModel = "spring")
public interface AccountMapper {

    /**
     * Maps an account entity to the API response DTO.
     *
     * @param account persisted account entity
     * @return account response for API clients
     */
    AccountResponse toResponse(Account account);

    /**
     * Maps a create request to a new account entity.
     * Ignores id and timestamps; sets available credit from the credit limit and status to ACTIVE.
     *
     * @param request validated create-account request
     * @return new account entity ready to save
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "availableCredit", source = "creditLimit")
    @Mapping(target = "status", constant = "ACTIVE")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Account toEntity(CreateAccountRequest request);
}
