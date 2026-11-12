package com.m2i.pti.reporting;

import org.springframework.web.servlet.view.jasperreports.*;

public class ReportUtils {
    public static AbstractJasperReportsView getJasperView(ReportFormat format) {
        AbstractJasperReportsView view;
        switch (format) {
            case XLSX:
                view = new JasperReportsXlsxView();
                break;
            case CSV:
                view = new JasperReportsCsvView();
                break;
            case HTML:
                view = new JasperReportsHtmlView();
                break;
            case PDF:
                view = new JasperReportsPdfView();
                break;
            default:
                view = new JasperReportsPdfView();
                break;
        }
        return view;
    }

}
