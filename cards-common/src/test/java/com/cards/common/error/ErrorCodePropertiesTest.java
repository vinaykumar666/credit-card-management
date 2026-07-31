package com.cards.common.error;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ErrorCodePropertiesTest {

    @Test
    void findsKnownCodeAndFallsBackForUnknown() {
        ErrorCodeProperties properties = new ErrorCodeProperties();
        Map<String, ErrorCodeDefinition> codes = new LinkedHashMap<>();
        codes.put(ErrorCodes.PAY_001, new ErrorCodeDefinition(404, "Payment not found"));
        properties.setCodes(codes);

        assertTrue(properties.find(ErrorCodes.PAY_001).isPresent());
        assertEquals(404, properties.require(ErrorCodes.PAY_001).httpStatus());
        assertEquals(500, properties.require("UNKNOWN_CODE").httpStatus());
    }
}
