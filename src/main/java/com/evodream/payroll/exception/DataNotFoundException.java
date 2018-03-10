package com.evodream.payroll.exception;

public class DataNotFoundException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2947838254841153243L;
	
	public DataNotFoundException(String message) {
		super(message); 
	}
}
