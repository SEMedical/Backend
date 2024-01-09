package edu.tongji.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class RegisterDTO {
    private String name;
    private String password;
    private String contact;
    private String gender;
    private Integer age;
    @Override
    public String toString(){
        String str="The user's name is "+name
                +"\nThe user's password is "+password
                +"\nThe user's contact is"+contact+
                "\nThe gender is "+gender+
                "\nThe age is "+age;
        return str;
    }
}
