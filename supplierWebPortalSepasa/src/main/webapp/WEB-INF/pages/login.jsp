<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="url" value="${pageContext.request.contextPath}"></c:set>
<html>

<head>
	<script type="text/javascript" charset="utf-8" src="${url}/ext-4.2.1/ext-all.js"></script>	
	<script src="https://www.google.com/recaptcha/api.js"></script>
	
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
	    	
	    	document.getElementsByName('username')[0].placeholder= SuppAppMsgLogin.loginUser;
	    	document.getElementsByName('password')[0].placeholder= SuppAppMsgLogin.loginPass;
	    	document.getElementsByName('buttonLogin')[0].innerHTML = SuppAppMsgLogin.loginButtonAccess;
	    	//document.getElementsByName("newSupplierText")[0].value = SuppAppMsgLogin.loginNewSupplier;
	    	document.getElementsByClassName("forgetPass")[0].innerHTML = SuppAppMsgLogin.loginForgetPass;
	    	
	    }
	}); 
	
	//var x = document.getElementsByClassName("newSupplierButton");
	
	
	</script>
	

  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
  
  <link rel="shortcut icon" href="resources/images/favicon.ico"/>
  <title>SEPASA&copy;-Login</title>
  <link rel="stylesheet" href="resources/css/reset.css">
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
  		<div style="margin-top:30px;text-align:left;font:30px Tahoma, Verdana, sans-serif;color:#333;">
   			<p><img src="resources/images/hdr-logo.png"></p><br />
		</div>
		<div class="avatar">
      		<img src="resources/images/Hame-logo.png">
		</div>
					<c:set var="exceptionParts" value="${fn:split(SPRING_SECURITY_LAST_EXCEPTION.message, ',')}"/>
						<c:set var="errorMessage" value="${exceptionParts[0]}"/>
							<c:set var="providerId" value="${exceptionParts[1]}"/>
								<c:set var="providerName" value="${exceptionParts[2]}"/>
		         	
				<c:choose>
	         <c:when test="${param.error == 'true'}">
		         	<c:choose>
		         		
				         <c:when test="${errorMessage  == '¡Aviso Importante!'}"> 
				         	
							<div class="window-notice" id="window-notice">
						    <div class="content">
						        <div class="content-text" style="font-size:15px;text-align:center;color:red">¡Aviso Importante! </div><br>
<!-- 						        <div style="font-size:15px;text-align:center;color:red"> <p>Usted actualizó sus datos de proveedor, por lo cual; su cuenta ha sido bloqueada temporalmente, usted podrá ingresar nuevamente al portal una vez que los cambios hayan sido revisados y autorizados por SEPASA. </p> -->
						       <div style="font-size:15px;text-align:center;color:red"> <p>Su acceso al portal de proveedores con ID ${providerId} - ${providerName} ha sido temporalmente deshabilitado, debido a que usted recientemente actualizó sus datos. </p>
						        
						        <p>Esta actualización esta en proceso de revisión y aprobación  por parte de SEPASA; usted podrá ingresar nuevamente al portal una vez concluido dicho proceso.</p> 
						        
						        <br>
						        
						        	<br>
<!-- 						        	<p>¿Dudas? Favor de contactar a su comprador. Gracias</p> -->
						        	<p>Para revisar su estatus, por favor contacte a su comprador.</p>
						        	
						        </div>
						        <div class="content-buttons"><button style="height:30;width:60" onclick="closeNoLlogin()">Aceptar</button></div>
						    </div>
							</div>
					    </c:when>  
					    <c:otherwise>  
					       <div class="error"><span style="color:red;font-weight:bold;">ERROR: </span>${fn:replace(SPRING_SECURITY_LAST_EXCEPTION.message, 'Credenciales incorrectas', 'Username/Password are incorrect')}</div>  
					    </c:otherwise>  
					</c:choose>
		    </c:when>    
		</c:choose>
		<form action="${pageContext.request.contextPath}/login" method="POST">
		<input type="text" placeholder="usuario" name="username" autofocus required>
		
		<div class="bar">
			<i></i>
		</div>
		
		<input type="password" maxlength="20" placeholder="contraseña" name="password" required>
		<br />
		<div class="g-recaptcha" 
			data-sitekey="6LedD8giAAAAAOgxe7KlAb6VDk24QNQFnsJ73-tI"></div><br /><br />
		<button name="buttonLogin" type="submit" onClick="login(this.form)">Login</button>
		
		</form>
		<br />					
		<!-- <form action="${pageContext.request.contextPath}/newRegister.action" method="get">
			<input type="submit" value="Click aquí para registrarse como nuevo proveedor" class="newSupplierButton" name="newSupplierText" />
		</form> -->
		<form action="${pageContext.request.contextPath}/requestTicketPage.action" method="get">
			<input style="width:180px;" type="submit" value="Tickets de solicitud" class="newSupplierButton"/>
		</form>
		<br />
		<a class="forgetPass" href="${pageContext.request.contextPath}/requestResetPassword.action" style="font-size:11px;text-align:center;"
		                   onclick="window.open(this.href, 'Envío de contraseña','left=430,top=320,width=400,height=170,scrollbars=no,resizable=no,status=no,location=no,toolbar=no,menubar=no'); return false;" >Olvidó su contraseña?</a>		
	</div>


	<div class="footer">
	    &copy; 2000-2025 Smartech Consulting Group S.A. de C.V. Derechos Reservados.</div>

</body>

<script>
	function closeNoLlogin() {
		document.getElementById("window-notice").style.display = "none";
		location.href = "login.action"
		}
</script>

</html>

