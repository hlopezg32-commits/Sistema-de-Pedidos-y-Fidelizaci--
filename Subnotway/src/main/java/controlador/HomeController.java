package controlador;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping({"/", "/inicio"})
    public String inicio() {
        return "inicio";
    }

    @GetMapping({"/movil", "/app"})
    public String appMovil() {
        return "movil";
    }
}
