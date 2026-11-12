package org.springframework.web.servlet.view.jasperreports;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.view.AbstractView;

import java.util.Map;

/**
 * Spring a retiré ces vues. Le type reste pour compiler les états existants.
 */
public abstract class AbstractJasperReportsView extends AbstractView {

    private String url;

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    @Override
    protected void renderMergedOutputModel(Map<String, Object> model, HttpServletRequest request, HttpServletResponse response) {
        throw new UnsupportedOperationException("Rendu Jasper non porté sur Boot 3");
    }
}
