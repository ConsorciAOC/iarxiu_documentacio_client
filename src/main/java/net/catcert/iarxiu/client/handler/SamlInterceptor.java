package net.catcert.iarxiu.client.handler;

import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;

import org.springframework.ws.client.support.interceptor.ClientInterceptor;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.client.WebServiceClientException;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.saaj.SaajSoapHeaderException;
import org.springframework.oxm.xmlbeans.XmlBeansMarshaller;
import org.springframework.ws.soap.SoapHeaderElement;

import org.w3c.dom.Node;
import org.w3c.dom.Text;

import x0Assertion.oasisNamesTcSAML2.AssertionDocument;
import x0Assertion.oasisNamesTcSAML2.AssertionType;
import x0Assertion.oasisNamesTcSAML2.AttributeStatementType;
import x0Assertion.oasisNamesTcSAML2.AttributeType;
import x0Assertion.oasisNamesTcSAML2.NameIDType;
import x0Assertion.oasisNamesTcSAML2.SubjectConfirmationType;
import x0Assertion.oasisNamesTcSAML2.SubjectType;

public class SamlInterceptor implements ClientInterceptor {

	public boolean handleFault(MessageContext arg0) throws WebServiceClientException {
		return false;
	}

	public boolean handleRequest(MessageContext messageContext) throws WebServiceClientException {
		
		QName qn = new QName("http://soap.iarxiu/headers","Context","ish");
		
		SoapMessage soapMessage = ((SoapMessage)messageContext.getRequest());
		
		SoapHeaderElement wsh = soapMessage.getSoapHeader().addHeaderElement(qn);
		wsh.setMustUnderstand(true);
		
		AssertionDocument assertion = createAssertion();

		Map<String,String> ns = new HashMap<>();
		ns.put("urn:oasis:names:tc:SAML:2.0:assertion", "saml2");
		
		XmlOptions xmlOptions = new XmlOptions();
		xmlOptions.setSaveSuggestedPrefixes(ns);
		xmlOptions.setSaveAggressiveNamespaces();
		
		XmlBeansMarshaller marshaller = new XmlBeansMarshaller();
		marshaller.setXmlOptions(xmlOptions);
		
		try {
			marshaller.marshal(assertion, wsh.getResult());
		} catch(IOException e) {
			throw new SaajSoapHeaderException("Error creating SAML Header", e);
		}

		return true;
	}

	public boolean handleResponse(MessageContext messageContext) throws WebServiceClientException {
		return false;
	}
	
	protected AssertionDocument createAssertion() {
		AssertionDocument assertionDocument = AssertionDocument.Factory.newInstance();
		AssertionType assertion;
		
		assertion = assertionDocument.addNewAssertion();
		
		assertion.setVersion("2.0");
		assertion.setID("AssertId-" + System.currentTimeMillis());
		assertion.setIssueInstant( Calendar.getInstance() );
		
		NameIDType issuerName = assertion.addNewIssuer();
		issuerName.setStringValue("iArxiuClient");
		
		SubjectType subject = assertion.addNewSubject();
		
		SubjectConfirmationType subjectConfirmation = subject.addNewSubjectConfirmation();
		subjectConfirmation.setMethod("urn:oasis:names:tc:SAML:2.0:cm:sender-vouches");
		
		NameIDType subjectName = subjectConfirmation.addNewNameID();
		subjectName.setStringValue("abarbeta");
		
		AttributeStatementType attributeStatement = assertion.addNewAttributeStatement();
		
		addAttribute(attributeStatement, "urn:iarxiu:2.0:names:organizationAlias", "organizationTest");
		addAttribute(attributeStatement, "urn:iarxiu:2.0:names:fondsAlias", "fondsTest");
		addAttribute(attributeStatement, "urn:iarxiu:2.0:names:member-of", "archivists");
		
		return assertionDocument;
	}
	
	private void addAttribute(
			final AttributeStatementType attributeStatement,
			final String name,
			final String value ) {
	
		AttributeType attribute = attributeStatement.addNewAttribute();
		attribute.setName(name);
		XmlObject xmlNode = attribute.addNewAttributeValue();
		Node node = xmlNode.getDomNode();
		Text text = node.getOwnerDocument().createTextNode(value);
		node.appendChild(text);
	}
	
}
