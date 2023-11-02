package net.catcert.iarxiu.client.test;

import gov.loc.mets.MetsDocument;
import gov.loc.mets.MetsDocument.Mets;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

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
import com.hp.iarxiu.core.schemas.x20.ingest.OfflineIngestRequestDocument;
import com.hp.iarxiu.core.schemas.x20.ingest.OfflineIngestResponseDocument;
import com.hp.iarxiu.core.schemas.x20.ingest.OfflineIngestRequestDocument.OfflineIngestRequest;


/**
 * Classe d'exemple de petició d'inserció d'un PIT a iArxiu, en mode offline.
 */
public class OfflineIngestTest {

	public static void main(String[] args) throws Exception {
		try {
			String fileSeparator = File.separator;
			String userDir = System.getProperty("user.dir").replace("/", fileSeparator);
			String pathOutDir = userDir + "/out/samples/".replace("/", fileSeparator);
			Utils.createIfNotExistsDirectory(pathOutDir);

			String path = userDir + "/src/main/resources/INT-2008-6.xml".replace("/", fileSeparator);
			Utils.updateMdDmdFile(path, "exp:data_tancament");

			//Verifiquem la validesa de la estructura del mets
			File sourceLocation = new File(path);
			try {

				// 1. Lookup a factory for the W3C XML Schema language
		        SchemaFactory factory =
		        SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");

				// 2. Compile the schema.
				// Here the schema is loaded from a java.io.File, but you could use
				// a java.net.URL or a javax.xml.transform.Source instead.
				File schemaLocation = new File(userDir + "/src/main/resources/xsd/mets.xsd".replace("/", fileSeparator));
				Schema schema = factory.newSchema(schemaLocation);

		        // 3. Get a validator from the schema.
		        Validator validator = schema.newValidator();

		        // 4. Parse the document you want to check.
		        Source source = new StreamSource(sourceLocation);

		        // 5. Check the document
	            validator.validate(source);
	            System.out.println(sourceLocation.toString() + " és vàlid.");
	        }
	        catch (SAXException ex) {
	            System.err.println(sourceLocation.toString() + " no és vàlid perquè " + ex.getMessage());
	        }catch (IOException ex) {
	            System.err.println(sourceLocation.toString() + " no és vàlid perquè " + ex.getMessage());
	        }

			// PIT: Paquet d'Informació de Transferència
			Mets mets = loadMets(path);

			// Càrrega de l'application context
			ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");

			// Client
			ProxyClient proxy = (ProxyClient) context.getBean("proxyClient");

			// Petició d'ingrés
			OfflineIngestRequestDocument requestDocument = OfflineIngestRequestDocument.Factory.newInstance();
			OfflineIngestRequest request = requestDocument.addNewOfflineIngestRequest();

			// Preservació d'evidència:
			// 		true -> preservació d'evidència i de continguts
			// 		false -> només preservació dels continguts
			request.setPreservation(true);

			// Tipus de detecció de contingut:
			//		replaceWithIntrospection -> 	Si s'identifica un format i és diferent de l'indicat pel client,
			//							   			aleshores es substitueix pel detectat.
			//		completeWithIntrospection -> 	Si s'identifica un format i és diferent de l'indicat pel client,
			//   						   			només s'informa el PREMIS amb les dades obtingudes amb DROID. Quan
			//   						   			no hi ha coincidència, es respecten les dades que posa el client.
			//		checkAndReject -> 	 	 	 	Si s'identifica un format i és diferent de l'indicat pel client,
			//							   			aleshores es rebutja el paquet.
			request.setContentTypeHandling(ContentTypeHandlingType.COMPLETE_WITH_INTROSPECTION);

			//PIT
			request.setMets(mets);

			// Pintem la petició
			Utils.printXmlObject(requestDocument);

			// Guardem la petició en un fitxer
			requestDocument.save(new File(pathOutDir + "OfflineIngestRequest.xml"));

			// Enviament de la petició
			OfflineIngestResponseDocument responseDocument = (OfflineIngestResponseDocument)proxy.send(requestDocument);

			// Pintem la resposta
			Utils.printXmlObject(responseDocument);

			// Guardem la resposta en un fitxer
			responseDocument.save(new File(pathOutDir + "OfflineIngestResponse.xml"));

		}catch(Exception e){
			System.out.println(e.getMessage());
		}

	}

	/**
	 * Càrrega del METS.
	 * @param metsName path del METS relatiu al classpath
	 * @return METS
	 * @throws Exception
	 */
	private static Mets loadMets(String metsName) throws Exception {
		//InputStream is = ClassLoader.getSystemResourceAsStream(metsName);
		File dmdFile = new File(metsName);
		InputStream is = new FileInputStream(dmdFile);
		return MetsDocument.Factory.parse(is).getMets();
	}

}
