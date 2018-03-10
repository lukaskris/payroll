package com.evodream.payroll.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Date;

import javax.xml.bind.annotation.XmlRootElement;

import com.google.gson.annotations.SerializedName;
import com.machinezoo.sourceafis.FingerprintMatcher;
import com.machinezoo.sourceafis.FingerprintTemplate;

@XmlRootElement
public class Employee implements Serializable {
	private long id; // Id(autoincrement)
	private String name; // Name
	private String nik; // NIK
	private String position; //Posisi
	private BigDecimal salary; //Gaji
	private BigDecimal salaryPerDay; //Gaji
	private BigDecimal cut; //Potongan
	private BigDecimal debt; //Hutang
	private BigDecimal borrow; //maupinjamberapa
	private BigDecimal saving; //Tabungan
	private String join; //Tanggal masuk
	private String out; //Tanggal Keluar
	private BigDecimal balance;
	private Integer lastDebt; //cek apakah ada pernah pinjam
	private String periode;
	private byte[] fingerprint;
	public FingerprintTemplate finger;
	private BigDecimal umk;
	
	public Employee() {}
	
	public Employee(long id, String name) {
		this.id = id;
		this.name = name;
	}
	
	public long getId() {
		return id;
	}
	
	public Integer getLastDebt() {
		return lastDebt;
	}

	public void setLastDebt(Integer lastDebt) {
		this.lastDebt = lastDebt;
	}

	public String getName() {
		return name;
	}
		
	public void setId(long id) {
		this.id = id;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getNik() {
		return nik;
	}

	public void setNik(String nik) {
		this.nik = nik;
	}

	public String getPosition() {
		return position;
	}

	public String getPeriode() {
		return periode;
	}

	public void setPeriode(String periode) {
		this.periode = periode;
	}

	public void setPosition(String position) {
		this.position = position;
	}

	public BigDecimal getSalary() {
		return salary;
	}

	public void setSalary(BigDecimal salary) {
		this.salary = salary;
	}

	public BigDecimal getCut() {
		return cut;
	}

	public void setCut(BigDecimal cut) {
		this.cut = cut;
	}

	public BigDecimal getSaving() {
		return saving;
	}

	public void setSaving(BigDecimal saving) {
		this.saving = saving;
	}

	public void setFinger(FingerprintTemplate finger) {
		this.finger = finger;
	}	
	
	public BigDecimal getSalaryPerDay() {
		return salaryPerDay;
	}

	public void setSalaryPerDay(BigDecimal salaryPerDay) {
		this.salaryPerDay = salaryPerDay;
	}

	public byte[] getFingerprint() {
		return fingerprint;
	}

	public void setFingerprint(byte[] fingerprint) {
		this.fingerprint = fingerprint;
	}
	
	public String getJoin() {
		return join;
	}

	public void setJoin(String join) {
		this.join = join;
	}

	public String getOut() {
		return out;
	}

	public void setOut(String out) {
		this.out = out;
	}
	
	public BigDecimal getBorrow() {
		return borrow;
	}

	public void setBorrow(BigDecimal borrow) {
		this.borrow = borrow;
	}

	public BigDecimal getDebt() {
		return debt;
	}

	public void setDebt(BigDecimal debt) {
		this.debt = debt;
	}
	

	public BigDecimal getBalance() {
		return balance;
	}

	public void setBalance(BigDecimal balance) {
		this.balance = balance;
	}

	public BigDecimal getUmk() {
		return umk;
	}

	public void setUmk(BigDecimal umk) {
		this.umk = umk;
	}

	@Override
	public String toString() {
		return name;
	}	
}
