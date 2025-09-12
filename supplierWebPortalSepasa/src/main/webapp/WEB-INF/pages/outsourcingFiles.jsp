<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ page import="java.util.Calendar"%>
<%@ page import="java.util.Date"%>

<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>  
<c:set var="url" value="${pageContext.request.contextPath}"></c:set>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
  <link rel="shortcut icon" href="resources/images/favicon.ico"/>
  <link href="<c:url value="/resources/css/pikaday.css" />" rel="stylesheet" type="text/css" />
  <title>SEPASA&copy;-Portal de proveedores</title>
<script>
	var displayName = '<c:out value="${name}"/>';
	var userName = '<c:out value="${userName}"/>';
	var message = '<c:out value="${message}"/>';
	var addressNumber = '<c:out value="${addressNumber}"/>';

</script>
<style>
/* Style all input fields */
body {
  font: 12px Tahoma, Verdana, sans-serif;
  background: #fff;
}

h2{
  font: 14px Tahoma, Verdana, sans-serif;
  font-weight:bold
}

h3{
  font: 18px Tahoma, Verdana, sans-serif;
  font-weight:bold
}

input {
  width: 180px;
  padding: 5px;
  border: 1px solid #ccc;
  border-radius: 4px;
  box-sizing: border-box;
  margin-top: 6px;
  margin-bottom: 16px;
  font: 12px Tahoma, Verdana, sans-serif;
}

/* Style the container for inputs */
.container {
  background-color: #fff;
  padding: 10px;
}

/* The message box is shown when the user clicks on the password field */
#message {
  display:none;
  background: #fff;
  color: #000;
  position: relative;
  padding: 2px;
  margin-top: 10px;
}

#message p {
  padding: 1px 35px;
  font-size: 11px;
}

/* Add a green text color and a checkmark when the requirements are right */
.valid {
  color: green;
}

.valid:before {
  position: relative;
  left: -35px;
  content: "✔";
}

/* Add a red text color and an "x" when the requirements are wrong */
.invalid {
  color: red;
}

.invalid:before {
  position: relative;
  left: -35px;
  content: "✖";
}

.hdrTable{
  height:9px;
  background-image: url(${url}/resources/images/Left-logo.png) !important; 
  background-repeat: no-repeat;
  background-size: 100% 100%;
}


.hdrTable tr td{
  color:#000;
  text-align:center;
  font: 14px Tahoma, Verdana, sans-serif;
}

.myButton {
	background-color:#03559c;
	-moz-border-radius:5px;
	-webkit-border-radius:5px;
	border-radius:5px;
	border:1px solid #B6985A;
	display:inline-block;
	cursor:pointer;
	width:150px;
	color:#ffffff;
	font-family:font: 18px Tahoma, Verdana, sans-serif;
	font-size:13px;
	padding:4px 17px;
	text-decoration:none;
}
.myButton:hover {
	background-color:#000;
}

table {
  font: 12px Tahoma, Verdana, sans-serif;
  }
</style>
</head>
<body>
               <table height='100%' width='90%' class='hdrTable'>  
            		<tr> 
            		  <td style='width:60%;'>  
            		     <table height='50%' width='100%'> 
            		      <tr><td style='width:200px;'> &nbsp;</td> 
            		          <td rowspan=2 style='font-size:26px;border-right:0px;color:#000;padding-bottom:7px;'>&nbsp;</td> 
            		          <td  rowspan=2 style='text-align:center;color:#000;font-size:17px;'>&nbsp;&nbsp; 
            		          Bienvenido <br />&nbsp;&nbsp;&nbsp;<script  type="text/javascript"> document.write(displayName)</script>  </td> 
            		          </tr> 
            		      </table> 
            		  </td> 
                      <td width:15%; style='text-align:right;padding-left:150px;vertical-align:middle;font: 16px Tahoma, Verdana, sans-serif; color:#000;'>  
                       <a href='j_spring_security_logout'  id='logoutLink' style='font-size: 14px;color: red;padding-top:5px;'>Salir</a> <br /><br /><br /> 
                      </td> 
                      <td width:5%; style='text-align:right;padding-right:40px;vertical-align:middle;'> 
                      &nbsp; 
                      </td> 
                      </tr></table>

<div class="container">

  <c:if test="${message=='_START'}">
  <h2>Solicitud de información adicional</h2>
	 Estimado proveedor. Nuestros registros indican que usted proporciona servicios especializados por lo que lo invitamos a enviarnos la siguiente documentación antes de poder utilizar este sistema. <br /><br />
	  <form:form id="form" commandName="multiFileUploadBean" method="post" action="uploadBaseLineFiles.action" enctype="multipart/form-data">
		<input type="hidden" id="addressNumber" name="addressNumber" value='<c:out value="${addressNumber}"/>' />
			<table>	
				<tr>
					<td>1. Registro y/o renovacion REPSE (PDF):&nbsp;
					<input type="file" name="uploadedFiles[0]" style="font-size:10px;width:330px;" required="required">
					</td>
				</tr>
				<!-- <tr>
					<td>1. Constancia de situación fiscal actualizada:&nbsp;
					<input type="file" name="uploadedFiles[0]" style="font-size:10px;width:330px;" required="required">
					</td>
				</tr>	
				<tr>
					<td>2. Autorización de la STPS y fecha de vigencia:&nbsp;
					<input type="file" name="uploadedFiles[1]" style="font-size:10px;width:330px;" required="required"> &nbsp;&nbsp;&nbsp;&nbsp;
					
					Fecha de vigencia:&nbsp;<input type="text" name="effectiveDate" id="datepicker" required="required">

					</td>
				</tr>
				<tr>
					<td>3. Listado de los trabajadores que colaboran con nosotros:&nbsp;
					<input type="file" name="uploadedFiles[2]" style="font-size:10px;width:330px;" required="required">
					</td>
				</tr>	
									
				<tr>
					<td>4. Acta protocolizada con detalle de su objeto social:&nbsp;
					<input type="file" name="uploadedFiles[3]" style="font-size:10px;width:330px;" required="required">
					</td>
				</tr>  -->
				 						
				<tr>
					<td><input id="submit" type="submit" value="Enviar archivos" class="myButton" /></td>
				</tr>
			</table>
		</form:form>
		Una vez que cargue los documentos, será dirigido a la página de Log In para ingresar de forma normal. <br /><br />
		
  </c:if>


  <c:if test="${message=='_STARTMONTH'}">
  <h2>La compañía ${relatedCompany} le solicita la siguiente información MENSUAL:  </h2>
  
  <%

	Date today = new Date();
	Calendar calendar = Calendar.getInstance();
	calendar.setTime(today);
	 
	calendar.add(Calendar.MONTH, -1);
	
	int monthLoad = (calendar.get(Calendar.MONTH));
	int yearLoad = calendar.get(Calendar.YEAR);
	
	System.out.println("monthLoad: " + monthLoad);
	System.out.println("yearLoad: " + yearLoad);
	
	String mes = "";
	
	if (monthLoad == 0) {
		mes = "Enero";
	} else if (monthLoad == 1){
		mes = "Febrero";
	} else if (monthLoad == 2){
		mes = "Marzo";
	} else if (monthLoad == 3){
		mes = "Abril";
	} else if (monthLoad == 4){
		mes = "Mayo";
	} else if (monthLoad == 5){
		mes = "Junio";
	} else if (monthLoad == 6){
		mes = "Julio";
	} else if (monthLoad == 7){
		mes = "Agosto";
	} else if (monthLoad == 8){
		mes = "Septiembre";
	} else if (monthLoad == 9){
		mes = "Octubre";
	} else if (monthLoad == 10){
		mes = "Noviembre";
	} else if (monthLoad == 11){
		mes = "Diciembre";
	}
  %>  
  
	 Estimado proveedor. Como proveedor de servicios sspecializados requerimos que porporcione los documentos de tipo MENSUAL por lo que  lo invitamos a enviarnos la siguiente documentación para continuar utilizando este sistema. <br /><br />
	  <form:form id="form" commandName="multiFileUploadBean" method="post" action="uploadMonthlyFiles.action" enctype="multipart/form-data">
		<input type="hidden" id="addressNumber" name="addressNumber" value='<c:out value="${addressNumber}"/>' />
			<table>
			
				<tr>
					<td>Mes:&nbsp;
					 	<select name="month" id="month">					 		
					        <option value="0">Enero</option>
					        <option value="1">Febrero</option>
					        <option value="2">Marzo</option>
					        <option value="3">Abril</option>
					        <option value="4">Mayo</option>
					        <option value="5">Junio</option>
					        <option value="6">Julio</option>
					        <option value="7">Agosto</option>
					        <option value="8">Septiembre</option>
					        <option value="9">Octubre</option>
					        <option value="10">Noviembre</option>
					        <option value="11">Diciembre</option>
					        <option value="<%= monthLoad %>" selected="<%= monthLoad %>"><%= mes %></option>
					    </select>
    				</td>
				</tr>
	
				<tr>				
					<td>Año:&nbsp;				    				
					 	<select name="year" id="year">					 		
					        <option value="2022">2022</option>
					        <option value="2023">2023</option>
					        <option value="2024">2024</option>
					        <option value="<%= yearLoad %>" selected="<%= yearLoad %>"><%= yearLoad %></option>
					    </select>
    				</td>        				
				</tr>	
				
				<tr>
					<td>1. Listado de Personal (Ciudad, Registro Patronal, Nombre, SBC, CURP) (Excel):&nbsp;
					<input type="file" name="uploadedFiles[0]" style="font-size:10px;width:350px;" required="required"> 
					</td>
				</tr>			
				<tr>
					<td>2. CFDIs (xmls, pdf):&nbsp;
					<input type="file" name="uploadedFiles[1]" style="font-size:10px;width:330px;" required="required">
					</td>
				</tr>
				<tr>
					<td>3. Opinion de cumplimiento SAT (PDF):&nbsp;
					<input type="file" name="uploadedFiles[2]" style="font-size:10px;width:330px;" required="required">
					</td>
				</tr>	
				<tr>
					<td>4. Opinion de cumplimiento IMSS (PDF):&nbsp;
					<input type="file" name="uploadedFiles[3]" style="font-size:10px;width:330px;" required="required">
					</td>
				</tr>	
				<tr>
					<td>5. Opinion de cumplimiento INFONAVIT (PDF):&nbsp;
					<input type="file" name="uploadedFiles[4]" style="font-size:10px;width:350px;" required="required"> 
					</td>
				</tr>	
				<tr>
					<td>6. Cedula de Determinacion de cuotas IMSS (PDF):&nbsp;
					<input type="file" name="uploadedFiles[5]" style="font-size:10px;width:330px;" required="required">
					</td>
				</tr>
				<tr>
					<td>7. Resumen de Liquidacion de cuotas IMSS (PDF):&nbsp;
					<input type="file" name="uploadedFiles[6]" style="font-size:10px;width:350px;" required="required"> 
					</td>
				</tr>			
				<tr>
					<td>8. Linea de Captura para pago de cuotas IMSS (PDF):&nbsp;
					<input type="file" name="uploadedFiles[7]" style="font-size:10px;width:330px;" required="required">
					</td>
				</tr>
				<tr>
					<td>9. Comprobante de pago de cuotas IMSS (PDF):&nbsp;
					<input type="file" name="uploadedFiles[8]" style="font-size:10px;width:330px;" required="required">
					</td>
				</tr>	
				<tr>
					<td>10. Declaracion de pago provisional mensual de IVA (PDF):&nbsp;
					<input type="file" name="uploadedFiles[9]" style="font-size:10px;width:330px;" required="required">
					</td>
				</tr>	
				<tr>
					<td>11. Acuse de Recibo y Linea de Captura de pago provisional mensual de IVA (PDF):&nbsp;
					<input type="file" name="uploadedFiles[10]" style="font-size:10px;width:350px;" required="required"> 
					</td>
				</tr>	
				<tr>
					<td>12. Comprobante de pago provisional mensual de IVA (PDF):&nbsp;
					<input type="file" name="uploadedFiles[11]" style="font-size:10px;width:330px;" required="required">
					</td>
				</tr>
				<tr>
					<td>13. Declaracion mensual de retenciones de ISR asalariados (PDF):&nbsp;
					<input type="file" name="uploadedFiles[12]" style="font-size:10px;width:350px;" required="required"> 
					</td>
				</tr>			
				<tr>
					<td>14. Acuse de Recibo y Linea de Captura de retencion mensual de ISR asalariados (PDF):&nbsp;
					<input type="file" name="uploadedFiles[13]" style="font-size:10px;width:330px;" required="required">
					</td>
				</tr>
				<tr>
					<td>15. Comprobante de pago de retenciones de ISR asalariados (PDF):&nbsp;
					<input type="file" name="uploadedFiles[14]" style="font-size:10px;width:330px;" required="required">
					</td>
				</tr>			
			
				<!-- <tr>
					<td>1. Acuse de declaración informativa mensual del IMSS:&nbsp;
					<input type="file" name="uploadedFiles[0]" style="font-size:10px;width:350px;" required="required"> 
					</td>
				</tr>			
				<tr>
					<td>2. Pagos provisionales de ISR por salarios:&nbsp;
					<input type="file" name="uploadedFiles[1]" style="font-size:10px;width:330px;" required="required">
					</td>
				</tr>
				<tr>
					<td>3. Pago de las cuotas obrero-patronales al IMSS:&nbsp;
					<input type="file" name="uploadedFiles[2]" style="font-size:10px;width:330px;" required="required">
					</td>
				</tr>	
				<tr>
					<td>4. Pago de ISN mensuales:&nbsp;
					<input type="file" name="uploadedFiles[3]" style="font-size:10px;width:330px;" required="required">
					</td>
				</tr>	
				<tr>
					<td>5. Evidencia del pago provisional de IVA:&nbsp;
					<input type="file" name="uploadedFiles[4]" style="font-size:10px;width:350px;" required="required"> 
					</td>
				</tr>	
				<tr>
					<td>6. Constancia de cumplimiento de obligaciones:&nbsp;
					<input type="file" name="uploadedFiles[5]" style="font-size:10px;width:330px;" required="required">
					</td>
				</tr>-->	
							
				<tr>
					<td><input id="submit" type="submit" value="Enviar archivos" class="myButton" /></td>
				</tr>
			</table>
		</form:form>
		Una vez que cargue los documentos, será dirigido a la página de Log In para ingresar de forma normal. <br /><br />
  </c:if>

	<c:if test="${message=='_STARTSECOND'}">
	<h2>Solicitud de información BIMESTRAL</h2>
	Estimado proveedor. Como proveedor de servicios especializados requerimos que porporcione los documentos de tipo BIMESTRAL por lo que  lo invitamos a enviarnos la siguiente documentación para continuar utilizando este sistema. <br /><br />
	<form:form id="form" commandName="multiFileUploadBean" method="post" action="uploadBimonthlyFiles.action" enctype="multipart/form-data">
	  <input type="hidden" id="addressNumber" name="addressNumber" value='<c:out value="${addressNumber}"/>' />
	  	<table>
	  			<tr>
					<td>1. Cedula de Determinacion de cuotas INFONAVIT (PDF):&nbsp;
					<input type="file" name="uploadedFiles[0]" style="font-size:10px;width:330px;" required="required">
					</td>
				</tr>
				<tr>
					<td>2. Resumen de Liquidacion de cuotas INFONAVIT (PDF):&nbsp;
					<input type="file" name="uploadedFiles[1]" style="font-size:10px;width:330px;" required="required">
					</td>
				</tr>
				<tr>
					<td>3. Linea de Captura para pago de cuotas INFONAVIT (PDF):&nbsp;
					<input type="file" name="uploadedFiles[2]" style="font-size:10px;width:330px;" required="required">
					</td>
				</tr>
				<tr>
					<td>4. Comprobante de pago de cuotas INFONAVIT (PDF):&nbsp;
					<input type="file" name="uploadedFiles[3]" style="font-size:10px;width:330px;" required="required">
					</td>
				</tr>
				<!-- <tr>
					<td>1. Pago de las aportaciones al INFONAVIT:&nbsp;
					<input type="file" name="uploadedFiles[0]" style="font-size:10px;width:330px;" required="required">
					</td>
				</tr> -->
				<tr>
					<td><input id="submit" type="submit" value="Enviar archivos" class="myButton" /></td>
				</tr>
			</table>
	</form:form>
	Una vez que cargue los documentos, será dirigido a la página de Log In para ingresar de forma normal. <br /><br />
	</c:if>

	<c:if test="${message=='_STARTQUARTER'}">
	<h2>Solicitud de información CUATRIMESTRAL</h2>
	Estimado proveedor. Como proveedor de servicios especializados requerimos que porporcione los documentos de tipo CUATRIMESTRAL por lo que  lo invitamos a enviarnos la siguiente documentación para continuar utilizando este sistema. <br /><br />
	<form:form id="form" commandName="multiFileUploadBean" method="post" action="uploadQuarterlyFiles.action" enctype="multipart/form-data">
	  <input type="hidden" id="addressNumber" name="addressNumber" value='<c:out value="${addressNumber}"/>' />
	  	<table>
	  			<tr>
					<td>1. Acuse de Declaracion cuatrimestral (IMSS-ICSOE):&nbsp;
					<input type="file" class="fileUpload" name="uploadedFiles[0]" style="font-size:10px;width:350px;" required="required"> 
					</td>
				</tr>
				<tr>
					<td>2. Acuse de Declaracion cuatrimestral (INFONAVIT-SISUB):&nbsp;
					<input type="file" class="fileUpload" name="uploadedFiles[1]" style="font-size:10px;width:350px;" required="required"> 
					</td>
				</tr>
				<!-- <tr>
					<td>1. Acuse de Informativa de Prestador de Servicios u Obras Especializados:&nbsp;
					<input type="file" class="fileUpload" name="uploadedFiles[0]" style="font-size:10px;width:350px;" required="required"> 
					</td>
				</tr> -->
				<tr>
					<td><input id="submit" type="submit" value="Enviar archivos" class="myButton" /></td>
				</tr>
			</table>
	</form:form>
	Una vez que cargue los documentos, será dirigido a la página de Log In para ingresar de forma normal. <br /><br />
	</c:if>

  <c:if test="${message=='_SUCCESS'}">
	  Hemos recibido sus documentos de forma exitosa. En breve la revisaremos y le notificaremos el resultado de la revisión
  </c:if>
  

  <c:if test="${message=='_SUCCESSMONTH' || message=='_SUCCESSSECOND' || message=='_SUCCESSQUARTER'}">
  	  <%-- Hemos recibido sus documentos de forma exitosa. --%>	  
	  <script type="text/javascript">
	  alert("Hemos recibido sus documentos de forma exitosa. En breve la revisaremos y le notificaremos el resultado de la revisión");
	  var myurl = '${url}';	  
	  window.location.replace(myurl + "/login");
	  </script>	 
  </c:if>  

  <c:if test="${message=='_ERROR'}">
	  <h2>Ha ocurrido un error con el envío de los documentos. Por favor, intente más tarde.</h2>
  </c:if>	

  <c:if test="${message=='_APPROVED'}">
	  El proveedor de OutSourcing: ${supplierName} ha sido APROBADO para la utilización del portal y notificado por correo electrónico.
  </c:if>

	<p style="background-color: crimson; color: #f1f1f1; font-weight: bold;"> ${msgstatus} </p>

</div>

  <script type="text/javascript" src="<c:url value="/resources/js/pikaday.js" />"></script>
  <script>

    var picker = new Pikaday({
        field: document.getElementById('datepicker'),
        format: 'D/M/YYYY',
        toString(date, format) {
            // you should do formatting based on the passed format,
            // but we will just return 'D/M/YYYY' for simplicity
            const day = date.getDate();
            const month = date.getMonth() + 1;
            const year = date.getFullYear();
            return day + '-' + month + '-' + year;
        }
    });
    
    var form = document.getElementById('form');
    var submitButton = document.getElementById('submit');

    form.addEventListener('submit', function() {
    // Disable the submit button
    submitButton.setAttribute('disabled', 'disabled');
    // Change the "Submit" text
    submitButton.value = 'Enviando Información...';
    			
    }, false);
    
</script>

</body>
</html>