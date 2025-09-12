package com.eurest.supplier.edi;

import java.util.List;

public class VoucherHeaderDTO
{
	
	private String SYEDTY;
	private int SYEDSQ;
	private String SYEKCO;
	private int SYEDOC;
	private String SYEDCT;
	private double SYEDLN;
	private String SYEDST;
	private String SYEDER;
	private String SYKCOO;
	private int SYDOCO;
	private String SYDCTO;
	private String SYSFXO;
	private String SYVR01;
	private String SYVR02;
	private String SYURRF;
	private int SYAN8;
	private String SYDGJ;
	private String SYDIVJ;
	private String SYVINV;
	private int SYSHAN;
	private String SYMCU;
	private String TORG;
	private String USER;
	private String PID;
	private String JOBN;
	private String UPMJ;
	private int TDAY;
	private String CRRM;
	private String SYCRCD;
	private double SYCRR;
	private int SYDDU;
	private int SYDDJ;
	private String SYEDBT;
	
	private List<VoucherDetailDTO> voucherDetailDTO;
	private VoucherSummaryDTO voucherSummaryDTO;
	
	public String getSYEDTY() {
		return SYEDTY;
	}
	public void setSYEDTY(String sYEDTY) {
		SYEDTY = sYEDTY;
	}
	public int getSYEDSQ() {
		return SYEDSQ;
	}
	public void setSYEDSQ(int sYEDSQ) {
		SYEDSQ = sYEDSQ;
	}
	public String getSYEKCO() {
		return SYEKCO;
	}
	public void setSYEKCO(String sYEKCO) {
		SYEKCO = sYEKCO;
	}
	public int getSYEDOC() {
		return SYEDOC;
	}
	public void setSYEDOC(int sYEDOC) {
		SYEDOC = sYEDOC;
	}
	public String getSYEDCT() {
		return SYEDCT;
	}
	public void setSYEDCT(String sYEDCT) {
		SYEDCT = sYEDCT;
	}
	public double getSYEDLN() {
		return SYEDLN;
	}
	public void setSYEDLN(double sYEDLN) {
		SYEDLN = sYEDLN;
	}
	public String getSYEDST() {
		return SYEDST;
	}
	public void setSYEDST(String sYEDST) {
		SYEDST = sYEDST;
	}
	public String getSYEDER() {
		return SYEDER;
	}
	public void setSYEDER(String sYEDER) {
		SYEDER = sYEDER;
	}
	public String getSYKCOO() {
		return SYKCOO;
	}
	public void setSYKCOO(String sYKCOO) {
		SYKCOO = sYKCOO;
	}
	public int getSYDOCO() {
		return SYDOCO;
	}
	public void setSYDOCO(int sYDOCO) {
		SYDOCO = sYDOCO;
	}
	public String getSYDCTO() {
		return SYDCTO;
	}
	public void setSYDCTO(String sYDCTO) {
		SYDCTO = sYDCTO;
	}
	public String getSYSFXO() {
		return SYSFXO;
	}
	public void setSYSFXO(String sYSFXO) {
		SYSFXO = sYSFXO;
	}
	public int getSYAN8() {
		return SYAN8;
	}
	public void setSYAN8(int sYAN8) {
		SYAN8 = sYAN8;
	}
	public String getSYDGJ() {
		return SYDGJ;
	}
	public void setSYDGJ(String sYDGJ) {
		SYDGJ = sYDGJ;
	}
	public String getSYDIVJ() {
		return SYDIVJ;
	}
	public void setSYDIVJ(String sYDIVJ) {
		SYDIVJ = sYDIVJ;
	}
	public String getSYVINV() {
		return SYVINV;
	}
	public void setSYVINV(String sYVINV) {
		SYVINV = sYVINV;
	}
	public int getSYSHAN() {
		return SYSHAN;
	}
	public void setSYSHAN(int sYSHAN) {
		SYSHAN = sYSHAN;
	}
	public String getSYMCU() {
		return SYMCU;
	}
	public void setSYMCU(String sYMCU) {
		SYMCU = sYMCU;
	}
	public String getTORG() {
		return TORG;
	}
	public void setTORG(String tORG) {
		TORG = tORG;
	}
	public String getUSER() {
		return USER;
	}
	public void setUSER(String uSER) {
		USER = uSER;
	}
	public String getPID() {
		return PID;
	}
	public void setPID(String pID) {
		PID = pID;
	}
	public String getJOBN() {
		return JOBN;
	}
	public void setJOBN(String jOBN) {
		JOBN = jOBN;
	}
	public String getUPMJ() {
		return UPMJ;
	}
	public void setUPMJ(String uPMJ) {
		UPMJ = uPMJ;
	}
	public int getTDAY() {
		return TDAY;
	}
	public void setTDAY(int tDAY) {
		TDAY = tDAY;
	}
	public List<VoucherDetailDTO> getVoucherDetailDTO() {
		return voucherDetailDTO;
	}
	public void setVoucherDetailDTO(List<VoucherDetailDTO> voucherDetailDTO) {
		this.voucherDetailDTO = voucherDetailDTO;
	}
	public VoucherSummaryDTO getVoucherSummaryDTO() {
		return voucherSummaryDTO;
	}
	public void setVoucherSummaryDTO(VoucherSummaryDTO voucherSummaryDTO) {
		this.voucherSummaryDTO = voucherSummaryDTO;
	}
	public String getSYVR01() {
		return SYVR01;
	}
	public void setSYVR01(String sYVR01) {
		SYVR01 = sYVR01;
	}
	public String getSYVR02() {
		return SYVR02;
	}
	public void setSYVR02(String sYVR02) {
		SYVR02 = sYVR02;
	}
	public String getSYURRF() {
		return SYURRF;
	}
	public void setSYURRF(String sYURRF) {
		SYURRF = sYURRF;
	}
	public String getCRRM() {
		return CRRM;
	}
	public void setCRRM(String cRRM) {
		CRRM = cRRM;
	}
	public String getSYCRCD() {
		return SYCRCD;
	}
	public void setSYCRCD(String sYCRCD) {
		SYCRCD = sYCRCD;
	}
	public double getSYCRR() {
		return SYCRR;
	}
	public void setSYCRR(double sYCRR) {
		SYCRR = sYCRR;
	}
	public int getSYDDU() {
		return SYDDU;
	}
	public void setSYDDU(int sYDDU) {
		SYDDU = sYDDU;
	}
	public int getSYDDJ() {
		return SYDDJ;
	}
	public void setSYDDJ(int sYDDJ) {
		SYDDJ = sYDDJ;
	}	
	public String getSYEDBT() {
		return SYEDBT;
	}
	public void setSYEDBT(String sYEDBT) {
		SYEDBT = sYEDBT;
	}	
}
