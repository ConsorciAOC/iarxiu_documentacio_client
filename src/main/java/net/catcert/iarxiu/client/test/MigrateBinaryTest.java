package net.catcert.iarxiu.client.test;

import java.io.File;
import java.io.InputStream;

import net.catcert.iarxiu.client.proxy.ProxyClient;
import net.catcert.iarxiu.client.test.utils.Utils;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.hp.iarxiu.core.schemas.x20.dissemination.MigrateBinaryRequestDocument;
import com.hp.iarxiu.core.schemas.x20.dissemination.MigrateBinaryRequestDocument.MigrateBinaryRequest;
import com.hp.iarxiu.core.schemas.x20.dissemination.MigrateBinaryResponseDocument;
import com.hp.iarxiu.core.schemas.x20.dissemination.MigrateBinaryResponseDocument.MigrateBinaryResponse;


/**
 * Classe d'exemple de petició de migració del format d'un binari prèviament carregat a iArxiu.
 */
public class MigrateBinaryTest {

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
			String downloadUrl = (String) context.getBean("downloadByTicketUrl");

			// Petició de migració de binari
			MigrateBinaryRequestDocument requestDocument = MigrateBinaryRequestDocument.Factory.newInstance();
			MigrateBinaryRequest request = requestDocument.addNewMigrateBinaryRequest();

			// Identificador del paquet
			request.setPackageId("catcert:1764904091507:20251212-135353912:5050");

			// Identificador del binari
			request.setBinaryId("BIN_1.0");

			// Format a migració (que ja no sigui un PDF)
			request.setToFormat("application/pdf");

			// Pintem la petició
			Utils.printXmlObject(requestDocument);

			// Guardem la petició en un fitxer
			requestDocument.save(new File(pathOutDir + "MigrateBinaryRequest.xml"));

			// Enviament de la petició
			MigrateBinaryResponseDocument responseDocument = (MigrateBinaryResponseDocument)proxy.send(requestDocument);

			// Pintem la resposta
			Utils.printXmlObject(responseDocument);

			// Guardem la resposta en un fitxer
			responseDocument.save(new File(pathOutDir + "MigrateBinaryResponse.xml"));

			MigrateBinaryResponse response = responseDocument.getMigrateBinaryResponse();
			String binToken = response.getContentDownloadTicket();
			InputStream binData = Utils.downloadByTicketAsInputStream(binToken, downloadUrl);
			Utils.writeToFile(pathOutDir + "MigratedDocument.pdf", binData);

		} catch(Exception e){
			System.out.println(e.getMessage());
		}
	}
}
