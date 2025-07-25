package phenriqued.BudgetMaster.DTOs.Security.Token;

public record TokenSignInDTO(
        String token,
        String refreshToken,
        String twoFactorAuthenticationMessage,
        String securityUserToken2fa) {
    public TokenSignInDTO(TokenDTO tokens){
        this(tokens.token(), tokens.refreshToken(), null, null);
    }
}
