package pl.kmolski.hydrohomie.account.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@NotBlank(message = "The password cannot be blank")
@Size(min = 10, message = "The password must be at least 10 characters long")
public record PlaintextPassword(String plaintext) {}
