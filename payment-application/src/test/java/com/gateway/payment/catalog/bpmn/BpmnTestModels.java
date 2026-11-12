package com.gateway.payment.catalog.bpmn;

import org.flowable.bpmn.converter.BpmnXMLConverter;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.Process;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.io.StringReader;

final class BpmnTestModels {

    private BpmnTestModels() {
    }

    static Process parse(String processBody) {
        String xml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"
                             xmlns:flowable="http://flowable.org/bpmn"
                             xmlns:pay="http://payment-gateway/schema/bpmn/pay"
                             targetNamespace="http://payment-gateway/test">
                  <process id="testProcess" isExecutable="true">
                    <startEvent id="start"/>
                %s
                  </process>
                </definitions>
                """.formatted(processBody);
        try {
            XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(new StringReader(xml));
            BpmnModel model = new BpmnXMLConverter().convertToBpmnModel(reader);
            return model.getProcessById("testProcess");
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
