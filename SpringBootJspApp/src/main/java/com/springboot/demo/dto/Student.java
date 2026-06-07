package com.springboot.demo.dto;

public class Student {

	private String htno;
	private String name;
	private String collegecode;
	private int branchcode;

	public String getHtno() {
		return htno;
	}

	public void setHtno(String htno) {
		this.htno = htno;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCollegecode() {
		return collegecode;
	}

	public void setCollegecode(String collegecode) {
		this.collegecode = collegecode;
	}

	public int getBranchcode() {
		return branchcode;
	}

	public void setBranchcode(int branchcode) {
		this.branchcode = branchcode;
	}

}
