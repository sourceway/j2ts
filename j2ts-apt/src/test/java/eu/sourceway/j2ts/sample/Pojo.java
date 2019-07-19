package eu.sourceway.j2ts.sample;

import java.util.Date;

import eu.sourceway.j2ts.annotations.J2TsProperty;
import eu.sourceway.j2ts.annotations.J2TsType;

@J2TsType
public class Pojo {

	private long id;
	private String name;
	private String password;

	private Date birthday;
	private Date dateAsNumber;

	private AnotherPojo anotherPojo;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Date getBirthday() {
		return birthday;
	}

	public void setBirthday(Date birthday) {
		this.birthday = birthday;
	}

	@J2TsProperty(type = "number")
	public Date getDateAsNumber() {
		return dateAsNumber;
	}

	public void setDateAsNumber(Date dateAsNumber) {
		this.dateAsNumber = dateAsNumber;
	}

	public AnotherPojo getAnotherPojo() {
		return anotherPojo;
	}

	public void setAnotherPojo(AnotherPojo anotherPojo) {
		this.anotherPojo = anotherPojo;
	}
}
