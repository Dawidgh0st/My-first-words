package pl.kasprzak.dawid.myfirstwords.model.children;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Builder
public class CreateChildResponse {

    private String name;
    private LocalDate birthDate;
    private Gender gender;
}
