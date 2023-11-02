package net.catcert.iarxiu.client.test;

import java.io.File;

import net.catcert.iarxiu.client.proxy.ProxyClient;
import net.catcert.iarxiu.client.test.utils.Utils;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.hp.iarxiu.core.schemas.x20.dissemination.GetEvidenceReportRequestDocument;
import com.hp.iarxiu.core.schemas.x20.dissemination.GetEvidenceReportResponseDocument;
import com.hp.iarxiu.core.schemas.x20.dissemination.GetEvidenceReportRequestDocument.GetEvidenceReportRequest;
import com.hp.iarxiu.core.schemas.x20.dissemination.GetEvidenceReportResponseDocument.GetEvidenceReportResponse;


/**
 * Classe d'exemple de sol·licitud de l'informe d'evidències d'un paquet a iArxiu.
 */
public class GetEvidenceReportRequestTest {

	public static void main(String[] args) throws Exception {
		try {
			String fileSeparator = File.separator;
			String userDir = System.getProperty("user.dir").replace("/", fileSeparator);
			String pathOutDir = userDir + "/out/samples/".replace("/", fileSeparator);
			Utils.createIfNotExistsDirectory(pathOutDir);

			// Càrrega de l'application context
			ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
	
			// Client
			ProxyClient proxy = (ProxyClient) context.getBean("proxyClient");
			
			// Petició de generació de l'informe d'evidències
			GetEvidenceReportRequestDocument requestDocument = GetEvidenceReportRequestDocument.Factory.newInstance();
			GetEvidenceReportRequest request = requestDocument.addNewGetEvidenceReportRequest();
			
			// Identificador del paquet
			request.setPackageId("catcert:m02396:20230117-134638756:8169");
			Utils.printXmlObject(requestDocument);
			
			// Guardem la petició en un fitxer
			requestDocument.save(new File(pathOutDir + "GetEvidenceReportRequest.xml"));
			
			// Enviament de la petició
			GetEvidenceReportResponseDocument responseDocument = (GetEvidenceReportResponseDocument)proxy.send(requestDocument);
			Utils.printXmlObject(responseDocument);
			
			// Guardem la resposta en un fitxer
			responseDocument.save(new File(pathOutDir + "GetEvidenceReportResponse.xml"));
			
			// desem les dades binàries de l'informe en un fitxer PDF
			GetEvidenceReportResponse response = responseDocument.getGetEvidenceReportResponse();
			byte[] binData = response.getReport();
			Utils.writeToFile(pathOutDir + "EvidenceReport.pdf", binData);
			
		}catch(Exception e){
			System.out.println(e.getMessage());
		}
		
	}
	
}
