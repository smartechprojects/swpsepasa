package com.eurest.supplier.invoiceXml;

import java.util.List;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

public class ImpuestosLocales {

    private String version;
    private String totaldeRetenciones;
    private String totaldeTraslados;
    private List<TrasladosLocales> trasladosLocales;
    private List<RetencionesLocales> retencionesLocales;

    public String getVersion() {
        if (version == null) {
            return "1.0";
        } else {
            return version;
        }
    }

    @XmlAttribute(name = "version")
    public void setVersion(String version) {
        this.version = version;
    }

    public String getTotaldeRetenciones() {
        return totaldeRetenciones;
    }

    @XmlAttribute(name = "TotaldeRetenciones")
    public void setTotaldeRetenciones(String totaldeRetenciones) {
        this.totaldeRetenciones = totaldeRetenciones;
    }

    public String getTotaldeTraslados() {
        return totaldeTraslados;
    }

    @XmlAttribute(name = "TotaldeTraslados")
    public void setTotaldeTraslados(String totaldeTraslados) {
        this.totaldeTraslados = totaldeTraslados;
    }

    @XmlElement(name = "TrasladosLocales", namespace="http://www.sat.gob.mx/implocal")
	public List<TrasladosLocales> getTrasladosLocales() {
		return trasladosLocales;
	}

	public void setTrasladosLocales(List<TrasladosLocales> trasladosLocales) {
		this.trasladosLocales = trasladosLocales;
	}

	@XmlElement(name = "RetencionesLocales", namespace="http://www.sat.gob.mx/implocal")
	public List<RetencionesLocales> getRetencionesLocales() {
		return retencionesLocales;
	}

	public void setRetencionesLocales(List<RetencionesLocales> retencionesLocales) {
		this.retencionesLocales = retencionesLocales;
	}
	
}
