package net.catcert.iarxiu.client.proxy;

import org.springframework.oxm.xmlbeans.XmlBeansMarshaller;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;

import java.io.File;


public class ProxyClient extends WebServiceGatewaySupport {

	public ProxyClient() {
		String fileSeparator = File.separator;
		String userDir = System.getProperty("user.dir").replace("/", fileSeparator);
		String trustStore = userDir + ("/src/main/resources/stores/preproduccio/truststore/truststore.jks").replace("/", fileSeparator);

		System.setProperty("javax.net.ssl.trustStore", trustStore);
		System.setProperty("javax.net.ssl.trustStoreType", "JKS");
		System.setProperty("javax.net.ssl.trustStorePassword", "111111");

		XmlBeansMarshaller xmlBeansMarshaller = new XmlBeansMarshaller();
		setMarshaller(xmlBeansMarshaller);
		setUnmarshaller(xmlBeansMarshaller);
	}

	public Object send(Object request) {
		return getWebServiceTemplate().marshalSendAndReceive(request);
	}
	
}
