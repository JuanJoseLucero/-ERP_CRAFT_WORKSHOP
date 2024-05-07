package com.cjconfecciones.utils;

import java.io.ByteArrayInputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.cjconfecciones.pojo.DetailBill;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletResponse;
import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.JasperRunManager;
import net.sf.jasperreports.engine.util.JRLoader;

@Named
@RequestScoped
public class GenerateReport {

	private Logger log = Logger.getLogger(GenerateReport.class.getName());
	
	public void generateReport(String nameReport, String pathToJasper, Map<String, Object> parameters) {
		OutputStream out = null;
		JasperReport jasperReport = null;
		JREmptyDataSource empthyds = new JREmptyDataSource();
		FacesContext context = FacesContext.getCurrentInstance();
		HttpServletResponse response = (HttpServletResponse) context.getExternalContext().getResponse();

		try {
			jasperReport = (JasperReport) JRLoader
					.loadObjectFromFile(pathToJasper);
			// ByteArrayInputStream fichero = null;
			// parametros.put("SUBREPORT_DIR", dirreporte);
			ByteArrayInputStream fichero = new ByteArrayInputStream(
					JasperRunManager.runReportToPdf(jasperReport, parameters, empthyds));

			String contentType = "application/vnd.ms-pdf";

			String headerContentPrefijo = "Content-disposition";
			String headerContentSufijo = "inline; filename=".concat(nameReport);

			String headerCachePrefijo = "Cache-Control";
			String headerCacheSufijo = "max-age=30";

			String headerPragmaPrefijo = "Pragma";
			String headerPragmaSufijo = "No-cache";

			String dateHeaderPrefijo = "Expires";
			long dateHeaderSufijo = 0;

			response.setContentType(contentType);
			response.setHeader(headerContentPrefijo, headerContentSufijo);
			response.setHeader(headerCachePrefijo, headerCacheSufijo);
			response.setHeader(headerPragmaPrefijo, headerPragmaSufijo);
			response.setDateHeader(dateHeaderPrefijo, dateHeaderSufijo);

			out = response.getOutputStream();
			// Escribir en bloques y no todo el contenido
			byte[] buf = new byte[1024];
			int len;
			while ((len = fichero.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
			fichero.close();
			out.flush();
			out.close();
			context.renderResponse();
			context.responseComplete();

		} catch (Exception e) {
			log.log(Level.SEVERE, "ERROR TO GENERATE REPORT ",e);
		}

	}
}
