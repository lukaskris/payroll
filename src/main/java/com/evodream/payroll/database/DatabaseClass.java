package com.evodream.payroll.database;

import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import com.evodream.payroll.helper.DatabaseHelper;
import com.evodream.payroll.model.Employee;
import com.machinezoo.sourceafis.FingerprintTemplate;

public class DatabaseClass {
	private static Map<String, Employee> employees = new HashMap();
	
	public static Map<String, Employee> getCutsSalaryEmployees(){
		employees.clear();
		ResultSet rs = DatabaseHelper.getInstance().query("select * from gaji_karyawan");
		try {
			while (rs.next()) {
				Employee employee = new Employee();
				employee.setNik(rs.getString("nik"));
				employee.setName(rs.getString("nama"));
				employee.setPosition(rs.getString("bagian_id"));
				employee.setSalaryPerDay(rs.getBigDecimal("gajiharian"));
				employee.setSalary(rs.getBigDecimal("gajitotal"));
				employee.setJoin(rs.getString("tanggal_masuk"));
				employee.setOut(rs.getString("tanggal_keluar"));
				employee.setPeriode(rs.getString("periode"));
				employee.setFingerprint(rs.getBytes("fingerprint"));
				FingerprintTemplate probe = new FingerprintTemplate().dpi(500).create(rs.getBytes("fingerprint"));
				employee.setFinger(probe);
				employee.setBalance(rs.getBigDecimal("saldo"));
				employee.setUmk(rs.getBigDecimal("umk"));
				
				if(rs.getBigDecimal("upah").intValue() == 0) {
					employee.setCut(rs.getBigDecimal("pemotongan1"));
				}else if(employee.getSalaryPerDay().intValue() >= rs.getBigDecimal("upah").intValue()) {
					employee.setCut(rs.getBigDecimal("pemotongan2"));
				}else {
					employee.setCut(rs.getBigDecimal("pemotongan1"));
				}
				
				employees.put(employee.getNik(), employee);
			}
			rs.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return employees;
	}
	
	public static Map<String, Employee> getDebtEmployees(){
		employees.clear();
		ResultSet rs = DatabaseHelper.getInstance().query("select * from pinjaman_karyawan");
		try {
			while (rs.next()) {
				Employee employee = new Employee();
				employee.setNik(rs.getString("nik"));
				employee.setName(rs.getString("nama"));
				employee.setPosition(rs.getString("bagian_id"));
				employee.setPeriode(rs.getString("periode"));
				employee.setSalaryPerDay(rs.getBigDecimal("gajiharian"));
				employee.setSalary(rs.getBigDecimal("gajitotal"));
				employee.setJoin(rs.getString("tanggal_masuk"));
				employee.setOut(rs.getString("tanggal_keluar"));
				employee.setFingerprint(rs.getBytes("fingerprint"));
				FingerprintTemplate probe = new FingerprintTemplate().dpi(500).create(rs.getBytes("fingerprint"));
				employee.setFinger(probe);
				employee.setBalance(rs.getBigDecimal("saldo"));
				employee.setDebt(rs.getBigDecimal("pinjaman"));
				employee.setUmk(rs.getBigDecimal("umk"));
				
				
				String q2 = "SELECT * FROM potongan where bagian_id = '"+employee.getPosition() + "'";
				ResultSet rs2 = DatabaseHelper.getInstance().query(q2);
				
				while(rs2.next()) {
					if(rs2.getBigDecimal("upah").intValue() == 0) {
						employee.setCut(rs2.getBigDecimal("pemotongan1"));
					}else if(employee.getSalaryPerDay().intValue() >= rs2.getBigDecimal("upah").intValue()) {
						employee.setCut(rs2.getBigDecimal("pemotongan2"));
					}else {
						employee.setCut(rs2.getBigDecimal("pemotongan1"));
					}
				}
				
				
				
				employees.put(employee.getNik(), employee);
			}
			rs.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return employees;
	}
}
