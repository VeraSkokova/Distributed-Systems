package ru.nsu.ccfit.skokova.project;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MainController {
    @Autowired
    public MainController(OsmReader osmReader) {
        osmReader.parseNodes("RU-NVS.osm");
    }

    @RequestMapping("/")
    public String greeting() {
        return "Hello, world!";
    }
}
