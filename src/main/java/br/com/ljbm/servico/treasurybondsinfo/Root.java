package br.com.ljbm.servico.treasurybondsinfo;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper; // version 2.11.1

public class Root {
	@Override
	public String toString() {
		return "Root [responseStatus=" + responseStatus + ", responseStatusText=" + responseStatusText + ", statusInfo="
				+ statusInfo + ", response=" + response + "]";
	}

	public int responseStatus;
	public String responseStatusText;
	public String statusInfo;
	public Response response;

	public static class BdTxTp {
		public int cd;
	}

	public static class BizSts {
		public Object cd;
		public String dtTm;
	}

	public static class BusSegmt {
		public int cd;
		public String nm;
	}

	public static class FinIndxs {
		public int cd;
		public String nm;
	}

	public static class Response {
		@JsonProperty("BdTxTp")
		public BdTxTp bdTxTp;
		@JsonProperty("TrsrBondMkt")
		public TrsrBondMkt trsrBondMkt;
		@JsonProperty("TrsrBdTradgList")
		public ArrayList<TrsrBdTradgList> trsrBdTradgList;
		@JsonProperty("BizSts")
		public BizSts bizSts;
	}

	public static class TrsrBd {
		public int cd;
		public String nm;
		public String featrs;
		public Date mtrtyDt;
		public double minInvstmtAmt;
		public double untrInvstmtVal;
		public String invstmtStbl;
		public boolean semiAnulIntrstInd;
		public String rcvgIncm;
		public double anulInvstmtRate;
		public double anulRedRate;
		public double minRedQty;
		public double untrRedVal;
		public double minRedVal;
		public String isinCd;
		@JsonProperty("FinIndxs")
		public FinIndxs finIndxs;
		public Object wdwlDt;
		public Date convDt;
		@JsonProperty("BusSegmt")
		public BusSegmt busSegmt;
		public int amortQuotQty;
	}

	public static class TrsrBdTradgList {
		@JsonProperty("TrsrBd")
		public TrsrBd trsrBd;
		@JsonProperty("TrsrBdType")
		public TrsrBdType trsrBdType;
		@JsonProperty("SelicCode")
		public int selicCode;
	}

	public static class TrsrBdType {
		public int cd;
		public String nm;
		public Object ctdyRate;
		public Object grPr;
	}

	public static class TrsrBondMkt {
		public Date opngDtTm;
		public Date clsgDtTm;
		public Date qtnDtTm;
		public int stsCd;
		public String sts;
	}

	public static void main(String[] args) throws Exception {

		ObjectMapper om = new ObjectMapper();
		String myJsonString = Files.readString(
				Paths.get("C:\\Users\\luciana\\OneDrive\\luc&luca\\luciano\\git\\investimentos.bb\\src\\main\\resources\\static\\data.json"), StandardCharsets.UTF_8);
		Root root = om.readValue(myJsonString, Root.class); 

		System.out.println(root.toString());
	}
}