package wedt.utils.model;

import lombok.Value;

import java.io.Serializable;

@Value
public class TokenLine implements Serializable {
    private TokenType tokenType;
    private String value;
}
