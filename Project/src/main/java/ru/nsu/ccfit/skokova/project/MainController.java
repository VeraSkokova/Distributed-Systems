package ru.nsu.ccfit.skokova.project;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import ru.nsu.ccfit.skokova.project.generated.Node;

import java.io.File;
import java.util.List;

@RestController
public class MainController {
    private static final Logger logger = LogManager.getLogger(MainController.class);

    private CoordinatesFinder coordinatesFinder;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public MainController(OsmReader osmReader, CoordinatesFinder coordinatesFinder) {
        //osmReader.parseNodes("RU-NVS.osm");
        this.coordinatesFinder = coordinatesFinder;
    }

    @RequestMapping("/")
    public String greeting() {
        return "Hello, world!";
    }

    @RequestMapping(name = "/getCoordinates", params = {"lat", "lon", "distance"})
    public @ResponseBody
    String coordinates(@RequestParam(value = "lat") double lat, @RequestParam(value = "lon") double lon, @RequestParam(value = "distance") int distance) {
        try {
            List<Node> nodes = coordinatesFinder.findNodes(lat, lon, distance);
            try {
                return objectMapper.writeValueAsString(nodes);
            } catch (OutOfMemoryError e) {
                File file = new File("result");
                objectMapper.writeValue(file, nodes);
                return "Ready";
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        }

        return "Oops";
    }
}
