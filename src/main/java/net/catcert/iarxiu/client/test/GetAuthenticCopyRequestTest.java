package net.catcert.iarxiu.client.test;

import java.io.File;
import java.io.InputStream;

import net.catcert.iarxiu.client.proxy.ProxyClient;
import net.catcert.iarxiu.client.test.utils.Utils;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.hp.iarxiu.core.schemas.x20.dissemination.GetAuthenticCopyRequestDocument;
import com.hp.iarxiu.core.schemas.x20.dissemination.GetAuthenticCopyResponseDocument;
import com.hp.iarxiu.core.schemas.x20.dissemination.GetAuthenticCopyRequestDocument.GetAuthenticCopyRequest;
import com.hp.iarxiu.core.schemas.x20.dissemination.GetAuthenticCopyResponseDocument.GetAuthenticCopyResponse;


/**
 * Classe d'exemple de petició de generació d'una còpia autèntica 
 * d'un determinat binari d'un paquet a iArxiu.
 * 
 * En cas que intentem accedir a un paquet sobre el qual no tenim permisos 
 * ens ho indicarà.
 */
public class GetAuthenticCopyRequestTest {

	public static void main(String[] args) throws Exception {
		try {
			String fileSeparator = File.separator;
			String userDir = System.getProperty("user.dir").replace("/", fileSeparator);
			String pathOutDir = userDir + "/out/samples/".replace("/", fileSeparator);
			Utils.createIfNotExistsDirectory(pathOutDir);

			ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
			ProxyClient proxy = (ProxyClient) context.getBean("proxyClient");
			String downloadUrl = (String) context.getBean("downloadByTicketUrl");
			
			// Petició de còpia autèntica de binaris
			GetAuthenticCopyRequestDocument requestDocument = GetAuthenticCopyRequestDocument.Factory.newInstance();
			GetAuthenticCopyRequest request = requestDocument.addNewGetAuthenticCopyRequest();

			request.setPackageId("catcert:m02396:20230824-142252845:6990");
			request.setBinaryId("BIN_1.0");

			Utils.printXmlObject(requestDocument);
			requestDocument.save(new File(pathOutDir + "GetAuthenticCopyRequest.xml"));

			GetAuthenticCopyResponseDocument responseDocument = (GetAuthenticCopyResponseDocument)proxy.send(requestDocument);
			Utils.printXmlObject(responseDocument);
			responseDocument.save(new File(pathOutDir + "GetAuthenticCopyResponse.xml"));
			
			// Desem les dades binàries en un fitxer
			GetAuthenticCopyResponse response = responseDocument.getGetAuthenticCopyResponse();
			String binToken = response.getContentDownloadTicket();
			InputStream binData = Utils.downloadByTicketAsInputStream(binToken, downloadUrl);

			Utils.writeToFile(pathOutDir + "authenticCopy.pdf", binData);
		
		} catch(Exception e){
			System.out.println(e.getMessage());
		}
	}
	
}
