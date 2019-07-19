package eu.sourceway.j2ts.sample;

import java.util.Date;

import eu.sourceway.j2ts.annotations.J2TsProperty;
import eu.sourceway.j2ts.annotations.J2TsType;

@J2TsType(name = "AwesomePojo")
public class AnotherPojo {

	private long id;
	private String name;
	private String password;

	private Date birthday;

	private Pojo pojo;

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

	@J2TsProperty(optional = true)
	public Pojo getPojo() {
		return pojo;
	}

	public void setPojo(Pojo pojo) {
		this.pojo = pojo;
	}
}
