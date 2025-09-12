<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<c:set var="url" value="${pageContext.request.contextPath}"></c:set>
<html>
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
  <link rel="shortcut icon" href="resources/images/favicon.ico"/>
  <title>SEPASA&copy;-Nuevo registro</title>
<link rel="stylesheet" type="text/css"
	href="${url}/ext-4.2.1/resources/css/ext-all-gray.css">
<script type="text/javascript" charset="utf-8"
	src="${url}/ext-4.2.1/ext-all.js"></script>
	
	<link rel="stylesheet" type="text/css"
	href="${url}/resources/css/app.css">

			<script type="text/javascript" charset="utf-8"
	src="${url}/ext-4.2.1/locale/ext-lang-es.js"></script>
	
	<script type="text/javascript" charset="utf-8"
	src="${url}/resources/js/ActionButtonColumn.js"></script>
<script type="text/javascript" charset="utf-8" src="${url}/app/publicApp.js"></script>
<script type="text/javascript" charset="utf-8"
	src="${url}/resources/js/common.js"></script>
	<script type="text/javascript" charset="utf-8"
	src="${url}/resources/js/MultiSelect.js"></script>
	


<script>
	var role = 'ANONYMOUS';
	var sessionId = '';
	
</script>

</head>
<body>
<table height='100%' width='100%' class='hdrTable'> 
            		<tr>
            		  <td style='width:60%;'>
            		     <table height='50%' width='100%'>
            		      <tr><td style='width:200px;'> &nbsp;</td>
            		          <td rowspan=2 style='font-size:26px;border-right:1px solid #000;color:#000;padding-bottom:7px;'>&nbsp;</td>
           		          <td  rowspan=2 style='width:500px;text-align:left;color:#000;font-size:17px;'>&nbsp;&nbsp;&nbsp;REGISTRO DE NUEVOS PROVEEDORES</td>
            		          </tr>
            		      </table>
            		  </td>
                     <td width=65% style='text-align:right;padding-right:50px;vertical-align:middle;font: 28px Tahoma, Verdana, sans-serif; color:#000;'>
	                      
	                      <a href='${pageContext.request.contextPath}/login.action'  style='font-size: 14px;color: red;padding-top:5px;'>Salir</a><br /><br />
                      </td>
                     <td width=25% style='text-align:right;padding-right:40px;vertical-align:middle;'>
                      &nbsp;
                      </td>
                      </tr>
                      </table>
                      
<div id="content"></div>

<input type="hidden" id="ticketAccepted" value="${ticketAccepted}" >   
<input type="hidden" id="rfcValid" name="rfcValid" value="${rfcValid}" > 
<input type="hidden" id="razonSocialValid" name="razonSocialValid"  value = "${razonSocialValid}">
<input type="hidden" id="searchTypeValid" name="searchTypeValid"  value = "${searchTypeValid}">
<input type="hidden" id="approvalFlowUserValid" name="approvalFlowUserValid"  value = "${approvalFlowUserValid}">

<div style="text-align: center;margin-top:5px;"> 
<span style="font: 10px Tahoma, Verdana, sans-serif;">
                    	
	    &copy; 2000-2020 Smartech Consulting Group S.A. de C.V. Derechos Reservados.</span>
</div>

</body>
</html>