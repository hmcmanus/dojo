package com.mcmanus.samples.xml;

import org.junit.Test;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class JaxbTest {

    @Test
    public void shouldMarshallSandwich() throws Throwable {
        Sandwich testSandwich = new Sandwich();
        testSandwich.setName("testSandwich");
        List<Filling> fillings = new ArrayList<>();
        Filling cheeseFilling = new Filling();
        cheeseFilling.setName("cheese");
        fillings.add(cheeseFilling);

        testSandwich.setFillings(fillings);

        File testFile = new File("/tmp/temp.xml");

        JAXBContext context = JAXBContext.newInstance(Sandwich.class);
        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.marshal(testSandwich, testFile);
    }

    @Test
    public void shouldUnMarshallSandwich() throws Throwable {
        Sandwich testSandwich = new Sandwich();
        testSandwich.setName("testSandwich");
        List<Filling> fillings = new ArrayList<>();
        Filling cheeseFilling = new Filling();
        cheeseFilling.setName("ham");
        fillings.add(cheeseFilling);
        testSandwich.setFillings(fillings);

        File testFile = new File(Thread.currentThread().getContextClassLoader().getResource("readMeIn.xml").toURI());

        JAXBContext context = JAXBContext.newInstance(Sandwich.class);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        Sandwich unmarshalledSandwich = (Sandwich)unmarshaller.unmarshal(testFile);

        assertEquals(testSandwich.getName(), unmarshalledSandwich.getName());
        assertEquals(testSandwich.getFillings().get(0).getName(), unmarshalledSandwich.getFillings().get(0).getName());
    }
}
