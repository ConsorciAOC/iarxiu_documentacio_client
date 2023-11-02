package net.catcert.iarxiu.client.test;

import java.io.File;

import net.catcert.iarxiu.client.proxy.ProxyClient;
import net.catcert.iarxiu.client.test.utils.Utils;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.hp.iarxiu.core.schemas.x20.ingest.GetOfflineIngestStatusRequestDocument;
import com.hp.iarxiu.core.schemas.x20.ingest.GetOfflineIngestStatusResponseDocument;
import com.hp.iarxiu.core.schemas.x20.ingest.OfflineIngestInfoType;
import com.hp.iarxiu.core.schemas.x20.ingest.GetOfflineIngestStatusRequestDocument.GetOfflineIngestStatusRequest;
import com.hp.iarxiu.core.schemas.x20.ingest.OfflineIngestInfoType.Status.Enum;


/**
 * Classe d'exemple de petició de consulta de l'estat d'un ingrés fet en mode offline.
 * Es preguntant per l'estat de l'ingrés mentre aquest no ha acabat.
 */
public class GetOfflineIngestStatusTest {

	public static void main(String[] args) throws Exception {

		try {

			String fileSeparator = File.separator;
			String userDir = System.getProperty("user.dir").replace("/", fileSeparator);
			String pathOutDir = userDir + "/out/samples/".replace("/", fileSeparator);
			Utils.createIfNotExistsDirectory(pathOutDir);

			// identificador del ticket
			String ticketId = "1542604";
				
			// càrrega de l'application context
			ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");

			// client
			ProxyClient proxy = (ProxyClient) context.getBean("proxyClient");

			// petició d'ingrés
			GetOfflineIngestStatusRequestDocument requestDocument = GetOfflineIngestStatusRequestDocument.Factory.newInstance();
			GetOfflineIngestStatusRequest getOfflineIngestStatusRequest = requestDocument.addNewGetOfflineIngestStatusRequest();
			getOfflineIngestStatusRequest.setStringValue(ticketId);
			getOfflineIngestStatusRequest.setSignatureValidationDetails(true);
			requestDocument.setGetOfflineIngestStatusRequest(getOfflineIngestStatusRequest);

			Utils.printXmlObject(requestDocument);
			requestDocument.save(new File(pathOutDir + "GetOfflineIngestStatusRequest.xml"));
		
			// informació de l'ingrés offline
			OfflineIngestInfoType ingestInfo = null;
		
			// bucle mentre l'estat és 'en procés'
			while(true) {
				//enviament de la petició
				GetOfflineIngestStatusResponseDocument responseDocument = (GetOfflineIngestStatusResponseDocument)proxy.send(requestDocument);

				//pintem la resposta
				Utils.printXmlObject(responseDocument);

				// Guardem la resposta en un fitxer
				responseDocument.save(new File(pathOutDir + "GetOfflineIngestStatusResponse.xml"));

				//informació
				ingestInfo = responseDocument.getGetOfflineIngestStatusResponse().getOfflineIngestInfo();

				// estat
				Enum status = ingestInfo.getStatus();

				// sortir quan l'estat ja no sigui en procés
				if (!status.equals(OfflineIngestInfoType.Status.IN_PROCESS))
					break;

				// esperem un segon
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					throw new RuntimeException("Error in sleep", e);
				}

				// estats d'error
				if (status.equals(OfflineIngestInfoType.Status.ERROR))
					throw new RuntimeException("Offline error: " + ingestInfo.getErrorCode());
				if (status.equals(OfflineIngestInfoType.Status.UNKNOWN))
					throw new RuntimeException("Unknown offline error: " + ingestInfo.getErrorCode());
			}

			// identificador del paquet obtingut a partir de la informació d'estat de l'ingrés
			String packageId = ingestInfo.getId();
			System.out.println("\n Tiquet: "+packageId);

		}catch(Exception e){
			System.out.println(e.getMessage());
		}

	}

}
