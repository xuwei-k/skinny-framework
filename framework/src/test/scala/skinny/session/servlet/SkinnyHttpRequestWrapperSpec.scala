package skinny.session.servlet

import javax.servlet.http.HttpServletRequest

import org.scalatest._
import org.scalatestplus.mockito.MockitoSugar
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class SkinnyHttpRequestWrapperSpec extends AnyFlatSpec with Matchers with MockitoSugar {

  it should "work" in {
    val underlying = mock[HttpServletRequest]
    val session    = null
    val req        = SkinnyHttpRequestWrapper(underlying, session)
    req.getSession(true) should equal(session)
    req.getSession should equal(session)
    req.getAuthType should equal(underlying.getAuthType)
    req.getAuthType should equal(underlying.getAuthType)
    req.getCookies should equal(underlying.getCookies)
    req.getDateHeader("foo") should equal(underlying.getDateHeader("foo"))
    req.getHeader("foo") should equal(underlying.getHeader("foo"))
    req.getHeaders("foo") should equal(underlying.getHeaders("foo"))
    req.getHeaderNames should equal(underlying.getHeaderNames)
    req.getIntHeader("foo") should equal(underlying.getIntHeader("foo"))
    req.getMethod should equal(underlying.getMethod)
    req.getPathInfo should equal(underlying.getPathInfo)
    req.getPathTranslated should equal(underlying.getPathTranslated)
    req.getContextPath should equal(underlying.getContextPath)
    req.getQueryString should equal(underlying.getQueryString)
    req.getRemoteUser should equal(underlying.getRemoteUser)
    req.isUserInRole("foo") should equal(underlying.isUserInRole("foo"))
    req.getUserPrincipal should equal(underlying.getUserPrincipal)
    req.getRequestedSessionId should equal(underlying.getRequestedSessionId)
    req.getRequestURI should equal(underlying.getRequestURI)
    req.getRequestURL should equal(underlying.getRequestURL)
    req.getServletPath should equal(underlying.getServletPath)
    req.isRequestedSessionIdValid should equal(underlying.isRequestedSessionIdValid)
    req.isRequestedSessionIdFromCookie should equal(underlying.isRequestedSessionIdFromCookie)
    req.isRequestedSessionIdFromURL should equal(underlying.isRequestedSessionIdFromURL)
    req.isRequestedSessionIdFromUrl should equal(underlying.isRequestedSessionIdFromUrl)
    req.authenticate(null) should equal(underlying.authenticate(null))
    req.login("foo", "bar") should equal(underlying.login("foo", "bar"))
    req.logout should equal(underlying.logout)
    req.getParts should equal(underlying.getParts)
    req.getPart("foo") should equal(underlying.getPart("foo"))
    req.getAttribute("foo") should equal(underlying.getAttribute("foo"))
    req.getAttributeNames should equal(underlying.getAttributeNames)
    req.getCharacterEncoding should equal(underlying.getCharacterEncoding)
    req.setCharacterEncoding("dev") should equal(underlying.setCharacterEncoding("dev"))
    req.setCharacterEncoding("dev") should equal(underlying.setCharacterEncoding("dev"))
    req.getContentLength should equal(underlying.getContentLength)
    req.getContentType should equal(underlying.getContentType)
    req.getInputStream should equal(underlying.getInputStream)
    req.getParameter("foo") should equal(underlying.getParameter("foo"))
    req.getParameterNames should equal(underlying.getParameterNames)
    req.getParameterValues("foo") should equal(underlying.getParameterValues("foo"))
    req.getParameterMap should equal(underlying.getParameterMap)
    req.getProtocol should equal(underlying.getProtocol)
    req.getScheme should equal(underlying.getScheme)
    req.getServerName should equal(underlying.getServerName)
    req.getServerPort should equal(underlying.getServerPort)
    req.getReader should equal(underlying.getReader)
    req.getRemoteAddr should equal(underlying.getRemoteAddr)
    req.getRemoteHost should equal(underlying.getRemoteHost)
    req.setAttribute("foo", "bar") should equal(underlying.setAttribute("foo", "bar"))
    req.removeAttribute("foo") should equal(underlying.removeAttribute("foo"))
    req.getLocale should equal(underlying.getLocale)
    req.getLocales should equal(underlying.getLocales)
    req.isSecure should equal(underlying.isSecure)
    req.getRequestDispatcher("/") should equal(underlying.getRequestDispatcher("/"))
    req.getRealPath("/") should equal(underlying.getRealPath("/"))
    req.getRemotePort should equal(underlying.getRemotePort)
    req.getLocalName should equal(underlying.getLocalName)
    req.getLocalAddr should equal(underlying.getLocalAddr)
    req.getLocalPort should equal(underlying.getLocalPort)
    req.getServletContext should equal(underlying.getServletContext)
    req.startAsync should equal(underlying.startAsync)
    req.startAsync(null, null) should equal(underlying.startAsync(null, null))
    req.isAsyncStarted should equal(underlying.isAsyncStarted)
    req.isAsyncSupported should equal(underlying.isAsyncSupported)
    req.getAsyncContext should equal(underlying.getAsyncContext)
    req.getDispatcherType should equal(underlying.getDispatcherType)
  }

}
