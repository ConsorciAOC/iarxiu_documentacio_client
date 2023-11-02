package net.catcert.iarxiu.client.test;

import java.io.File;
import java.io.InputStream;

import net.catcert.iarxiu.client.proxy.ProxyClient;
import net.catcert.iarxiu.client.test.utils.Utils;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.hp.iarxiu.core.schemas.x20.dissemination.GetZipPackageRequestDocument;
import com.hp.iarxiu.core.schemas.x20.dissemination.GetZipPackageResponseDocument;
import com.hp.iarxiu.core.schemas.x20.dissemination.GetZipPackageRequestDocument.GetZipPackageRequest;
import com.hp.iarxiu.core.schemas.x20.dissemination.GetZipPackageResponseDocument.GetZipPackageResponse;


/**
 * Classe d'exemple de petició de recuperació d'un paquet a iArxiu. El format en què es recupera és un fitxer comprimit zip.
 */
public class GetZipPackageRequestTest {

	public static void main(String[] args) {
		try {
			String fileSeparator = File.separator;
			String userDir = System.getProperty("user.dir").replace("/", fileSeparator);
			String pathOutDir = userDir + "/out/samples/".replace("/", fileSeparator);
			Utils.createIfNotExistsDirectory(pathOutDir);

			// Càrrega de l'application context
			ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
			
			// Client
			ProxyClient proxy = (ProxyClient) context.getBean("proxyClient");
			String downloadUrl = (String) context.getBean("downloadByTicketUrl");
			
			// Petició de recuperació de paquet dins un fitxer zip
			GetZipPackageRequestDocument requestDocument = GetZipPackageRequestDocument.Factory.newInstance();
			GetZipPackageRequest request = requestDocument.addNewGetZipPackageRequest();
			
			// Identificador del paquet
			request.setPackageId("catcert:m02396:20230117-134638756:8169");
			
			// Incloure metadades d'acord a l'esquema Dublin Core
			request.setIncludeDC(true);

			Utils.printXmlObject(requestDocument);
			
			// Guardem la petició en un fitxer
			requestDocument.save(new File(pathOutDir + "GetZipPackageRequest.xml"));

			// Enviament de la petició
			GetZipPackageResponseDocument responseDocument = (GetZipPackageResponseDocument)proxy.send(requestDocument);
			
			// Pintem la resposta
			Utils.printXmlObject(responseDocument);
			
			// Guardem la resposta en un fitxer
			responseDocument.save(new File(pathOutDir + "GetZipPackageResponse.xml"));
		
			// Desem les dades binàries en un fitxer
			GetZipPackageResponse response = responseDocument.getGetZipPackageResponse(); 
			String binToken = response.getPackageDownloadTicket();
			InputStream binData = Utils.downloadByTicketAsInputStream(binToken, downloadUrl);
			Utils.writeToFile(pathOutDir + "ZipPackage.pdf", binData);

		}catch(Exception e){
			System.out.println(e.getMessage());
		}
		
	}
	
}
