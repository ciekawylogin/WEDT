package wedt.utils.model;

import java.io.Serializable;

public enum TokenType implements Serializable {
    STREET_NAME,
    CITY,
    MUNICIPALITY,
    MUNICIPALITY_INDICATOR,
    VOIVODESHIP,
    VOIVODESHIP_INDICATOR,
    COMPANY_TYPE,
    STREET_INDICATOR,
    ACCOUNT_NO,
    NIP_INDICATOR,
    NIP,
    INTEGER,
    PERCENT,
    POSTAL_CODE,
    DECIMAL,
    NUMBER_SEQ,
    COUNTRY_CODE,
    CURRENCY,
    BEGIN_ADDRESS,
    END_ADDRESS,
    UNKNOWN,
    NUMBER,
    DATE,
    FIRST_NAME,
    LAST_NAME;
}
