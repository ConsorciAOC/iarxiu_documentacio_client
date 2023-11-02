package net.catcert.iarxiu.client.test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import net.catcert.iarxiu.client.proxy.ProxyClient;
import net.catcert.iarxiu.client.test.utils.Utils;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.xml.sax.SAXException;

import com.hp.iarxiu.core.schemas.x20.ingest.ContentTypeHandlingType;
import com.hp.iarxiu.core.schemas.x20.ingest.GetOfflineIngestStatusRequestDocument;
import com.hp.iarxiu.core.schemas.x20.ingest.GetOfflineIngestStatusResponseDocument;
import com.hp.iarxiu.core.schemas.x20.ingest.GetUploadTicketRequestDocument;
import com.hp.iarxiu.core.schemas.x20.ingest.GetUploadTicketResponseDocument;
import com.hp.iarxiu.core.schemas.x20.ingest.OfflineIngestInfoType;
import com.hp.iarxiu.core.schemas.x20.ingest.OfflineUploadIngestRequestDocument;
import com.hp.iarxiu.core.schemas.x20.ingest.OfflineUploadIngestResponseDocument;
import com.hp.iarxiu.core.schemas.x20.ingest.GetOfflineIngestStatusRequestDocument.GetOfflineIngestStatusRequest;
import com.hp.iarxiu.core.schemas.x20.ingest.GetUploadTicketResponseDocument.GetUploadTicketResponse;
import com.hp.iarxiu.core.schemas.x20.ingest.OfflineUploadIngestRequestDocument.OfflineUploadIngestRequest;


/**
 * Classe d'exemple de petició per realitzar un ingrés per upload comprimit. En un ingrés
 * upload comprimit es puja a una URL un zip amb el fitxer mets i els fitxers de l'expedient
 * dins, com el zip que es fa servir pel OfflineZipIngest. 
 *
 * Un cop s'ha puja el fitxer zip (només s'admet un) es pot fer
 * la petició d'ingrés.
 * 
 * A grans trets seguim els passos següents:
 * 1. Obtenció d'un tiquet d'upload.
 * 2. Validació del fitxer mets de l'expedient. (opcional)
 * 3. Upload del zip. 
 * 4. Obtenció d'un tiquet d'ingrés offline.
 * 5. Consulta del resultat de l'ingrés offline a partir del tiquet d'ingrés.
 * 
 */
public class GetCompressedUploadTicketTest {

	private static final String UPLOAD_SERVLET_PARAM_TICKET = "ticket";
	private static final String UPLOAD_SERVLET_PARAM_COMPRESSED = "compressed";

	public static void main(String[] args) throws Exception {
		try {
			String fileSeparator = File.separator;
			String userDir = System.getProperty("user.dir").replace("/", fileSeparator);
			String pathOutDir = userDir + "/out/samples/".replace("/", fileSeparator);
			Utils.createIfNotExistsDirectory(pathOutDir);

			String folderToZip = userDir + "/src/main/resources/PIT_document_pdf".replace("/", fileSeparator);
			Utils.updateMdDmdFile(folderToZip + "/mets.xml", "voc:data_creacio");
			// Creem el ZIP amb el mets, documents i signatures
			String zipPath = pathOutDir + "PIT_document_pdf.zip";
			Utils.createZipPIT(zipPath, folderToZip);

			// Verifiquem la validesa de l'estructura del fitxer mets
			ZipFile zipFile1 = new ZipFile(zipPath);
			ZipEntry zipEntry = zipFile1.getEntry("mets.xml");
			InputStream inputStream = zipFile1.getInputStream(zipEntry);

			try {
				SchemaFactory factory =
						SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
				File schemaLocation = new File(userDir + "/src/main/resources/xsd/mets.xsd".replace("/", fileSeparator));
				Schema schema = factory.newSchema(schemaLocation);
				Validator validator = schema.newValidator();
				Source source = new StreamSource(inputStream);
				validator.validate(source);
				System.out.println("mets.xml de " + (zipFile1.getName()).substring(10) + " és vàlid.");
			} catch (SAXException ex) {
				System.err.println("mets.xml de " + (zipFile1.getName()).substring(10) + " no és vàlid perquè " + ex.getMessage());
			} catch (IOException ex) {
				System.err.println("mets.xml de " + (zipFile1.getName()).substring(10) + " no és vàlid perquè " + ex.getMessage());
			}

			/*
			 * 1. Obtenció del tiquet d'upload.
			 */

			// Càrrega de l'application context i recuperació de paràmetres
			ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
			ProxyClient proxy = (ProxyClient) context.getBean("proxyClient");
			String uploadUrl = (String) context.getBean("uploadUrl");

			// Petició del tiquet d'upload
			GetUploadTicketRequestDocument requestDocument = GetUploadTicketRequestDocument.Factory.newInstance();
			requestDocument.addNewGetUploadTicketRequest();
			Utils.printXmlObject(requestDocument); // pintem la petició
			// Guardem la petició en un fitxer
			requestDocument.save(new File(pathOutDir + "GetCompressedUploadTicketRequest.xml"));

			// Enviament de la petició
			Utils.printXmlObject(requestDocument);
			GetUploadTicketResponseDocument responseDocument = (GetUploadTicketResponseDocument) proxy.send(requestDocument);
			Utils.printXmlObject(responseDocument);
			responseDocument.save(new File(pathOutDir + "GetCompressedUploadTicketResponse.xml"));

			// De la response ens interessa el ticket d'upload
			GetUploadTicketResponse response = responseDocument.getGetUploadTicketResponse();
			String uploadTicket = response.getTicket();

			/*
			 * 2. Upload del fitxer mets.xml i els fitxers de l'expedient
			 */

			boolean uploaded = upload(uploadUrl, uploadTicket, zipPath);
			if (!uploaded) {
				System.out.println("S'ha produit un error durant la càrrega del/s fitxer/s.");
			} else {
				/*
				 * 4. Si tots els fitxers s'han pujat correctament, fem la
				 * sol·licitud d'ingrés. L'aplicació ens tornarà el tiquet
				 * identificatiu de la petició d'ingrés.
				 */
				System.out.println("\nFitxer/s carregat/s correctament a iArxiu.");
				System.out.println("Sol·licitant ingrés offline...");

				// Petició d'ingrés
				OfflineUploadIngestRequestDocument offlineUploadIngestRequestDocument = OfflineUploadIngestRequestDocument.Factory.newInstance();
				OfflineUploadIngestRequest offlineUploadIngestRequest = offlineUploadIngestRequestDocument.addNewOfflineUploadIngestRequest();
				offlineUploadIngestRequest.setUploadTicket(uploadTicket);

				// Preservació d'evidència:
				// 		true -> preservació d'evidència i de continguts
				// 		false -> només preservació dels continguts
				offlineUploadIngestRequest.setPreservation(false);

				// Tipus de detecció de contingut:
				//		replaceWithIntrospection -> 	Si s'identifica un format i és diferent de l'indicat pel client,
				//							   			aleshores es substitueix pel detectat.
				//		completeWithIntrospection -> 	Si s'identifica un format i és diferent de l'indicat pel client,
				//   						   			només s'informa el PREMIS amb les dades obtingudes amb DROID. Quan
				//   						   			no hi ha coincidència, es respecten les dades que posa el client.
				//		checkAndReject -> 	 	 	 	Si s'identifica un format i és diferent de l'indicat pel client,
				//							   			aleshores es rebutja el paquet.
				offlineUploadIngestRequest.setContentTypeHandling(ContentTypeHandlingType.COMPLETE_WITH_INTROSPECTION);

				Utils.printXmlObject(offlineUploadIngestRequestDocument);

				// Enviament de la petició d'ingrés
				OfflineUploadIngestResponseDocument offlineUploadIngestResponseDocument =
						(OfflineUploadIngestResponseDocument) proxy.send(offlineUploadIngestRequestDocument);

				Utils.printXmlObject(offlineUploadIngestResponseDocument);

				// Capturem el ticket d'ingrés
				String ingestTicket = offlineUploadIngestResponseDocument.getOfflineUploadIngestResponse();

				/*
				 * 5. Un cop obtingut el tiquet de la petició d'ingrés anem consultant
				 * a l'aplicació per l'estat de l'ingrés. Anirem fent la consulta
				 * fins que l'aplicació ens digui que ja no l'està processant.
				 */
				System.out.println("\nSol·licitem estat d'ingrés offline mentres estigui en procés...");

				OfflineIngestInfoType.Status.Enum status = OfflineIngestInfoType.Status.IN_PROCESS;
				while (status.equals(OfflineIngestInfoType.Status.IN_PROCESS)) {

					// Esperem 10 segons entre consulta i consulta
					Thread.sleep(10000);

					// Preparem la petició d'estat de l'ingrés
					GetOfflineIngestStatusRequestDocument getOfflineIngestStatusRequestDocument =
							GetOfflineIngestStatusRequestDocument.Factory.newInstance();
					GetOfflineIngestStatusRequest getOfflineIngestStatusRequest =
							getOfflineIngestStatusRequestDocument.addNewGetOfflineIngestStatusRequest();
					getOfflineIngestStatusRequest.setStringValue(ingestTicket);
					getOfflineIngestStatusRequest.setSignatureValidationDetails(true);

					getOfflineIngestStatusRequestDocument.setGetOfflineIngestStatusRequest(getOfflineIngestStatusRequest);
					Utils.printXmlObject(getOfflineIngestStatusRequestDocument);

					// Enviament de la petició sobre l'estat de l'ingrés
					GetOfflineIngestStatusResponseDocument getOfflineIngestStatusResponseDocument = (GetOfflineIngestStatusResponseDocument) proxy.send(getOfflineIngestStatusRequestDocument);
					Utils.printXmlObject(getOfflineIngestStatusResponseDocument);

					// Capturem l'estat de l'ingrés de la resposta
					OfflineIngestInfoType statusInfo =
							getOfflineIngestStatusResponseDocument.getGetOfflineIngestStatusResponse().getOfflineIngestInfo();
					status = statusInfo.getStatus(); // si deixa d'estar en procés es trenca el bucle
				}
			}

		} catch (Exception e){
			e.printStackTrace();
		}
	}

	
	/**
	 * Upload del fitxer METS i dels fitxers adjunts al servidor.
	 * 
	 * @param uploadUrl url a la qual pujarem els fitxers 
	 * @param uploadTicket tiquet identificatiu de l'upload
	 * @param zipPath path absolut del fitxer METS
	 * @return true -> els fitxers s'han pujat correctament al servidor; false -> hi ha hagut algun problema
	 */
	private static boolean upload(String uploadUrl, String uploadTicket, String zipPath){
	
		try{
			File zipFile = new File(zipPath);
			HttpClient httpClient = new HttpClient();
			PostMethod post = new PostMethod(uploadUrl);
			
			// Identificador del tiquet d'upload, i el flag compressed
			Part ticketPart = new StringPart(UPLOAD_SERVLET_PARAM_TICKET, uploadTicket);
			Part compressedPart = new StringPart(UPLOAD_SERVLET_PARAM_COMPRESSED, "true");
			
			// Arxiu ZIP
			Part zipPart = new FilePart("upload.zip", zipFile);

			// Parts
			Part[] parts = new Part[]{ticketPart, compressedPart, zipPart};
			
			post.setRequestEntity( new MultipartRequestEntity(parts,post.getParams()) );
			
			int result = httpClient.executeMethod(post);
			
			return result==200; // true si la petició http retorna un codi 200
		
		} catch(Exception e){
			e.printStackTrace();
			return false;
		}
	}

}
