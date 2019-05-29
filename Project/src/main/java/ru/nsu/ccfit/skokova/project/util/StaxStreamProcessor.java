package ru.nsu.ccfit.skokova.project.util;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.stream.StreamSource;
import java.io.BufferedReader;
import java.io.InputStream;

public class StaxStreamProcessor implements AutoCloseable {
    private static final XMLInputFactory FACTORY = XMLInputFactory.newInstance();

    private final XMLStreamReader reader;

    public StaxStreamProcessor(BufferedReader bufferedReader) throws XMLStreamException {
        reader = FACTORY.createXMLStreamReader(bufferedReader);
    }

    public StaxStreamProcessor(InputStream is) throws XMLStreamException {
        reader = FACTORY.createXMLStreamReader(is);
    }

    public StaxStreamProcessor(String fileName) throws XMLStreamException {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(fileName);
        StreamSource source = new StreamSource(inputStream);
        reader = FACTORY.createXMLStreamReader(source);
    }

    public XMLStreamReader getReader() {
        return reader;
    }

    @Override
    public void close() {
        if (reader != null) {
            try {
                reader.close();
            } catch (XMLStreamException e) { // empty
            }
        }
    }
}
