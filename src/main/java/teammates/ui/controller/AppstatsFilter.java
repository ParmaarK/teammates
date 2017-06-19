package teammates.ui.controller;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import teammates.common.util.Config;
import teammates.common.util.Const;

/**
 * Custom filter which can exclude Appstats calculcation for certain conditions, e.g URL patterns.
 */
public class AppstatsFilter extends com.google.appengine.tools.appstats.AppstatsFilter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (!(request instanceof HttpServletRequest)) {
            chain.doFilter(request, response);
            return;
        }

        // Actions which write data to Google Cloud Storage needs to be excluded in dev server
        // due to a bug in App Engine GCS client library.
        // The bug occurs for App Engine SDK 1.9.28 and above, highly likely due to the removed Files API.
        // The bug does not occur in production server.

        String url = ((HttpServletRequest) request).getRequestURI();
        boolean isUrlForActionWritingDataToGcs =
                url.equals(Const.ActionURIs.STUDENT_PROFILE_PICTURE_UPLOAD)
                || url.equals(Const.ActionURIs.STUDENT_PROFILE_PICTURE_EDIT);
        if (Config.isDevServer() && isUrlForActionWritingDataToGcs) {
            chain.doFilter(request, response);
            return;
        }

        super.doFilter(request, response, chain);
    }

}
