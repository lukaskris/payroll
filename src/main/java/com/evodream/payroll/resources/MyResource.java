
package com.evodream.payroll.resources;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.imageio.stream.ImageOutputStreamImpl;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import com.evodream.payroll.helper.DatabaseHelper;
import com.evodream.payroll.model.Employee;
import com.evodream.payroll.service.PayrollService;
import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;

/** Example resource class hosted at the URI path "/myresource"
 */
@Path("/myresource")
public class MyResource {

	PayrollService employeeService = new PayrollService();
    /** Method processing HTTP GET requests, producing "text/plain" MIME media
     * type.
     * @return String that will be send back as a response of type "text/plain".
     * @throws UnsupportedEncodingException 
     */
    @GET 
    @Produces("text/plain")
    public String getIt(){
    	String loc = "c:/temp/image.jpg";
//    	File file = new File(loc);
//    	InputStream input;
//		try {
//			input = new FileInputStream(file);
//			byte[] data = Files.readAllBytes(Paths.get(loc));
//			
//			DatabaseHelper.getInstance().updateBlob(data, "karyawan", "2");
//		} catch (FileNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//    	
    	Employee emp = employeeService.getAllEmployee().get(0);
    	try {
			FileUtils.writeByteArrayToFile(new File(loc), emp.getFingerprint());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	
    	
    	return DatabaseHelper.getInstance().isConnected();
    }

    @POST
	@Path("/upload") 
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public Response uploadFile(@FormDataParam("upload") InputStream is, 
	                    @FormDataParam("upload") FormDataContentDisposition formData) {
		String fileLocation = "c:/temp/" + formData.getFileName();
		try {
			saveFile(is, fileLocation);
			String result = "Successfully File Uploaded on the path "+fileLocation;
			return Response.status(Status.OK).entity(result).build();
		} catch (IOException e) {
			e.printStackTrace();
			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
		}
	}
    
    
    
    
	private void saveFile(InputStream is, String fileLocation) throws IOException {
	    	OutputStream os = new FileOutputStream(new File(fileLocation));
		byte[] buffer = new byte[256];
		int bytes = 0;
		while ((bytes = is.read(buffer)) != -1) {
		     os.write(buffer, 0, bytes);
		}
		os.close();
    }
}
