package ch.epfl.javelo.routing;
import ch.epfl.javelo.projection.PointCh;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

/**
 * class that manages to creat and write all the document in gpx format.
 * @author Ilyas Hawazine (326815).
 */
public final class GpxGenerator {

    /**
     * returns a document in gpx format made of route and elevationProfile.
     * @param route the route from which the document will be made.
     * @param elevationProfile will go over all the points of
     *                         the route to get the profiles.
     * @return the documents in gpx format.
     */
    public static Document createGpx(Route route, ElevationProfile elevationProfile) {

        Document doc;
        try {
            doc = DocumentBuilderFactory.newDefaultInstance().newDocumentBuilder().newDocument();
        }catch (ParserConfigurationException e){
            throw new Error(e);
        }
        Element root = doc.createElementNS("http://www.topografix.com/GPX/1/1",
                        "gpx");
        doc.appendChild(root);

        root.setAttributeNS(
                "http://www.w3.org/2001/XMLSchema-instance",
                "xsi:schemaLocation",
                "http://www.topografix.com/GPX/1/1 "
                        + "http://www.topografix.com/GPX/1/1/gpx.xsd");
        root.setAttribute("version", "1.1");
        root.setAttribute("creator", "JaVelo");

        //assigning the different paths for the gpx file.
        Element metadata = doc.createElement("metadata");
        root.appendChild(metadata);

        Element name = doc.createElement("name");
        metadata.appendChild(name);
        name.setTextContent("Route JaVelo");

        Element rte = doc.createElement("rte");
        root.appendChild(rte);

        double position;
        for(PointCh actualPoint : route.points()){
            //setting the attributes over all the points
            Element routePoint = doc.createElement("rtept");
            routePoint.setAttribute("lon", String.format("value is%.5f", Math.toDegrees(actualPoint.lon())));
            routePoint.setAttribute("lat", String.format("value is%.5f", Math.toDegrees(actualPoint.lat())));
            rte.appendChild(routePoint);

            //setting the element  for elevation.
            position = actualPoint.distanceTo(route.points().get(0));
            Element ele = doc.createElement("ele");
            ele.setTextContent(String.valueOf(elevationProfile.elevationAt(position)));
            rte.appendChild(ele);
        }
        return doc;
    }

    /**
     * writes a document in gpx formats with the given attributes.
     * @param fileName the name of the file.
     * @param route the route from which the gpx format will be made.
     * @param elevationProfile will go over all the points of
     *      *                  the route to get the profiles.
     * @throws IOException if there is a problem writing the document.
     * @throws TransformerException if there is a problem writing the document.
     */
    public static void writeGpx(String fileName, Route route, ElevationProfile elevationProfile)
            throws TransformerException, IOException {
        Document doc = createGpx(route, elevationProfile);
        Transformer transformer;
        Writer writer;
        //setting up all the written files and assigning
        //the properties by transform.
        try {
            writer = new FileWriter(fileName);
            transformer = TransformerFactory
                    .newDefaultInstance()
                    .newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.transform(new DOMSource(doc),
                    new StreamResult(writer));
            //checking all the possible errors a the end.
        }catch(TransformerException e) {
            e.printStackTrace();
        }
    }
}
