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

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.xml.sax.SAXException;

import com.hp.iarxiu.core.schemas.x20.ingest.ContentTypeHandlingType;
import com.hp.iarxiu.core.schemas.x20.ingest.OfflineZipIngestRequestDocument;
import com.hp.iarxiu.core.schemas.x20.ingest.OfflineZipIngestResponseDocument;
import com.hp.iarxiu.core.schemas.x20.ingest.OfflineZipIngestRequestDocument.OfflineZipIngestRequest;


/**
 * Classe d'exemple de petició d'inserció a iArxiu d'un PIT comprimit dins un fitxer ZIP, en mode offline.
 */
public class OfflineZipIngestTest {

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

			// PIT: Paquet d'Informació de Transferència
			byte[] zipFile = loadFileData(zipPath);

			// Verifiquem la validesa de l'estructura dels mets
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
				System.out.println("mets.xml de " + zipFile1.getName() + " és vàlid.");
			}
			catch (SAXException ex) {
				System.err.println("mets.xml de " + zipFile1.getName() + " no és vàlid perquè " + ex.getMessage());
			}catch (IOException ex) {
				System.err.println("mets.xml de " + zipFile1.getName() + " no és vàlid perquè " + ex.getMessage());
			}

			// Càrrega de l'application context
			ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");

			// Client
			ProxyClient proxy = (ProxyClient) context.getBean("proxyClient");

			// Petició d'ingrés d'un arxiu zip
			OfflineZipIngestRequestDocument requestDocument = OfflineZipIngestRequestDocument.Factory.newInstance();
			OfflineZipIngestRequest request = requestDocument.addNewOfflineZipIngestRequest();

			// Preservació d'evidència:
			// 		true -> preservació d'evidència i de continguts
			// 		false -> només preservació dels continguts
			request.setPreservation(false);

			// Tipus de detecció de contingut:
			//		replaceWithIntrospection -> 	Si s'identifica un format i és diferent de l'indicat pel client,
			//							   			aleshores es substitueix pel detectat.
			//		completeWithIntrospection -> 	Si s'identifica un format i és diferent de l'indicat pel client,
			//   						   			només s'informa el PREMIS amb les dades obtingudes amb DROID. Quan
			//   						   			no hi ha coincidència, es respecten les dades que posa el client.
			//		checkAndReject -> 	 	 	 	Si s'identifica un format i és diferent de l'indicat pel client,
			//							   			aleshores es rebutja el paquet.
			request.setContentTypeHandling(ContentTypeHandlingType.COMPLETE_WITH_INTROSPECTION);

			// ZIP
			request.setZipFile(zipFile);

			// Pintem la petició
			Utils.printXmlObject(requestDocument);

			// Guardem la petició en un fitxer
			requestDocument.save(new File(pathOutDir + "OfflineZipIngestRequest.xml"));

			OfflineZipIngestResponseDocument responseDocument = (OfflineZipIngestResponseDocument)proxy.send(requestDocument);

			// Pintem la resposta
			Utils.printXmlObject(responseDocument);

			// Guardem la resposta en un fitxer
			responseDocument.save(new File(pathOutDir + "OfflineZipIngestResponse.xml"));

		} catch(Exception e){
			System.out.println(e.getMessage());
		}
	}

	/**
	 * Càrrega de les dades d'un fitxer.
	 * @param filePath path del fitxer relatiu al classpath
	 * @return byte[] del fitxer
	 * @throws Exception
	 */
	private static byte[] loadFileData(String filePath) throws Exception {
		return Utils.getBytesFromFile(filePath);
	}
}
