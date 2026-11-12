package com.m2i.pti.web.rest.errors;

import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Custom, parameterized exception, which can be translated on the client side.
 * For example:
 *
 * <pre>
 * throw new CustomParameterizedException(&quot;myCustomError&quot;, &quot;hello&quot;, &quot;world&quot;);
 * </pre>
 *
 * Can be translated with:
 *
 * <pre>
 * "error.myCustomError" :  "The server says {{param0}} to {{param1}}"
 * </pre>
 */
public class CustomSettlementException extends RuntimeException {
   
    private static final long serialVersionUID = 1L;

    private static final String PARAM = "param";

    private final String message;
 private final Logger log = LoggerFactory.getLogger(CustomSettlementException.class);
    private final Map<String, String> paramMap = new HashMap<>();
    public CustomSettlementException(String message, String... params) {
        super(message);
        this.message = message;
        if (params != null && params.length > 0) {
            for (int i = 0; i < params.length; i++) {
                paramMap.put(PARAM + i, params[i]);
            }
        }
      //  this.messageProducer.sendMessage(message, "error");
        log.error(message);
    }

    public CustomSettlementException(String message, Map<String, String> paramMap) {
        super(message);
        this.message = message;
        this.paramMap.putAll(paramMap);
        log.error(message);
       //  this.messageProducer.sendMessage(message, "error");
    }

    public ParameterizedErrorVM getErrorVM() {
        return new ParameterizedErrorVM(message, paramMap);
    }
}
