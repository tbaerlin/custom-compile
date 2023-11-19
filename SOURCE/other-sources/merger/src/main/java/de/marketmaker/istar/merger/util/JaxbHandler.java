package de.marketmaker.istar.merger.util;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.transform.stream.StreamSource;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

import static javax.xml.XMLConstants.NULL_NS_URI;

/**
 * Created on 30.08.12 08:35
 * Copyright (c) market maker Software AG. All Rights Reserved.
 * @author Michael Lösch
 */

public class JaxbHandler {
    private final Marshaller marshaller;

    private final Unmarshaller unmarshaller;

    public JaxbHandler(String jaxbContextPath) {
        try {
            final JAXBContext jc = JaxbContextCache.INSTANCE.getContext(jaxbContextPath);
            this.unmarshaller = jc.createUnmarshaller();
            this.marshaller = jc.createMarshaller();
            this.marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
            this.marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> T unmarshal(String xml, Class<T> responseTypeClass) {
        return unmarshal(new StreamSource(new StringReader(xml)), responseTypeClass);
    }

    public  <T> T unmarshal(StreamSource streamSource, Class<T> responseTypeClass) {
        synchronized (this.unmarshaller) {
            try {
                return this.unmarshaller.unmarshal(streamSource, responseTypeClass).getValue();
            } catch (JAXBException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public <T> String marshal(Class<T> declaredType, T value, String elementName) {
        final JAXBElement<T> element = new JAXBElement<>(new QName(NULL_NS_URI, elementName),
                declaredType, value);
        final StringWriter writer = new StringWriter(128);
        synchronized (this.marshaller) {
            try {
                this.marshaller.marshal(element, writer);
            } catch (JAXBException e) {
                throw new RuntimeException(e);
            }
            return writer.toString();
        }
    }

    public <T> AbstractHttpMessageConverter<T> createMessageConverter(final Class<T> c) {
        return new AbstractHttpMessageConverter<T>(MediaType.parseMediaType("text/xml;charset=UTF-8")) {
            @Override
            protected boolean supports(Class<?> clazz) {
                return clazz == c;
            }

            @Override
            protected T readInternal(Class<? extends T> clazz,
                    HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
                return unmarshal(new StreamSource(inputMessage.getBody()), c);
            }

            @Override
            protected void writeInternal(T t,
                    HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean canWrite(Class<?> clazz, MediaType mediaType) {
                return false;
            }
        };
    }
}