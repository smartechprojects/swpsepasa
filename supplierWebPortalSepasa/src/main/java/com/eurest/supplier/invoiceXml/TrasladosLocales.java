package com.eurest.supplier.invoiceXml;

import javax.xml.bind.annotation.XmlAttribute;

public class TrasladosLocales {
    
    private String impLocTrasladado;
    private String tasadeTraslado;
    private String importe;

    public String getImpLocTrasladado() {
        return impLocTrasladado;
    }

    @XmlAttribute(name = "ImpLocTrasladado")
    public void setImpLocTrasladado(String impLocTrasladado) {
        this.impLocTrasladado = impLocTrasladado;
    }

    public String getTasadeTraslado() {
        return tasadeTraslado;
    }

    @XmlAttribute(name = "TasadeTraslado")
    public void setTasadeTraslado(String tasadeTraslado) {
        this.tasadeTraslado = tasadeTraslado;
    }

    public String getImporte() {
        return importe;
    }

    @XmlAttribute(name = "Importe")
    public void setImporte(String importe) {
        this.importe = importe;
    }
}
