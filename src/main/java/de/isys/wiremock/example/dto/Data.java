package de.isys.wiremock.example.dto;

public class Data {

	private int id;
	private String sampleContent;

	public Data(int id, String sampleContent) {
		this.id = id;
		this.sampleContent = sampleContent;
	}

	public Data() {
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getSampleContent() {
		return sampleContent;
	}

	public void setSampleContent(String sampleContent) {
		this.sampleContent = sampleContent;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		result = prime * result
				+ ((sampleContent == null) ? 0 : sampleContent.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final Data other = (Data) obj;
		if (id != other.id)
			return false;
		if (sampleContent == null) {
			if (other.sampleContent != null)
				return false;
		} else if (!sampleContent.equals(other.sampleContent))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Data [id=" + id + ", sampleContent=" + sampleContent + "]";
	}

}
