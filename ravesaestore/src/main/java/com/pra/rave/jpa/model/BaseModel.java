package com.pra.rave.jpa.model;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.MappedSuperclass;
import javax.persistence.TableGenerator;
import javax.persistence.Version;

@MappedSuperclass
@Inheritance(strategy=InheritanceType.TABLE_PER_CLASS)
public abstract class BaseModel implements java.io.Serializable {
	private static final long serialVersionUID = 5995725622016781052L;

	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "idGenerator")
	@TableGenerator(name = "idGenerator", allocationSize = 1)
	@Column(name="Id", updatable=false)
	protected int Id = -1;

	@Column(name="Updated_Dtc", nullable=false)
	@Version
	protected Timestamp updtTime = null;

	public int getId() {
		return Id;
	}
	public Timestamp getUpdtTime() {
		return updtTime;
	}
	public void setUpdtTime(Timestamp updtTime) {
		this.updtTime = updtTime;
	}
}
