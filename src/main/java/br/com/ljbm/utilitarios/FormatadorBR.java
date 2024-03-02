/**
 * 
 */
package br.com.ljbm.utilitarios;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * @author guest
 * 
 */
public class FormatadorBR {

	private static final DateFormat dateFormat = new SimpleDateFormat(
			"dd/MM/yyyy");
	private static final DateFormat dateFormatYyyyMMdd = new SimpleDateFormat(
			"yyyyMMdd");
	
	private static final DateTimeFormatter formataDataBR = DateTimeFormatter.ofPattern("dd/MM/yyyy");

	public static String formataDataCurta(Calendar calendario) {

		return dateFormat.format(calendario.getTime());
	}

	public static String formataDecimal(BigDecimal valor, int casas) {

		BigDecimal formatado = valor.setScale(casas, RoundingMode.DOWN);
		return formatado.toString();
	}

	public static String formataDecimal(BigDecimal valor) {

		return valor.toString();
	}

	public static String formataMoeda(BigDecimal valor) {

		// return numberFormat.format(valor.doubleValue());
		return formataDecimal(valor, 2);
	}

	public static Calendar paraCalendario(String valor) throws ParseException {

		Date d = dateFormat.parse(valor);
		Calendar c = new GregorianCalendar();
		c.setTime(d);
		return c;
	}

	public static Calendar paraCalendarioyyyyMMdd(String valor)
			throws ParseException {

		Date d = dateFormatYyyyMMdd.parse(valor);
		Calendar c = new GregorianCalendar();
		c.setTime(d);
		return c;
	}

	public static BigDecimal paraBigDecimal(String valor)  {

		String v = valor.replaceAll("\\.", "").replaceAll(",", ".");
		return new BigDecimal(v);
	}

	public static LocalDate paraLocalDate(String data_ddMMyyyy) {
		return LocalDate.from(formataDataBR.parse(data_ddMMyyyy));
	}
}
