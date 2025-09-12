<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<c:set var="url" value="${pageContext.request.contextPath}"></c:set>
<html>
<head>
<link rel="stylesheet" type="text/css"
	href="${url}/resources/css/login.css">
</head>
<body>

	<table class='hdrTable'>
		<tr>
			<td width=15%
				style='text-align: right; padding-right: 60px; padding-top:15px; vertical-align: middle;'>
				<form action="${pageContext.request.contextPath}/newRegister.action" method="get">
					<input type="submit" value="Registro de nuevos proveedores" class="myButton" />
				</form>
			</td>
		</tr>
	</table>


	<table width=100% style='padding: 0px'>
		<tr style='height: 463px'>
			<td width=55% style='padding-left:120px;font: 45px Tahoma, Verdana, sans-serif;text-right;'>
			<IMG SRC='resources/images/EuresLogin.png'></td>
			<td width=45%>
				<div class="login">
					<form action="${pageContext.request.contextPath}/login"
						method="POST">
						<table>
							<tr>
								<td style='height:50px;'><label for="username">Usuario:</label></td>
								<td><input style='height:30px;font-size:13px;' type="text" name="username" autofocus><br /></td>
							</tr>
							<tr style="margin-top: 15px;">
								<td><label for="password">Contraseña:</label></td>
								<td><input style='height:30px;font-size:13px;' type="password" name="password" /><br /></td>
							</tr>
							<tr>
								<td><br /><input type="submit" value="Ingresar" class="myButton" /></td>
							</tr>
							<tr>
							<td colspan=2 style='text-align:center;padding-top:20px;'>&nbsp;
								<c:if test="${param.error == 'true'}">
				    					<span style='font-size:14px;color:#2b6bc4;'>Las credenciales introducidas son incorrectas</span>
								</c:if>
							</td>
							</tr>
						</table>
					</form>
				</div>
			</td>
		</tr>
		<tr style='height: 95px;background:#fff;font-size:10px;color:#000;text-align:center;'>
			<td colspan=2>&copy;CopyRight - Derechos Reservados. Eurest 2018&nbsp;&nbsp;&nbsp;&nbsp;</td>
		</tr>
	</table>




</body>
</html>
