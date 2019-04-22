package ru.nsu.ccfit.skokova.project;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.nsu.ccfit.skokova.project.generated.Bounds;
import ru.nsu.ccfit.skokova.project.generated.Node;
import ru.nsu.ccfit.skokova.project.generated.Osm;
import ru.nsu.ccfit.skokova.project.util.StaxStreamProcessor;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;
import javax.xml.stream.util.StreamReaderDelegate;
import java.io.IOException;
import java.io.InputStream;

@Component
public class OsmReader {
    private static final Logger logger = LogManager.getLogger(OsmReader.class);

    public static final String NODE = "node";
    public static final String OSM = "osm";
    public static final String BOUNDS = "bounds";

    @Autowired
    private OsmDao osmDao;

    public void parseNodes(String fileName) {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(fileName)) {
            StaxStreamProcessor streamProcessor = new StaxStreamProcessor(inputStream);
            XMLStreamReader reader = streamProcessor.getReader();
            XMLStreamReader streamReader = new OsmTypeReader(reader);

            JAXBContext jaxbContext = JAXBContext.newInstance(Node.class.getPackage().getName());

            while (streamReader.hasNext() && !streamReader.isStartElement()) {
                streamReader.next();
            }

            try {
                while (streamReader.hasNext()) {
                    int event = streamReader.next();
                    if (event == XMLEvent.START_ELEMENT) {

                        if (NODE.equals(streamReader.getLocalName())) {
                            Node node = parseNode(jaxbContext, streamReader);
                            osmDao.saveNode(node);
                            if (!node.getTag().isEmpty()) {
                                osmDao.saveTags(node, node.getTag());
                            }
                        }
                    }
                }
            } catch (OutOfMemoryError e) {
                logger.warn("Out of memory :(");
                return;
            }

            logger.info("Parsing finished");
        } catch (IOException | XMLStreamException | JAXBException e) {
            logger.error("An error occurred while parsing nodes", e);
        }
    }

    private Bounds parseBounds(Unmarshaller unmarshaller, XMLStreamReader streamReader) throws JAXBException {
        Bounds bounds = (Bounds) unmarshaller.unmarshal(streamReader);
        return bounds;
    }

    private Osm parseOsm(Unmarshaller unmarshaller, XMLStreamReader streamReader) throws JAXBException {
        Osm osm = (Osm) unmarshaller.unmarshal(streamReader);
        return osm;
    }

    private Node parseNode(JAXBContext context, XMLStreamReader streamReader) throws JAXBException {
        Unmarshaller unmarshaller = context.createUnmarshaller();
        Node node = (Node) unmarshaller.unmarshal(streamReader);
        return node;
    }

    private static class OsmTypeReader extends StreamReaderDelegate {

        public OsmTypeReader(XMLStreamReader reader) {
            super(reader);
        }


        public OsmTypeReader() {
            super();
        }

        @Override
        public String getAttributeNamespace(int arg0) {
            if ("version".equalsIgnoreCase(getAttributeLocalName(arg0)) ||
                    "generator".equalsIgnoreCase(getAttributeLocalName(arg0)) ||
                    NODE.equalsIgnoreCase(getAttributeLocalName(arg0)) ||
                    "tag".equalsIgnoreCase(getAttributeLocalName(arg0)) ||
                    BOUNDS.equalsIgnoreCase(getAttributeLocalName(arg0))) {
                return "http://openstreetmap.org/osm/0.6";
            }
            return super.getAttributeNamespace(arg0);
        }

        @Override
        public String getNamespaceURI() {
            return "http://openstreetmap.org/osm/0.6"; //tut
        }
    }
}
