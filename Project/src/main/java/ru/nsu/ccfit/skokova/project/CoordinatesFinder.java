package ru.nsu.ccfit.skokova.project;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.nsu.ccfit.skokova.project.generated.Node;

import java.util.List;

@Component
public class CoordinatesFinder {
    @Autowired
    private OsmDao osmDao;

    public List<Node> findNodes(double lat, double lon, int distance) {
        return osmDao.getNearNodes(lat, lon, distance);
    }
}
