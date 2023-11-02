package net.catcert.iarxiu.client.test;

import java.io.File;
import java.io.InputStream;

import net.catcert.iarxiu.client.proxy.ProxyClient;
import net.catcert.iarxiu.client.test.utils.Utils;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.hp.iarxiu.core.schemas.x20.dissemination.BinaryType;
import com.hp.iarxiu.core.schemas.x20.dissemination.GetBinaryRequestDocument;
import com.hp.iarxiu.core.schemas.x20.dissemination.GetBinaryRequestDocument.GetBinaryRequest;
import com.hp.iarxiu.core.schemas.x20.dissemination.GetBinaryResponseDocument;


/**
 * Classe d'exemple de petició de recuperació d'un determinat binari que 
 * forma part d'un paquet a iArxiu.
 */
public class GetBinaryRequestTest {

	public static void main(String[] args) throws Exception {
		try {
			String fileSeparator = File.separator;
			String userDir = System.getProperty("user.dir").replace("/", fileSeparator);
			String pathOutDir = userDir + "/out/samples/".replace("/", fileSeparator);
			Utils.createIfNotExistsDirectory(pathOutDir);

			ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
			ProxyClient proxy = (ProxyClient) context.getBean("proxyClient");
			String downloadUrl = (String) context.getBean("downloadByTicketUrl");
			
			// Petició de cerca de binaris
			GetBinaryRequestDocument requestDocument = GetBinaryRequestDocument.Factory.newInstance();
			GetBinaryRequest request = requestDocument.addNewGetBinaryRequest();

			request.setPackageId("catcert:m02396:20230824-142252845:6990");
			request.setBinaryId("BIN_1.0");
			Utils.printXmlObject(requestDocument);
			requestDocument.save(new File(pathOutDir + "GetBinaryRequest.xml"));

			GetBinaryResponseDocument responseDocument = (GetBinaryResponseDocument)proxy.send(requestDocument);
			Utils.printXmlObject(responseDocument);
			responseDocument.save(new File(pathOutDir + "GetBinaryResponse.xml"));
			
			// Desem les dades binàries en un fitxer
			BinaryType bin = responseDocument.getGetBinaryResponse();
			String binToken = bin.getBinDataDownloadTicket();
			InputStream binData = Utils.downloadByTicketAsInputStream(binToken, downloadUrl);

			System.out.println(bin.getContentType());
			Utils.writeToFile(pathOutDir + "binData.temp", binData);
		
		} catch(Exception e){
			e.printStackTrace();
		}
	}
	
}
