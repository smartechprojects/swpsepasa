Ext.define('SupplierApp.view.Main', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.mainpanel',
    autoWidth: false,
    id:'mainpanel',
    layout: 'border',
    height:370,
    border:false,
    defaults: {
        collapsible: false,
 		labelAlign: 'top',
 		border:false,
 		hideCollapseTool: true,
    },
    initComponent: function () {

    	var showItems = false;

    	if(multipleRfc != "empty"){
    		multipleRfc = multipleRfc.replace(/&#034;/g,'"');
    		var rfcList = Ext.JSON.decode(multipleRfc);
    		
    		var msg = 'Nuestro sistema ha identificado que tiene mútiples cuentas asociadas con el mismo RFC. <br /><br /> A continuación se muestra la lista de las cuentas disponibles. <br /><br />Debe seleccionar una de ellas para continuar.<br /><br /><select id="supplierRfcId" style="width:550px;height:20px;">';
    		for (var i = 0; i < rfcList.length; i++) {
    			msg = msg + '<option value="' + rfcList[i].addresNumber +'">' + rfcList[i].addresNumber + ' - ' + rfcList[i].rfc + ' - ' + rfcList[i].razonSocial + '</option>';
    		}
    		msg = msg + '</select><br />';
        	
        	Ext.MessageBox.show({
        	    title: 'Múltiples cuentas asociadas',
        	    msg: msg,
        	    buttons: Ext.MessageBox.OKCANCEL,
        	    fn: function (btn) {
        	        if (btn == 'ok') {
        	            addresNumber = Ext.get('supplierRfcId').getValue();
        	        	Ext.MessageBox.show({
        	        	    title: 'Aviso',
        	        	    msg: 'Su sesión se reiniciará con la cuenta: ' + addresNumber + '.',
        	        	    buttons: Ext.MessageBox.OKCANCEL,
        	        	    fn: function (btn) {
        	        	        if (btn == 'ok') {
        	        	        	var box = Ext.MessageBox.wait('Redireccionando a la nueva cuenta. Espere unos segundos', 'Redirección de cuentas');
        	        	            location.href = "homeUnique.action?userName=" + addresNumber;
        	        	        }else{
        	        	        	location.href = "j_spring_security_logout";
        	        	        }
        	        	    }
        	        	});
        	        }else{
        	        	location.href = "j_spring_security_logout";
        	        }
        	    }
        	});
        	
    	}else{
    		showItems = true;
    	}
    	

        if(showItems){
        	
        	this.items = [
        	    {
                region: 'north',
                height: 89,
                border: false,
                html: "<table height='100%' width='100%' class='hdrTable'> " +
                		"<tr>" +
                		  "<td style='width:60%;'> " +
                		     "<table height='70%' width='100%'>" +
                		      "<tr><td style='width:500px;'> &nbsp;</td>" +
                		          "<td rowspan=2 style='font-size:26px;border-right:1px solid #333;color:#000;padding-bottom:7px;'>&nbsp;</td>" +
                		          "<td  rowspan=2 style='width:700px;text-align:left;color:#000;font-size:17px;'>&nbsp;&nbsp; "  + displayName + " <br />&nbsp;&nbsp;<span style='font-size:14px;color:#666;'> " +SuppAppMsg.headerAccount + ":" + userName + "</span>" +
                		          "<br />&nbsp;&nbsp;&nbsp;<span style='font-size:12px;color:red;font-weight:bold;'>" + approveNotif + "</span></td>" +
                		          "</tr>" +
                		      "</table>" +
                		  "</td>" +
                          "<td width:15%; style='text-align:right;padding-left:150px;vertical-align:bottom;font: 16px Tahoma, Verdana, sans-serif; color:#000;'>" + 
                          " <a href='j_spring_security_logout'  id='logoutLink' style='font-size: 14px;color: red;'>" + SuppAppMsg.approvalExit  + "</a><br /><br />" +
                          "</td>" +
                          "<td width:5%; style='text-align:right;padding-right:40px;vertical-align:middle;'>" +
                          "&nbsp;" +
                          "</td>" +
                          "</tr></table>"
            },{
        		collapsible: false,
    		    region:'center',
    		    id:'mainContent',
    		    height:500,
                border:false,
    		    items:[
    		           {
    		        	   xtype:'homeTabs'
    		           }],
    		    bodyStyle: 'padding-top:0px;background: #fff;text-align:center;'
    			}
            ];	
        }
    	

	    this.callParent(arguments);
    }
});