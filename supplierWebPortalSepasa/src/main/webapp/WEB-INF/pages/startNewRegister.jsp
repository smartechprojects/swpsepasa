<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="url" value="${pageContext.request.contextPath}"></c:set>
<html>

<head>
	<script type="text/javascript" charset="utf-8" src="${url}/ext-4.2.1/ext-all.js"></script>	
	
	<script>
	var SuppAppMsgLogin = {};
	
	var langu = window.navigator.language;
	var lang = "";
	if(langu.startsWith("es", 0)){
		lang = "es";
	}else{
		lang = "en";
	}
	
	Ext.Ajax.request({
	    url: 'getLocalization.action',
	    method: 'GET',
	    params: {
	    	lang : lang
        },
	    success: function(fp, o) {
	    	var resp = Ext.decode(fp.responseText, true);
	    	SuppAppMsgLogin = resp.data;
	    	document.getElementsByName('buttonLogin')[0].innerHTML = SuppAppMsgLogin.startButtonNewRegister;
	    	document.getElementById("startRegisterLegend").innerHTML = SuppAppMsgLogin.startMessageNewRegister;
	    	document.getElementById("errorRFC").innerHTML = SuppAppMsgLogin.errorRFCExist;
	    }
	}); 
	
	</script>
	

  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
  
  <link rel="shortcut icon" href="resources/images/favicon.ico"/>
  <title>Empresas Dragón&copy;-Nuevo Registro</title>
  <link rel="stylesheet" href="resources/css/login.css" media="screen" type="text/css" />
  
  <script>
	function login(form){
		document.getElementById("loading").style.display = "block";
	    form.submit();
	}
</script>

<style>
table {
  font-family: arial, sans-serif;
  border-collapse: collapse;
  width: 100%;
}

td {
  border: 1px solid #dddddd;
  text-align: left;
  padding: 8px;
  font-size: 90%;
}

th {
  border: 1px solid #dddddd;
  text-align: left;
  padding: 8px;
  background-color: #dddddd;
}

</style>

</head>

<body>
<div id="loading" style="display:none;"></div>

	<div class="wrap">
  <div style="margin-top:50px;margin-bottom:50px;text-align:center;font:30px Poppins-Bold, sans-serif;color:#333;">
   <p style="font-family: Poppins-Regular, sans-serif !important;">Nuevos Proveedores</p>
</div>

		<div class="avatar">
      <img src="resources/images/Hame-logo.png">
      <br /><br /><br /><br />
		</div>
		<p style="font-size:13px;" id="startRegisterLegend">Proporcione su RFC</p>
		
		
				<c:choose>
	         <c:when test="${param.error == 'true'}">
				<div class="error"><span style="color:red;font-weight:bold;">ERROR: </span>${fn:replace(SPRING_SECURITY_LAST_EXCEPTION.message, 'Credenciales incorrectas', 'Username/Password are incorrect')}</div>
		    </c:when>    
		</c:choose>
		<form action="${pageContext.request.contextPath}/public/validateTaxId" method="POST">
		<input type="text" onkeyup="this.value = this.value.toUpperCase();" placeholder="RFC/TaxId" name="taxId" autofocus required style="font:Poppins-Regular, sans-serif;">
		<input name="access_token" type="hidden" value="${access_token}">
		<br />
		<br />
		<button name="buttonLogin" type="submit" onClick="login(this.form)" >Enviar</button>
		</form>
		<br />	
		<p id="errorRFC" style="display:none;"></p>	
		<c:choose>
	         <c:when test="${errorRequest == 'ERROR: RFC or TAX ID Already exists'}">
				<script>document.getElementById("errorRFC").style.display = "block";</script>
		    </c:when>    
		</c:choose>
	
	</div>
	

	<div class="footer">
	    &copy; 2000-2021 Smartech Consulting Group S.A. de C.V. Derechos Reservados.</div>

</body>

</html>

