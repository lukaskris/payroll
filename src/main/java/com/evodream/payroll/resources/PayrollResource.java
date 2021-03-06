package com.evodream.payroll.resources;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.io.IOUtils;
import org.codehaus.jackson.map.deser.StdDeserializer.CalendarDeserializer;

import com.evodream.payroll.database.DatabaseClass;
import com.evodream.payroll.exception.DataNotFoundException;
import com.evodream.payroll.helper.DatabaseHelper;
import com.evodream.payroll.model.Employee;
import com.evodream.payroll.model.Position;
import com.evodream.payroll.service.PayrollService;
import com.machinezoo.sourceafis.FingerprintMatcher;
import com.machinezoo.sourceafis.FingerprintTemplate;
import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;
import com.sun.research.ws.wadl.Application;

@Path("/employee")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class PayrollResource {
	PayrollService employeeService = new PayrollService();
	
	@GET
	public List<Employee> getEmployees(@QueryParam("filter") String filter, @QueryParam("offset") int offset, @QueryParam("limit") int limit, @QueryParam("sort") String sort ){
		
		return employeeService.getEmployees(filter, offset, limit, sort);
	}
	
	@GET
	@Path("/find/{id}")
	public Employee getEmployee(@PathParam("id") String id) {
		return employeeService.getEmployeesDebt(id);
	}
	
//	@GET
//	@Path("{id}")
//	public Employee getEmployee(@PathParam("id") String id) {
//		Employee employee = employeeService.getEmployee(id);
//		return employee;
//	}
	
	@GET
	@Produces( MediaType.APPLICATION_JSON )
	@Path("/position")
	public List<Position> getBagian() {
		String query = "select * from bagian";
		ResultSet rs = DatabaseHelper.getInstance().query(query);
		List<Position> pos = new ArrayList();
		try {
			while(rs.next()) {
				Position posNew = new Position();
				posNew.setId(rs.getString(1));
				posNew.setNama(rs.getString(2));
				pos.add(posNew);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(pos.toString());
		return pos;
	}
	
	@GET
	@Path("/login/{username}/{password}")
	public Response setLogin(@PathParam("username")String username, @PathParam("password")String password) {
		try {
			String query = "SELECT * FROM user WHERE username = '"+ username +"' and password = md5('"+ password +"')";
			ResultSet rs = DatabaseHelper.getInstance().query(query);
			while(rs.next()) {
				return Response.status(Status.OK).entity(rs.getString("role")).build();
			}
			return Response.status(Status.OK).entity("false").build();
		}catch (Exception e) {
			// TODO: handle exception
			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
		}
	}
	@POST
	@Path("/addkaryawan") 
	public Response uploadFile(Employee employee) {
		if(DatabaseHelper.getInstance().insertKaryawan(employee) > 0) {
			return Response.status(Status.OK).entity(employee).build();			
		}
		return Response.status(Status.INTERNAL_SERVER_ERROR).build();
	}

	//buat cek untuk mau ambil uang
	@POST
	@Path("/upload/{id}")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public Response upload(@FormDataParam("upload") InputStream is, @FormDataParam("upload") FormDataContentDisposition formData, @PathParam("id") String nik) {
		try {

			byte[] bytes = IOUtils.toByteArray(is);
			DatabaseHelper.getInstance().updateFinger(nik, bytes);
			return Response.status(Status.OK).entity("Ok").build();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
		}
	}
	
//	Buat peminjaman uang
	@POST
	@Path("/debt")
	public Response setDebtEmployee(Employee employee) {
		try {
			String query = "INSERT INTO history_pinjaman(karyawan_id, pinjaman, periode, create_at) VALUES(?, ?, (select periode from periode order by id desc limit 1), now())";
			
			PreparedStatement ps = DatabaseHelper.getInstance().getConnection().prepareStatement(query);
			ps.setString(1, employee.getNik());
			ps.setBigDecimal(2, employee.getBorrow());
			ps.executeUpdate();
			
			query = "UPDATE karyawan SET saldo = saldo - ? WHERE nik = ?";
			ps = DatabaseHelper.getInstance().getConnection().prepareStatement(query);
			ps.setBigDecimal(1, employee.getBorrow());
			ps.setString(2, employee.getNik());
			ps.executeUpdate();
			return Response.status(Status.OK).build();
		} catch (SQLException e) {
			e.printStackTrace();
			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
		}
	}
	
	
	@POST
	public Response addEmployee(Employee employee) {
		Employee newEmployee = employeeService.addEmployee(employee);
		return Response.status(Status.CREATED).entity(newEmployee).build();
	}
	
	//buat untuk teken ok di android setelah mendapatkan informasi gaji karyawan
	@POST
	@Path("/approvesalary")
	public Response approveSalary(Employee employee) {
		if (employeeService.ApproveSalary(employee) != -1) {
			return Response.status(Status.OK).build();
		}
		return Response.status(Status.INTERNAL_SERVER_ERROR).build();
	}
	
	@POST
	@Path("/approvedebtmoney")
	public Response approveDebtMoney(Employee employee) {
		if(employeeService.ApproveDebt(employee) != -1){
			return Response.status(Status.OK).build();
		}
		return Response.status(Status.INTERNAL_SERVER_ERROR).build();
	}
	
	@GET
	 @Path("/income")
	 public Response getIncome() {
	  String query = "select ifnull(sum(potongan), 0) as income from history_potongan where periode = (select periode from periode order by id desc limit 1)";
	  ResultSet rs = DatabaseHelper.getInstance().query(query);
	  try {
	   while(rs.next()) {
	    return Response.status(Status.OK).entity(rs.getString("income")).build();
	   }
	  } catch (SQLException e) {
	   e.printStackTrace();
	  }
	  return Response.status(Status.OK).entity("0").build();
	 }
	 
	 @GET
	 @Path("/outcome")
	 public Response getOutcome() {
	  String query = "select ifnull(sum(gaji), 0) as outcome from history_gaji where periode = (select periode from periode order by id desc limit 1)";
	  ResultSet rs = DatabaseHelper.getInstance().query(query);
	  try {
	   while(rs.next()) {
	    return Response.status(Status.OK).entity(rs.getString("outcome")).build();
	   }
	  } catch (SQLException e) {
	   e.printStackTrace();
	  }
	  return Response.status(Status.OK).entity("0").build();
	 }
	 
	 @GET
	 @Path("/newemployee")
	 public Response getNewEmployee() {
	  String query = "select count(*) as total from karyawan where month(tanggal_masuk) = month(now())";
	  ResultSet rs = DatabaseHelper.getInstance().query(query);
	  try {
	   while(rs.next()) {
	    return Response.status(Status.OK).entity(rs.getString("total")).build();
	   }
	  } catch (SQLException e) {
	   e.printStackTrace();
	  }
	  return Response.status(Status.OK).entity("0").build();
	 }
	 
	 @GET
	 @Path("/umkkaryawan")
	 public Response getUmkEmployee() {
	  String query = "select count(*) as total from gaji_karyawan where periode = (select periode from periode order by id limit 1) and gajitotal>=umk";
	  ResultSet rs = DatabaseHelper.getInstance().query(query);
	  try {
	   while(rs.next()) {
	    return Response.status(Status.OK).entity(rs.getString("total")).build();
	   }
	  } catch (SQLException e) {
	   e.printStackTrace();
	  }
	  return Response.status(Status.OK).entity("0").build();
	 }
	 	 
	 @GET
	 @Path("/periode")
	 public Response getPeriode() {
	  String query = "select periode from periode order by id limit 1";
	  ResultSet rs = DatabaseHelper.getInstance().query(query);
	  try {
	   while(rs.next()) {
	    return Response.status(Status.OK).entity(rs.getString("periode")).build();
	   }
	  } catch (SQLException e) {
	   e.printStackTrace();
	  }
	  return Response.status(Status.OK).entity("0").build();
	 }
	 
	 @GET
	 @Path("/debtemployee")
	 public Response getDebtEmployee() {
	  String query = "select count(*) as total from history_pinjaman where periode = (select periode from periode order by id desc limit 1)";
	  ResultSet rs = DatabaseHelper.getInstance().query(query);
	  try {
	   while(rs.next()) {
	    return Response.status(Status.OK).entity(rs.getString("total")).build();
	   }
	  } catch (SQLException e) {
	   e.printStackTrace();
	  }
	  return Response.status(Status.OK).entity("0").build();
	 }
	 
	 
	 @GET
	 @Path("/exitemployee")
	 public Response getExitEmployee() {
	  String query = "select * from karyawan where month(tanggal_keluar) = month(now())";
	  ResultSet rs = DatabaseHelper.getInstance().query(query);
	  try {
	   while(rs.next()) {
	    return Response.status(Status.OK).entity(rs.getString("total")).build();
	   }
	  } catch (SQLException e) {
	   e.printStackTrace();
	  }
	  return Response.status(Status.OK).entity("0").build();
	 }
	
	//buat cek untuk penggajian
	@POST
	@Path("/takesalary")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public Response findEmployee(@FormDataParam("upload") InputStream is, @FormDataParam("upload") FormDataContentDisposition formData) {
		try {
			employeeService.getEmployeesCuts();
			byte[] bytes = IOUtils.toByteArray(is);
			FingerprintTemplate probe = new FingerprintTemplate().dpi(500).create(bytes);
			FingerprintMatcher matcher = new FingerprintMatcher().index(probe);
			Employee employee = null;
			double high = 0;
			for(Iterator<Employee> i = employeeService.getAllEmployee().iterator(); i.hasNext(); ) {
				  Employee item = i.next();
				  double score = matcher.match(item.finger);
				  if(score>high) {
					  high = score;
					  employee = item;
				  }
			}
			if(high >= 40) {
				employee.setFingerprint(null);
				if(employee.getPeriode() == null) {
					employee = employeeService.GetPinjamanPeriodeSebelumnya(employee);
					return Response.status(Status.OK).entity(employee).build();
				}else {
					return Response.status(Status.OK).entity(null).build();
				}
			}
			return Response.status(Status.OK).entity(null).build();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
		} 
	}
		
	//buat cek untuk mau ambil uang
	@POST
	@Path("/takemoney")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public Response findEmployeeTakeMoney(@FormDataParam("upload") InputStream is, @FormDataParam("upload") FormDataContentDisposition formData) {
		try {
			employeeService.getEmployeesDebt();
			byte[] bytes = IOUtils.toByteArray(is);
			FingerprintTemplate probe = new FingerprintTemplate().dpi(500).create(bytes);
			FingerprintMatcher matcher = new FingerprintMatcher().index(probe);
			Employee employee = null;
			double high = 0;
			for(Iterator<Employee> i = employeeService.getAllEmployee().iterator(); i.hasNext(); ) {
				  Employee item = i.next();
				  double score = matcher.match(item.finger);
				  if(score>high) {
					  high = score;
					  employee = item;
				  }
			}
			if(high >= 40) {
							
				employee.setFingerprint(null);
				
				return Response.status(Status.OK).entity(employee).build();
			}
			
			return Response.status(Status.OK).entity(null).build();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
		}
	}
	
	@PUT
	@Path("/{id}")
	public Employee updateEmployee(@PathParam("id") long id, Employee employee) {
		employee.setId(id);
		return employeeService.updateEmployee(employee);
	}
	
	@DELETE
	@Path("/{id}")
	public void deleteMessage(@PathParam("id") long id) {
		employeeService.removeEmployee(id);
	}
	
}