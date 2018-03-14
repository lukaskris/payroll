package com.evodream.payroll.service;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.evodream.payroll.database.DatabaseClass;
import com.evodream.payroll.exception.DataNotFoundException;
import com.evodream.payroll.helper.DatabaseHelper;
import com.evodream.payroll.model.Employee;
import com.machinezoo.sourceafis.FingerprintTemplate;

public class PayrollService {
	
	private Map<String, Employee> employees = DatabaseClass.getCutsSalaryEmployees();
	
	public PayrollService() {}
	
	public List<Employee> getAllEmployee(){
		return new ArrayList<Employee>(employees.values());
	}
	
	public List<Employee> getEmployees(String filter, int offset, int limit, String sort){
		return new ArrayList<Employee>(DatabaseClass.getEmployees(filter, offset, limit, sort).values());
	}
	
	

	public Employee getEmployee(String id) {
		Employee employee = employees.get(id);
		if(employee == null) throw new DataNotFoundException("Message with id " + id + " not found");
		return employee;
	}
		
	public void getEmployeesCuts() {
		employees.clear();
		employees = DatabaseClass.getCutsSalaryEmployees();
	}
	
	public void getEmployeesDebt() {
		employees.clear();
		employees = DatabaseClass.getDebtEmployees();
	}
	
	public Employee getEmployeesDebt(String id) {
		employees.clear();
		employees = DatabaseClass.getDebtEmployees();
		Employee employee = employees.get(id);
		if(employee == null) throw new DataNotFoundException("Message with id " + id + " not found");
		employee.setFingerprint(null);
		employee.setFinger(null);
		return employee;
	}
	
	public Employee updateEmployee(Employee employee) {
		if(employee.getId() <= 0) {
			return null;
		}
		employees.put(employee.getNik(), employee);
		return employee;
	}
	
	public Employee removeEmployee(long id) {
		return employees.remove(id);
	}
	
	public Employee addEmployee(Employee employee) {
		employees.put(employee.getNik(), employee);
		return employee;
	}
	
	public Employee GetPinjamanPeriodeSebelumnya(Employee employee) {
		
		String query = "Select * from pinjaman_karyawan_periode_sebelumnya where nik = " + employee.getNik();
		
		ResultSet rs = DatabaseHelper.getInstance().query(query);
		try {
			while (rs.next()) {
				employee.setLastDebt(1);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		query = "select * from previous_history_potongan where karyawan_id = " + employee.getNik();
		rs = DatabaseHelper.getInstance().query(query);
		try {
			while(rs.next()) {
				employee.setDebt(rs.getBigDecimal("hutang"));
			}
		}catch (Exception e) {
			// TODO: handle exception
		}
		
		
		return employee;
	}
	
	//pinjam uang (butuh cuman nik dan nominal pinjaman)
	public Integer ApproveDebt(Employee employee) {
		String query = "INSERT INTO history_pinjaman(karyawan_id, pinjaman, periode, sudah_umk, create_at) values(?,?, (select periode from periode order by id desc limit 1),?,now())";
		int umk = 0;

		if(employee.getBalance().intValue() >= employee.getUmk().intValue()){
			umk = 1;
		}
		PreparedStatement ps;
		try {
			ps = DatabaseHelper.getInstance().getConnection().prepareStatement(query);
			ps.setString(1, employee.getNik());
			ps.setBigDecimal(2, employee.getDebt());
			ps.setInt(3, umk);
			ps.executeUpdate();
			return UpdateSaldo(employee.getDebt(), employee.getNik()); 
		} catch (SQLException e) {
			e.printStackTrace();
			return -1;
		}
	}
	
	//terima gaji 
	public Integer ApproveSalary(Employee employee){
		String query = "INSERT INTO history_gaji(karyawan_id, create_at, gaji, periode) values(?,now(), ?, (select periode from periode order by id desc limit 1))";

		PreparedStatement ps;
		try {
			ps = DatabaseHelper.getInstance().getConnection().prepareStatement(query);
			ps.setString(1, employee.getNik());
			ps.setBigDecimal(2, employee.getSalary());
			ps.executeUpdate();
			return ApprovePotongan(employee); 
		} catch (SQLException e) {
			e.printStackTrace();
			return -1;
		}
	}	
	
	//insert di potongan
	private Integer ApprovePotongan(Employee employee){
		if(employee.getCut().intValue() > 0){
			String query = "INSERT INTO history_potongan(karyawan_id, create_at, potongan, hutang, periode) values(?,now(),?,?, (select periode from periode order by id desc limit 1))";
			BigDecimal potongan = BigDecimal.ZERO;
			BigDecimal hutang = BigDecimal.ZERO;
			if(employee.getSalary().intValue() >= employee.getCut().intValue()) {
				potongan = employee.getCut();
			}else {
				potongan = employee.getSalary(); //kalo misal baru masuk pegawainya 
				hutang = new BigDecimal(employee.getCut().intValue() - potongan.intValue());
			}
			
			potongan = potongan.add(employee.getDebt());
			PreparedStatement ps;
			try {
				ps = DatabaseHelper.getInstance().getConnection().prepareStatement(query);
				ps.setString(1, employee.getNik());
				ps.setBigDecimal(2, potongan);
				ps.setBigDecimal(3, hutang);
				
				ps.executeUpdate();
				
				return UpdateSaldo(potongan, employee.getNik());
			} catch (SQLException e) {
				e.printStackTrace();
				return -1;
			}
		}
		return -1;		
	}
	
	//update saldo karyawan setelah terima gaji
	private Integer UpdateSaldo(BigDecimal uang, String nik) {
		String query = "UPDATE karyawan SET SALDO = SALDO + ? WHERE NIK = ?";
		
		PreparedStatement ps;
		try {
			ps = DatabaseHelper.getInstance().getConnection().prepareStatement(query);
			ps.setBigDecimal(1, uang);
			ps.setString(2, nik);
			return ps.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
			return -1;
		} 
	}
	
}
