/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.pti.service.util;

import jakarta.xml.bind.*;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author cda
 */
public final class FastXml {

    private FastXml() {
    }

    /**
     * Cache des JAXBContext par type
     */
    private static final Map<Class<?>, JAXBContext> CTX = new ConcurrentHashMap<>();

    /**
     * Unmarshaller par type, stocké en ThreadLocal
     */
    private static final Map<Class<?>, ThreadLocal<Unmarshaller>> UNM = new ConcurrentHashMap<>();

    /**
     * Marshaller par type, stocké en ThreadLocal
     */
    private static final Map<Class<?>, ThreadLocal<Marshaller>> MAR = new ConcurrentHashMap<>();

    /**
     * StAX factory sécurisée (XXE OFF)
     */
    private static final XMLInputFactory SAFE_XIF = secureXmlInputFactory();

    private static XMLInputFactory secureXmlInputFactory() {
        XMLInputFactory xif = XMLInputFactory.newFactory();
        try {
            // Empêcher les entités externes / DTD
            xif.setProperty(XMLInputFactory.SUPPORT_DTD, false);
        } catch (Exception ignored) {
        }
        try {
            xif.setProperty("javax.xml.stream.isSupportingExternalEntities", false);
        } catch (Exception ignored) {
        }
        return xif;
    }

    private static JAXBContext contextFor(Class<?> type) {
        try {
            return CTX.computeIfAbsent(type, t -> {
                try {
                    return JAXBContext.newInstance(t);
                } catch (JAXBException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (RuntimeException re) {
            Throwable c = re.getCause();
            if (c instanceof JAXBException) {
                throw new IllegalStateException("JAXB init failed for " + type, c);
            }
            throw re;
        }
    }

    private static Unmarshaller unmarshallerFor(Class<?> type) {
        return UNM.computeIfAbsent(type, t -> ThreadLocal.withInitial(() -> {
            try {
                return contextFor(t).createUnmarshaller();
            } catch (JAXBException e) {
                throw new RuntimeException(e);
            }
        })).get();
    }

    private static Marshaller marshallerFor(Class<?> type) {
        return MAR.computeIfAbsent(type, t -> ThreadLocal.withInitial(() -> {
            try {
                Marshaller m = contextFor(t).createMarshaller();
                m.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
                m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.FALSE);
                return m;
            } catch (JAXBException e) {
                throw new RuntimeException(e);
            }
        })).get();
    }

    /* =======================
       Deserialization (XML -> Object)
       ======================= */
    public static <T> T fromXml(String xml, Class<T> type) {
        if (xml == null) {
            return null;
        }
        try (Reader r = new StringReader(xml)) {
            XMLStreamReader xsr = SAFE_XIF.createXMLStreamReader(r);
            Object o = unmarshallerFor(type).unmarshal(xsr);
            return type.cast(o);
        } catch (Exception e) {
            throw new IllegalArgumentException("XML parse failed for " + type.getSimpleName(), e);
        }
    }

    public static <T> T fromXml(byte[] xml, Class<T> type) {
        if (xml == null) {
            return null;
        }
        try (InputStream is = new ByteArrayInputStream(xml);
                Reader r = new InputStreamReader(is, StandardCharsets.UTF_8)) {
            XMLStreamReader xsr = SAFE_XIF.createXMLStreamReader(r);
            Object o = unmarshallerFor(type).unmarshal(xsr);
            return type.cast(o);
        } catch (Exception e) {
            throw new IllegalArgumentException("XML parse failed for " + type.getSimpleName(), e);
        }
    }

    public static <T> T fromXml(InputStream is, Class<T> type) {
        if (is == null) {
            return null;
        }
        try (Reader r = new InputStreamReader(is, StandardCharsets.UTF_8)) {
            XMLStreamReader xsr = SAFE_XIF.createXMLStreamReader(r);
            Object o = unmarshallerFor(type).unmarshal(xsr);
            return type.cast(o);
        } catch (Exception e) {
            throw new IllegalArgumentException("XML parse failed for " + type.getSimpleName(), e);
        }
    }

    /* =======================
       Serialization (Object -> XML)
       ======================= */
    public static String toXml(Object obj) {
        if (obj == null) {
            return null;
        }
        try (StringWriter sw = new StringWriter(256)) {
            marshallerFor(obj.getClass()).marshal(obj, sw);
            return sw.toString();
        } catch (Exception e) {
            throw new IllegalArgumentException("XML write failed for " + obj.getClass().getSimpleName(), e);
        }
    }

    public static byte[] toXmlBytes(Object obj) {
        String xml = toXml(obj);
        return xml != null ? xml.getBytes(StandardCharsets.UTF_8) : null;
    }
}
