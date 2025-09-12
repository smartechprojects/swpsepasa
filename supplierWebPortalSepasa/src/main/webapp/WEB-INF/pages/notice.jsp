<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="url" value="${pageContext.request.contextPath}"></c:set>
<html>
<head>
<meta name="viewport" content="width=device-width, initial-scale=1">

	<link rel="stylesheet" type="text/css" href="${url}/ext-4.2.1/resources/css/ext-all-gray.css">
	<script type="text/javascript" charset="utf-8" src="${url}/ext-4.2.1/ext-all.js"></script>	
	<link rel="stylesheet" type="text/css" href="${url}/resources/css/app.css">
	<script type="text/javascript" charset="utf-8" src="${url}/ext-4.2.1/locale/ext-lang-es.js"></script>	
	<script type="text/javascript" charset="utf-8" src="${url}/resources/js/common.js"></script>

<script>
var displayName = '<c:out value="${name}"/>';
var userName = '<c:out value="${userName}"/>';
var role = '<c:out value="${role}"/>';

	

	Ext.Ajax.request({
		url : 'notice/noticeActivesBySupp.action',
		method : 'GET',
			params : {
				supp : userName
			},
			success : function(response,opts) {

				response = Ext.decode(response.responseText);
				//rec = Ext.create('SupplierApp.model.NoticeDTO');
				//rec.set(response.data);
	        	//form.loadRecord(rec);
	        	
	        	// Obtén el contenido Base64 del archivo
			    const base64Content = response.data.docNotice;
	        	
			    const binaryContent = atob(base64Content);
			      const byteArray = new Uint8Array(binaryContent.length);
			      for (let i = 0; i < binaryContent.length; i++) {
			        byteArray[i] = binaryContent.charCodeAt(i);
			      }
			      const pdfBlob = new Blob([byteArray], { type: 'application/pdf' });
			
			    // Decodifica el contenido y crea una URL del archivo
			    //const pdfBlob = base64ToPDF(base64Content);
			    const pdfUrl = URL.createObjectURL(pdfBlob);
			
			    // Establece la URL del archivo en el iframe
			    const pdfViewer = document.getElementById('pdfViewer');
			    pdfViewer.src = pdfUrl;
			    
			    document.getElementById('idNoticeDoc').value = response.data.idNotice;
			    document.getElementById('titleNotice').innerHTML  = response.data.noticeTitle;
			    
			    if(response.data.docSupplier == false){
			    	const docSup = document.getElementById('docSupplier');

				    // Disable the submit button
				    docSup.removeAttribute('required');
				    docSup.style.visibility = "hidden";
				    document.getElementById('attachOblig').style.visibility = "hidden";
				    document.getElementById('attachOblig1').style.visibility = "hidden";
				    document.getElementById('attachOblig2').style.visibility = "hidden";
				    document.getElementById('attachOblig3').style.visibility = "hidden";
				    document.getElementById('attachOblig4').style.visibility = "hidden";
				    //docSup.setAttribute('type', 'hidden');
			    }else{
			    	document.getElementById('rejectButton').value  = 'CARGAR MAS TARDE';
			    }
			    
			    if(!response.data.required){
			    	document.getElementById('textOblig').style.visibility = "hidden";
			    }
			    
		}
	});
</script>

<style>
/* Style all input fields */
body {
  font: 14px Tahoma, Verdana, sans-serif;
  background: #fff;
}

h2{
  font: 24px Tahoma, Verdana, sans-serif;
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
  height:99px;
  background-image: url(${url}/resources/images/topBanner3.png) !important; 
  background-repeat: no-repeat;
  background-size: 100% 100%;
}

.hdrTable tr td{
  color:#000;
  text-align:center;
  font: 14px Tahoma, Verdana, sans-serif;
}

.myButton {
	background-color:#B6985A;
	-moz-border-radius:5px;
	-webkit-border-radius:5px;
	border-radius:5px;
	border:1px solid #B6985A;
	display:inline-block;
	cursor:pointer;
	width:100px;
	color:#ffffff;
	font-family:font: 18px Tahoma, Verdana, sans-serif;
	font-size:13px;
	padding:2px 17px;
	text-decoration:none;
	text-shadow:0px -1px 0px #B6985A;
}
.myButton:hover {
	background-color:#000;
}

</style>
</head>
<body>

               <table height='100%' width='100%' class='hdrTable'>  
            		<tr> 
            		  <td style='width:60%;'>  
            		     <table height='50%' width='100%'> 
            		      <tr><td style='width:200px;'> &nbsp;</td> 
            		          <td rowspan=2 style='font-size:26px;border-right:1px solid #333;color:#000;padding-bottom:7px;'>&nbsp;</td> 
            		          <td  rowspan=2 style='width:700px;text-align:left;color:#000;font-size:17px;'>&nbsp;&nbsp; 
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

<div class="container" style="margin-left:100px;margin-right:100px;text-align: justify; text-justify: inter-word;border:0px;padding:20px;">
<h2 id="titleNotice"></h2>

	<p><b id="attachOblig" style="font-size: 15px;color:red;">Para poder ingresar a nuestro portal nuevamente usted debe:</b></p>
	<b id="attachOblig1" style="font-size: 15px;color:red;">1.-Leer detalladamente el aviso anexo.</b><br>
	<b id="attachOblig2" style="font-size: 15px;color:red;">2.-Descargarlo y firmarlo como signo de conocimiento y aceptación (revisar si la firma solicitada es la del representante legal).</b><br>
	<b id="attachOblig3" style="font-size: 15px;color:red;">3.-Cargar el documento firmado en la seccion "Seleccionar archivo"</b><br>
	<b id="attachOblig4" style="font-size: 15px;color:red;">4.-Para enviar el aviso, dar click en ACEPTAR.</b><br>
  
  <p style="text-align:center;color:red;">${errorRequest}</p>
  
  <form action="${pageContext.request.contextPath}/uploadFileNoticeSupplier.action" method="post" enctype="multipart/form-data">
    <table>
        <tr>
            <td><input type="hidden"  id="idNoticeDoc" name="idNoticeDoc"  value = "">
            </td>
        </tr>
        <tr>
            <td><input type="hidden" id="createdBy" name="createdBy"  value = "${userName}">
            </td>
        </tr>
        <tr>
            <td><input type="hidden"  id="statusNotice" name="statusNotice"  value = "">
            </td>
        </tr>
        <tr>
            <td><input id="docSupplier" type="file"  name="docSupp" style="font-size:10px;width:240px;" required="required">
            </td>
        </tr>
        <tr>
            <td>
			    <input type="submit" value="ACEPTAR" onclick="aceptar()" style="width:225px;margin:auto;">
            </td>
            <td>
            	<p style="font-size: 13px;">Al dar click en aceptar usted confirma que su representada ha leído, comprendido y aceptado el aviso anexo.</p>
            </td>
        </tr>
        <tr>
            <td>
			    <input id="rejectButton" type="submit" value="RECHAZAR" onclick="rechazar()" style="width:225px;margin:auto;">
            </td>
            <td>
            	<p style="font-size: 13px;"><b id="textOblig">Por favor considere que hasta no aceptar nuestro aviso usted no podrá ingresar a nuestro portal de proveedores.</b></p>
            </td>
        </tr>
    </table>
</form>

<iframe id="pdfViewer" width="100%" height="500px"></iframe>

</div>


</body>

<script>   
function aceptar() {   
		document.getElementById('statusNotice').value = "ACEPTADO";
	}

function rechazar() {
		const docSup = document.getElementById('docSupplier').removeAttribute('required');

		document.getElementById('statusNotice').value = "RECHAZADO";  
	}   
</script>  

</html>
