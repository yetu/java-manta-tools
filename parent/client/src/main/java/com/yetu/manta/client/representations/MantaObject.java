package com.yetu.manta.client.representations;

public class MantaObject {

	private String name;
	private String type;
	private String mtime;
	private Long size;
	private String etag;
	private Integer durability;
	
	private String path;

	public boolean isDirectory() {
		return "directory".equals(type);
	}

	public boolean isObject() {
		return "object".equals(type);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getMtime() {
		return mtime;
	}

	public void setMtime(String mtime) {
		this.mtime = mtime;
	}

	public Long getSize() {
		return size;
	}

	public void setSize(Long size) {
		this.size = size;
	}

	public String getEtag() {
		return etag;
	}

	public void setEtag(String etag) {
		this.etag = etag;
	}

	public Integer getDurability() {
		return durability;
	}

	public void setDurability(Integer durability) {
		this.durability = durability;
	}
	
	public String getPath(){
		return path;
	}
}
