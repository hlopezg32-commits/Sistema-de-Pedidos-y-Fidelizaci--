/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.mvcproyecto;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 *
 * @author PC
 */
@SpringBootApplication(scanBasePackages = "com.mycompany.mvcproyecto") 

public class MVCPROYECTO {

    public static void main(String[] args) {
        System.setProperty("server.port", "8081");
        SpringApplication.run(MVCPROYECTO.class, args);
    }
}
