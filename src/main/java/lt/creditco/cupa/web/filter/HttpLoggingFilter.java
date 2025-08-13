package lt.creditco.cupa.web.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import lt.creditco.cupa.service.AuditLogService;
import org.apache.commons.io.output.TeeOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class HttpLoggingFilter implements Filter {

    public static final int BODY_LENGTH_LIMIT = 5000;

    private static final Logger log = LoggerFactory.getLogger(HttpLoggingFilter.class);

    private final AuditLogService auditLogService;

    public HttpLoggingFilter(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    public boolean skipLoggingFor(String servletPath) {
        // 	log.debug("Check skipLoggingFor({})", servletPath);
        if ("/".equals(servletPath)) return true;
        if ("/manifest.webapp".equals(servletPath)) return true;
        if ("/api/profile-info".equals(servletPath)) return true;
        if ("/v3/api-docs".equals(servletPath)) return true;
        if (servletPath.matches("(.*)(\\.)html$")) return true;
        if (servletPath.matches("(.*)(\\.)js$")) return true;
        if (servletPath.matches("(.*)(\\.)css$")) return true;
        if (servletPath.matches("(.*)(\\.)xlsx$")) return true;
        if (servletPath.matches("(.*)(\\.)txt$")) return true;
        if (servletPath.matches("(.*)(\\.)json$")) return true;
        if (servletPath.matches("(.*)(\\.)png$")) return true;
        if (servletPath.matches("(.*)(\\.)jpeg$")) return true;
        if (servletPath.matches("(.*)(\\.)jpg$")) return true;
        if (servletPath.matches("(.*)(\\.)ico$")) return true;
        if (servletPath.matches("(.*)(\\.)bmp$")) return true;
        if (servletPath.matches("(.*)(\\.)less$")) return true;
        if (servletPath.matches("(.*)(\\.)map$")) return true;
        if (servletPath.matches("/management/(.*)")) return true;
        if (servletPath.matches("/swagger(.*)")) return true;
        if (servletPath.matches("/api/account(.*)")) return true;
        if (servletPath.matches("/api/admin(.*)")) return true;
        if (servletPath.matches("/api/users(.*)")) return true;
        if (servletPath.matches("/api/action-logs(.*)")) return true;
        if (servletPath.matches("/api/endpoint-wirings(.*)")) return true;
        if (servletPath.matches("/bower_components/(.*)")) return true;
        if (servletPath.matches("/v3/api-docs/(.*)")) return true;
        return false;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        try {
            HttpServletRequest httpServletRequest = (HttpServletRequest) request;
            HttpServletResponse httpServletResponse = (HttpServletResponse) response;
            boolean logRequest = !skipLoggingFor(httpServletRequest.getServletPath());

            Map<String, String> requestMap = this.getTypesafeRequestMap(httpServletRequest);
            BufferedRequestWrapper bufferedRequest = new BufferedRequestWrapper(httpServletRequest);
            BufferedResponseWrapper bufferedResponse = new BufferedResponseWrapper(httpServletResponse);

            // Create ApiRequestDetails object to collect request and response data
            ApiRequestDetails apiRequestDetails = null;
            if (logRequest) {
                apiRequestDetails = new ApiRequestDetails(httpServletRequest, requestMap, bufferedRequest);
            }

            chain.doFilter(bufferedRequest, bufferedResponse);

            if (logRequest && apiRequestDetails != null) {
                // Set response details
                apiRequestDetails.setResponseDetails(bufferedResponse);

                // Log using the collected data
                log.debug(apiRequestDetails.buildLogMessage());

                // TODO: In the next step, add call to auditLoggingService.saveApiRequestDetails(apiRequestDetails);
                String responseId = httpServletResponse.getHeader("X-Response-Id");
                if (responseId != null) {
                    Long responseIdLong = Long.parseLong(responseId);
                    auditLogService.updateAuditLogWithResponse(responseIdLong, apiRequestDetails);
                }
            }
        } catch (Throwable a) {
            if (a.getMessage() == null) log.error("Unknown error:", a);
            else {
                log.debug("===================================================================");
                log.error(
                    "Error processing request {} {}",
                    ((HttpServletRequest) request).getMethod(),
                    ((HttpServletRequest) request).getRequestURL().toString()
                );
                log.error(a.getMessage(), a);
                log.debug("Request: {}", request);
                log.debug("Response: {}", response);
                log.debug("===================================================================");
            }
        }
    }

    private Map<String, String> getTypesafeRequestMap(HttpServletRequest request) {
        Map<String, String> typesafeRequestMap = new HashMap<String, String>();
        Enumeration<?> requestParamNames = request.getParameterNames();
        while (requestParamNames.hasMoreElements()) {
            String requestParamName = (String) requestParamNames.nextElement();
            String requestParamValue;
            if (requestParamName.equalsIgnoreCase("password")) {
                requestParamValue = "********";
            } else {
                requestParamValue = request.getParameter(requestParamName);
            }
            typesafeRequestMap.put(requestParamName, requestParamValue);
        }
        return typesafeRequestMap;
    }

    @Override
    public void destroy() {}

    /**
     * Internal class to collect API request and response details for logging and audit purposes.
     */
    public static final class ApiRequestDetails {

        private final String username;
        private final String httpMethod;
        private final String servletPath;
        private final Map<String, String> requestParameters;
        private final String requestBody;
        private final String remoteAddress;
        private Integer responseStatus;
        private String responseDescription;
        private String responseBody;

        public ApiRequestDetails(HttpServletRequest request, Map<String, String> requestParameters, BufferedRequestWrapper bufferedRequest)
            throws IOException {
            this.username = request.getUserPrincipal() == null ? null : request.getUserPrincipal().getName();
            this.httpMethod = request.getMethod();
            this.servletPath = request.getServletPath();
            this.requestParameters = requestParameters;
            this.requestBody = bufferedRequest.getRequestBody();
            this.remoteAddress = request.getRemoteAddr();
        }

        public void setResponseDetails(BufferedResponseWrapper bufferedResponse) {
            this.responseStatus = bufferedResponse.getStatus();
            this.responseBody = bufferedResponse.getContent();

            String errorMessage = bufferedResponse.getHeader("X-Error-Title");
            this.responseDescription = errorMessage != null ? errorMessage : getResponseDescription(bufferedResponse.getStatus());
        }

        private String getResponseDescription(int statusCode) {
            return switch (statusCode) {
                case 200 -> "OK";
                case 201 -> "Created";
                case 400 -> "Bad Request";
                case 401 -> "Unauthorized";
                case 403 -> "Forbidden";
                case 404 -> "Not Found";
                case 500 -> "Internal Server Error";
                default -> "Status " + statusCode;
            };
        }

        public String getUsername() {
            return username;
        }

        public String getHttpMethod() {
            return httpMethod;
        }

        public String getServletPath() {
            return servletPath;
        }

        public Map<String, String> getRequestParameters() {
            return requestParameters;
        }

        public String getRequestBody() {
            return requestBody;
        }

        public String getRemoteAddress() {
            return remoteAddress;
        }

        public Integer getResponseStatus() {
            return responseStatus;
        }

        public String getResponseDescription() {
            return responseDescription;
        }

        public String getResponseBody() {
            return responseBody;
        }

        /**
         * Builds the log message string in the same format as the original implementation.
         */
        public String buildLogMessage() {
            StringBuilder logMessage = new StringBuilder();
            logMessage
                .append("REST Request - ")
                .append("[USERNAME:")
                .append(username)
                .append("]")
                .append(" [HTTP METHOD:")
                .append(httpMethod)
                .append("]")
                .append(" [PATH INFO:")
                .append(servletPath)
                .append("]")
                .append(" [REQUEST PARAMETERS:")
                .append(requestParameters)
                .append("]")
                .append(" [REQUEST BODY:")
                .append(requestBody)
                .append("]")
                .append(" [REMOTE ADDRESS:")
                .append(remoteAddress)
                .append("]");

            if (responseStatus != null) {
                logMessage.append(" [RESPONSE STATUS:").append(responseStatus).append("]");
            }
            if (responseBody != null) {
                logMessage.append(" [RESPONSE BODY:").append(responseBody).append("]");
            }

            return logMessage.toString();
        }
    }

    private static final class BufferedRequestWrapper extends HttpServletRequestWrapper {

        private ByteArrayInputStream bais = null;
        private ByteArrayOutputStream baos = null;
        private BufferedServletInputStream bsis = null;
        private byte[] buffer = null;

        public BufferedRequestWrapper(HttpServletRequest req) throws IOException {
            super(req);
            // Read InputStream and store its content in a buffer with length limit.
            InputStream is = req.getInputStream();
            this.baos = new ByteArrayOutputStream();
            byte buf[] = new byte[1024];
            int read;
            int totalRead = 0;
            while ((read = is.read(buf)) > 0) {
                if (totalRead + read > BODY_LENGTH_LIMIT) {
                    // Only read up to the limit
                    int remainingBytes = BODY_LENGTH_LIMIT - totalRead;
                    this.baos.write(buf, 0, remainingBytes);
                    totalRead = BODY_LENGTH_LIMIT;
                    break;
                }
                this.baos.write(buf, 0, read);
                totalRead += read;
            }
            this.buffer = this.baos.toByteArray();
        }

        @Override
        public ServletInputStream getInputStream() {
            this.bais = new ByteArrayInputStream(this.buffer);
            this.bsis = new BufferedServletInputStream(this.bais);
            return this.bsis;
        }

        String getRequestBody() throws IOException {
            BufferedReader reader = new BufferedReader(new InputStreamReader(this.getInputStream()));
            String line = null;
            StringBuilder inputBuffer = new StringBuilder();
            int totalLength = 0;
            do {
                line = reader.readLine();
                if (null != line) {
                    String trimmedLine = line.trim();
                    if (totalLength + trimmedLine.length() > BODY_LENGTH_LIMIT) {
                        // Truncate and add indicator
                        int remainingChars = BODY_LENGTH_LIMIT - totalLength;
                        inputBuffer.append(trimmedLine.substring(0, remainingChars));
                        inputBuffer.append("... TRUNCATED ...");
                        break;
                    }
                    inputBuffer.append(trimmedLine);
                    totalLength += trimmedLine.length();
                }
            } while (line != null);
            reader.close();
            return inputBuffer.toString().trim();
        }
    }

    private static final class BufferedServletInputStream extends ServletInputStream {

        private ByteArrayInputStream bais;

        public BufferedServletInputStream(ByteArrayInputStream bais) {
            this.bais = bais;
        }

        @Override
        public int available() {
            return this.bais.available();
        }

        @Override
        public int read() {
            return this.bais.read();
        }

        @Override
        public int read(byte[] buf, int off, int len) {
            return this.bais.read(buf, off, len);
        }

        @Override
        public boolean isFinished() {
            return false;
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setReadListener(ReadListener readListener) {}
    }

    public class TeeServletOutputStream extends ServletOutputStream {

        private final TeeOutputStream targetStream;

        public TeeServletOutputStream(OutputStream one, OutputStream two) {
            targetStream = new TeeOutputStream(one, two);
        }

        @Override
        public void write(int arg0) throws IOException {
            this.targetStream.write(arg0);
        }

        public void flush() throws IOException {
            super.flush();
            if (targetStream != null) this.targetStream.flush();
        }

        public void close() throws IOException {
            super.close();
            if (targetStream != null) this.targetStream.close();
        }

        @Override
        public boolean isReady() {
            return false;
        }

        @Override
        public void setWriteListener(WriteListener writeListener) {}
    }

    public class BufferedResponseWrapper extends HttpServletResponseWrapper {

        HttpServletResponse original;
        TeeServletOutputStream tee;
        ByteArrayOutputStream bos;
        private int totalWritten = 0;

        public BufferedResponseWrapper(HttpServletResponse response) {
            super(response);
            original = response;
        }

        public String getContent() {
            if (bos == null) {
                return "";
            }
            String content = bos.toString();
            if (content.length() > BODY_LENGTH_LIMIT) {
                return content.substring(0, BODY_LENGTH_LIMIT) + "... TRUNCATED ...";
            }
            return content;
        }

        public PrintWriter getWriter() throws IOException {
            return original.getWriter();
        }

        public ServletOutputStream getOutputStream() throws IOException {
            if (tee == null) {
                bos = new ByteArrayOutputStream() {
                    @Override
                    public void write(byte[] b, int off, int len) {
                        if (totalWritten + len > BODY_LENGTH_LIMIT) {
                            // Only write up to the limit
                            int remainingBytes = BODY_LENGTH_LIMIT - totalWritten;
                            super.write(b, off, remainingBytes);
                            totalWritten = BODY_LENGTH_LIMIT;
                        } else {
                            super.write(b, off, len);
                            totalWritten += len;
                        }
                    }

                    @Override
                    public void write(int b) {
                        if (totalWritten < BODY_LENGTH_LIMIT) {
                            super.write(b);
                            totalWritten++;
                        }
                    }
                };
                tee = new TeeServletOutputStream(original.getOutputStream(), bos);
            }
            return tee;
        }

        @Override
        public String getCharacterEncoding() {
            return original.getCharacterEncoding();
        }

        @Override
        public String getContentType() {
            return original.getContentType();
        }

        @Override
        public void setCharacterEncoding(String charset) {
            original.setCharacterEncoding(charset);
        }

        @Override
        public void setContentLength(int len) {
            original.setContentLength(len);
        }

        @Override
        public void setContentLengthLong(long l) {
            original.setContentLengthLong(l);
        }

        @Override
        public void setContentType(String type) {
            original.setContentType(type);
        }

        @Override
        public void setBufferSize(int size) {
            original.setBufferSize(size);
        }

        @Override
        public int getBufferSize() {
            return original.getBufferSize();
        }

        @Override
        public void flushBuffer() throws IOException {
            if (tee != null) tee.flush();
        }

        @Override
        public void resetBuffer() {
            original.resetBuffer();
        }

        @Override
        public boolean isCommitted() {
            return original.isCommitted();
        }

        @Override
        public void reset() {
            original.reset();
        }

        @Override
        public void setLocale(Locale loc) {
            original.setLocale(loc);
        }

        @Override
        public Locale getLocale() {
            return original.getLocale();
        }

        @Override
        public void addCookie(Cookie cookie) {
            original.addCookie(cookie);
        }

        @Override
        public boolean containsHeader(String name) {
            return original.containsHeader(name);
        }

        @Override
        public String encodeURL(String url) {
            return original.encodeURL(url);
        }

        @Override
        public String encodeRedirectURL(String url) {
            return original.encodeRedirectURL(url);
        }

        @Override
        public void sendError(int sc, String msg) throws IOException {
            original.sendError(sc, msg);
        }

        @Override
        public void sendError(int sc) throws IOException {
            original.sendError(sc);
        }

        @Override
        public void sendRedirect(String location) throws IOException {
            original.sendRedirect(location);
        }

        @Override
        public void setDateHeader(String name, long date) {
            original.setDateHeader(name, date);
        }

        @Override
        public void addDateHeader(String name, long date) {
            original.addDateHeader(name, date);
        }

        @Override
        public void setHeader(String name, String value) {
            original.setHeader(name, value);
        }

        @Override
        public void addHeader(String name, String value) {
            original.addHeader(name, value);
        }

        @Override
        public void setIntHeader(String name, int value) {
            original.setIntHeader(name, value);
        }

        @Override
        public void addIntHeader(String name, int value) {
            original.addIntHeader(name, value);
        }

        @Override
        public void setStatus(int sc) {
            original.setStatus(sc);
        }

        @Override
        public String getHeader(String arg0) {
            return original.getHeader(arg0);
        }

        @Override
        public Collection<String> getHeaderNames() {
            return original.getHeaderNames();
        }

        @Override
        public Collection<String> getHeaders(String arg0) {
            return original.getHeaders(arg0);
        }

        @Override
        public int getStatus() {
            return original.getStatus();
        }
    }
}
